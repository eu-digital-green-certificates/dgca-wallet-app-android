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
import dgca.verifier.app.decoder.CertificateDecoder
import dgca.verifier.app.decoder.CertificateDecodingResult
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.local.AppDatabase
import dgca.wallet.app.android.data.local.toCertificateModel
import dgca.wallet.app.android.security.KeyStoreCryptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CertificatesViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val certificateDecoder: CertificateDecoder,
    private val cryptor: KeyStoreCryptor
) : ViewModel() {

    private val _certificates = MutableLiveData<List<CertificateCard>>()
    val certificates: LiveData<List<CertificateCard>> = _certificates

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    fun fetchCertificates() {
        _inProgress.value = true
        viewModelScope.launch {
            var certificateCards: List<CertificateCard>? = null
            withContext(Dispatchers.IO) {
                certificateCards =
                    appDatabase.certificateDao().getAll().map { encryptedCertificate ->
                        val certificate =
                            encryptedCertificate.copy(qrCodeText = cryptor.decrypt(encryptedCertificate.qrCodeText)!!)
                        // We assume that we do not store invalid QR codes, thus here, no errors should appear.
                        val certificateModel: CertificateModel =
                            (certificateDecoder.decodeCertificate(certificate.qrCodeText) as CertificateDecodingResult.Success).greenCertificate.toCertificateModel()
                        CertificateCard(
                            certificate,
                            certificateModel
                        )
                    }
            }
            _inProgress.value = false
            _certificates.value = certificateCards ?: emptyList()
        }
    }
}