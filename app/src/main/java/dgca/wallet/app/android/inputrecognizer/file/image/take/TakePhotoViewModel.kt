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

package dgca.wallet.app.android.inputrecognizer.file.image.take

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dgca.wallet.app.android.ProtocolHandler
import dgca.wallet.app.android.inputrecognizer.file.BitmapFetcher
import dgca.wallet.app.android.inputrecognizer.file.FileSaver
import dgca.wallet.app.android.inputrecognizer.file.QrCodeFetcher
import dgca.wallet.app.android.inputrecognizer.file.UriProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TakePhotoViewModel @Inject constructor(
    private val qrCodeFetcher: QrCodeFetcher,
    private val bitmapFetcher: BitmapFetcher,
    uriProvider: UriProvider,
    private val fileSaver: FileSaver,
    private val protocolHandler: ProtocolHandler
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

        if (qrCodeString != null) {
            protocolHandler.handle(qrCodeString)?.let {
                return TakePhotoResult.CertificateRecognised(it)
            }
        }

        val file = try {
            fileSaver.saveFileFromUri(this, "images", "${System.currentTimeMillis()}.jpeg")
        } catch (exception: Exception) {
            null
        }

        return if (file?.exists() == true && file.isFile) TakePhotoResult.Success else TakePhotoResult.Failed
    }
}

sealed class TakePhotoResult {
    object Failed : TakePhotoResult()
    object Success : TakePhotoResult()
    class CertificateRecognised(val intent: Intent) : TakePhotoResult()
}
