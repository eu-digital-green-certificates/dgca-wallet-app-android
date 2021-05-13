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

package dgca.wallet.app.android.certificate.view

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.certificate.CertificateCard
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.qr.QrCodeConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CertificateViewCard(val certificateCard: CertificateCard, val qrCode: Bitmap)

@HiltViewModel
class ViewCertificateViewModel @Inject constructor(
    private val qrCodeConverter: QrCodeConverter,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _certificate = MutableLiveData<CertificateViewCard>()
    val certificate: LiveData<CertificateViewCard> = _certificate

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    fun setCertificateId(certificateId: Int, qrCodeSize: Int) {
        viewModelScope.launch {
            _inProgress.value = true
            var qrCode: Bitmap

            val certificateCard = walletRepository.getCertificatesById(certificateId)
            if (certificateCard == null) {
                _inProgress.value = false
                return@launch
            }

            withContext(Dispatchers.IO) {
                qrCode = qrCodeConverter.convertStringIntoQrCode(certificateCard.qrCodeText, qrCodeSize)
            }
            _certificate.value = CertificateViewCard(certificateCard, qrCode)
            _inProgress.value = false
        }
    }
}