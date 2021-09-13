/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
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
 *  Created by osarapulov on 9/12/21 1:36 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.local.toCertificateModel
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.GreenCertificateFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class ModelFetcherResult {
    class GreenCertificateRecognised(
        val qrCodeText: String,
        val dgci: String,
        val cose: ByteArray,
        val certificateModel: CertificateModel
    ) : ModelFetcherResult()

    class BookingSystemModelRecognised(val bookingSystemModel: BookingSystemModel) : ModelFetcherResult()

    object NotApplicable : ModelFetcherResult()
}

@HiltViewModel
class ModelFetcherViewModel @Inject constructor(
    private val greenCertificateFetcher: GreenCertificateFetcher,
) : ViewModel() {
    private val _modelFetcherResult = MutableLiveData<ModelFetcherResult>()
    val modelFetcherResult: LiveData<ModelFetcherResult> = _modelFetcherResult

    fun init(qrCodeText: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val greenCertificateRecognised: ModelFetcherResult.GreenCertificateRecognised? =
                    tryToFetchGreenCertificate(qrCodeText)
                if (greenCertificateRecognised != null) return@withContext greenCertificateRecognised
                return@withContext ModelFetcherResult.NotApplicable
            }.apply {
                _modelFetcherResult.value = this
            }
        }
    }

    private fun tryToFetchGreenCertificate(qrCodeText: String): ModelFetcherResult.GreenCertificateRecognised? {
        val res: Pair<ByteArray?, GreenCertificate?> = greenCertificateFetcher.fetchDataFromQrString(qrCodeText)
        val cose: ByteArray? = res.first
        val greenCertificate: GreenCertificate? = res.second
        return if (cose != null && greenCertificate != null) {
            val certificateModel = greenCertificate.toCertificateModel()
            ModelFetcherResult.GreenCertificateRecognised(qrCodeText, greenCertificate.getDgci(), cose, certificateModel)
        } else {
            null
        }
    }
}