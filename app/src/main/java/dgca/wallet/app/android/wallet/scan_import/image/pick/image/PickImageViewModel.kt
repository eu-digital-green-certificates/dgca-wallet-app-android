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
 *  Created by osarapulov on 10/21/21 9:25 AM
 */

package dgca.wallet.app.android.wallet.scan_import.image.pick.image

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.local.toCertificateModel
import dgca.wallet.app.android.wallet.scan_import.BitmapFetcher
import dgca.wallet.app.android.wallet.scan_import.FileSaver
import dgca.wallet.app.android.wallet.scan_import.GreenCertificateFetcher
import dgca.wallet.app.android.wallet.scan_import.QrCodeFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class PickImageResult {
    object Failed : PickImageResult()
    object Success : PickImageResult()
    class GreenCertificateRecognised(
        val qrCodeText: String,
        val dgci: String,
        val cose: ByteArray,
        val certificateModel: CertificateModel
    ) : PickImageResult()
}

@HiltViewModel
class PickImageViewModel @Inject constructor(
    private val qrCodeFetcher: QrCodeFetcher,
    private val bitmapFetcher: BitmapFetcher,
    private val fileSaver: FileSaver,
    private val greenCertificateFetcher: GreenCertificateFetcher
) : ViewModel() {

    private val _result = MutableLiveData<PickImageResult>()
    val result: LiveData<PickImageResult> = _result

    fun save(uri: Uri?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { uri?.handle() ?: PickImageResult.Failed }.apply {
                _result.value = this
            }
        }
    }

    private fun Uri.handle(): PickImageResult {
        val qrCodeString: String? = try {
            bitmapFetcher.loadBitmapByImageUri(this).let { bitmap -> qrCodeFetcher.fetchQrCodeString(bitmap) }
        } catch (exception: Exception) {
            null
        }

        val greenCertificateData = qrCodeString?.let { qrString -> greenCertificateFetcher.fetchDataFromQrString(qrString) }
        val cose: ByteArray? = greenCertificateData?.first
        val greenCertificate: GreenCertificate? = greenCertificateData?.second

        return if (greenCertificate != null && cose != null) {
            PickImageResult.GreenCertificateRecognised(
                qrCodeString,
                greenCertificate.getDgci(),
                cose,
                greenCertificate.toCertificateModel()
            )
        } else {
            val file = try {
                fileSaver.saveFileFromUri(this, "images", "${System.currentTimeMillis()}.jpeg")
            } catch (exception: Exception) {
                null
            }
            if (file?.exists() == true && file.isFile) PickImageResult.Success else PickImageResult.Failed
        }
    }
}

