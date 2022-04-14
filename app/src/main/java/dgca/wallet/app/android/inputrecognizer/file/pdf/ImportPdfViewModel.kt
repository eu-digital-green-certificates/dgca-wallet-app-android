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
 *  Created by osarapulov on 9/10/21 1:00 PM
 */

package dgca.wallet.app.android.inputrecognizer.file.pdf

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dgca.wallet.app.android.ProtocolHandler
import dgca.wallet.app.android.inputrecognizer.file.BitmapFetcher
import dgca.wallet.app.android.inputrecognizer.file.FileSaver
import dgca.wallet.app.android.inputrecognizer.file.QrCodeFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportPdfViewModel @Inject constructor(
    private val bitmapFetcher: BitmapFetcher,
    private val qrCodeFetcher: QrCodeFetcher,
    private val fileSaver: FileSaver,
    private val protocolHandler: ProtocolHandler
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
        val qrStrings = mutableListOf<String>()
        var bitmaps: List<Bitmap>? = null
        try {
            bitmaps = bitmapFetcher.loadBitmapByPdfUri(this)
            bitmaps.forEach { bitmap ->
                qrStrings.add(qrCodeFetcher.fetchQrCodeString(bitmap))
                bitmap.recycle()
            }
        } catch (exception: Exception) {
            Timber.d(exception, "Error fetching qr strings from bitmaps")
        } finally {
            bitmaps?.forEach { bitmap -> bitmap.recycle() }
        }

        var certDetectedIntent: Intent? = null
        qrStrings.forEach { curQrString ->
            protocolHandler.handle(curQrString)?.let {
                certDetectedIntent = it
                return@forEach
            }
        }

        return if (certDetectedIntent != null) {
            ImportPdfResult.CertificateRecognised(certDetectedIntent!!)
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

sealed class ImportPdfResult {
    object Failed : ImportPdfResult()
    object Success : ImportPdfResult()
    class CertificateRecognised(val intent: Intent) : ImportPdfResult()
}
