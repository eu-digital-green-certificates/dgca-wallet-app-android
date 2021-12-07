/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by osarapulov on 5/10/21 11:48 PM
 */

package dgca.wallet.app.android.certificate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.Event
import dgca.wallet.app.android.certificate.view.certificate.ViewCertificateViewModel
import dgca.wallet.app.android.certificate.view.file.ViewFileViewModel
import dgca.wallet.app.android.data.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CertificatesViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _certificates = MutableLiveData<List<CertificatesCard>>()
    val certificates: LiveData<List<CertificatesCard>> = _certificates

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    fun fetchCertificates(filesDir: File) {
        _inProgress.value = true
        viewModelScope.launch {
            val certificateCards = walletRepository.getCertificates()
            val certificatesCards = mutableListOf<CertificatesCard>()
            if (certificateCards?.isNotEmpty() == true) {
                certificatesCards.add(CertificatesCard.CertificatesHeader)
                certificatesCards.addAll(certificateCards)
            }

            val imagesDir = File(filesDir, "images")
            val imageFileCards = mutableListOf<CertificatesCard.FileCard>()
            val pdfFileCards = mutableListOf<CertificatesCard.FileCard>()
            imagesDir.listFiles()?.reversed()?.forEach { file ->
                when (file.extension) {
                    "jpeg" -> imageFileCards.add(CertificatesCard.FileCard(file))
                    "pdf" -> pdfFileCards.add(CertificatesCard.FileCard(file))
                    else -> {
                    }
                }
            }

            if (imageFileCards.isNotEmpty()) {
                certificatesCards.add(CertificatesCard.ImagesHeader)
                certificatesCards.addAll(imageFileCards)
            }

            if (pdfFileCards.isNotEmpty()) {
                certificatesCards.add(CertificatesCard.PdfsHeader)
                certificatesCards.addAll(pdfFileCards)
            }

            _inProgress.value = false
            _certificates.value = certificatesCards.toList()
        }
    }

    fun deleteCertificate(certificateId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                walletRepository.deleteCertificateById(certificateId)
            }
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                file.delete() || !file.exists()
            }
        }
    }
}