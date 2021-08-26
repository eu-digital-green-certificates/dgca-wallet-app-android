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
 *  Created by osarapulov on 8/25/21 6:45 PM
 */

package dgca.wallet.app.android.certificate.add.pdf

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.wallet.app.android.certificate.GreenCertificateFetcher
import dgca.wallet.app.android.certificate.add.BitmapFetcher
import dgca.wallet.app.android.certificate.add.FileSaver
import dgca.wallet.app.android.certificate.add.QrCodeFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class ImportPdfResult {
    object Failed : ImportPdfResult()
    object Success : ImportPdfResult()
    class QrRecognised(val qr: String) : ImportPdfResult()
}

@HiltViewModel
class ImportPdfViewModel @Inject constructor(
    private val bitmapFetcher: BitmapFetcher,
    private val qrCodeFetcher: QrCodeFetcher,
    private val fileSaver: FileSaver,
    private val greenCertificateFetcher: GreenCertificateFetcher
) : ViewModel() {
    private val _result = MutableLiveData<ImportPdfResult>()
    val result: LiveData<ImportPdfResult> = _result

    fun save(uri: Uri?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { uri?.handle() ?: ImportPdfResult.Failed }.apply {
                _result.value = this
            }
        }
    }

    private fun Uri.handle(): ImportPdfResult {
        val qrCodeString: String? = try {
            bitmapFetcher.loadBitmapByPdfUri(this)
                .let { bitmap -> qrCodeFetcher.fetchQrCodeString(bitmap) }
        } catch (exception: Exception) {
            null
        }

        val greenCertificate: GreenCertificate? =
            qrCodeString?.let { qrString -> greenCertificateFetcher.fetchGreenCertificateFromQrString(qrString) }

        return if (greenCertificate != null) {
            ImportPdfResult.QrRecognised(qrCodeString)
        } else {
            val file = try {
                fileSaver.saveFileFromUri(this, "images", "${System.currentTimeMillis()}.pdf")
            } catch (exception: Exception) {
                null
            }
            if (file?.exists() == true && file.isFile) ImportPdfResult.Success else ImportPdfResult.Failed
        }
    }
}