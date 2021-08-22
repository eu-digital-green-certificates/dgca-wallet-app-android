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

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dgca.wallet.app.android.data.WalletRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CertificatesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _certificates = MutableLiveData<List<CertificatesCard>>()
    val certificates: LiveData<List<CertificatesCard>> = _certificates

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    fun fetchCertificates() {
        _inProgress.value = true
        viewModelScope.launch {
            val certificateCards = walletRepository.getCertificates()
            val certificatesCards = mutableListOf<CertificatesCard>()
            if (certificateCards?.isNotEmpty() == true) {
                certificatesCards.add(CertificatesCard.CertificatesHeader)
                certificatesCards.addAll(certificateCards)
            }

            val imagesDir = File(context.filesDir, "images")
            val imageFiles = imagesDir.listFiles().filter {
                Timber.tag("MYTAG").d("File: ${it.path}")
                return@filter true
            }

            if (imageFiles.isNotEmpty()) {
                certificatesCards.add(CertificatesCard.ImagesHeader)
                imageFiles.forEach {
                    certificatesCards.add(CertificatesCard.FileCard(it))
                }
            }

            _inProgress.value = false
            _certificates.value = certificatesCards.toList()
        }
    }
}