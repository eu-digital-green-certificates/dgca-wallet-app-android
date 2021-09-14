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
 *  Created by osarapulov on 9/10/21 1:01 PM
 */

package dgca.wallet.app.android.wallet.scan_import.image.take.photo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.local.toCertificateModel
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class TakePhotoResult {
    object Failed : TakePhotoResult()
    object Success : TakePhotoResult()
    class GreenCertificateRecognised(
        val qrCodeText: String,
        val dgci: String,
        val cose: ByteArray,
        val certificateModel: CertificateModel
    ) : TakePhotoResult()

    class BookingSystemModelRecognised(
        val bookingSystemModel: BookingSystemModel
    ) : TakePhotoResult()
}

@HiltViewModel
class TakePhotoViewModel @Inject constructor(
    private val qrCodeFetcher: QrCodeFetcher,
    private val bitmapFetcher: BitmapFetcher,
    private val uriProvider: UriProvider,
    private val fileSaver: FileSaver,
    private val greenCertificateFetcher: GreenCertificateFetcher,
    private val objectMapper: ObjectMapper
) : ViewModel() {
    val uriLiveData: LiveData<Uri> = MutableLiveData(uriProvider.getUriFor("temp", "temp.jpeg"))
    private val _result = MutableLiveData<TakePhotoResult>()
    val result: LiveData<TakePhotoResult> = _result

    fun handleResult() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                uriLiveData.value?.handleUri() ?: TakePhotoResult.Failed
            }.apply {
                _result.value = this
            }
        }
    }

    private fun Uri.handleUri(): TakePhotoResult {
        val qrCodeString: String? = try {
            bitmapFetcher.loadBitmapByImageUri(this).let { bitmap -> qrCodeFetcher.fetchQrCodeString(bitmap) }
        } catch (exception: Exception) {
            null
        }
        val greenCertificateData = qrCodeString?.let { qrString -> greenCertificateFetcher.fetchDataFromQrString(qrString) }
        val cose: ByteArray? = greenCertificateData?.first
        val greenCertificate: GreenCertificate? = greenCertificateData?.second

        return when {
            greenCertificate != null && cose != null && uriProvider.deleteFileByUri(
                this
            ) -> {
                TakePhotoResult.GreenCertificateRecognised(
                    qrCodeString,
                    greenCertificate.getDgci(),
                    cose,
                    greenCertificate.toCertificateModel()
                )
            }
            else -> {
                runCatching {
                    objectMapper.readValue(qrCodeString, BookingSystemModel::class.java)
                        .let { TakePhotoResult.BookingSystemModelRecognised(it) }
                }.getOrElse {
                    val file = try {
                        fileSaver.saveFileFromUri(this, "images", "${System.currentTimeMillis()}.jpeg")
                    } catch (exception: Exception) {
                        null
                    }
                    if (file?.exists() == true && file.isFile) TakePhotoResult.Success else TakePhotoResult.Failed
                }
            }
        }
    }
}