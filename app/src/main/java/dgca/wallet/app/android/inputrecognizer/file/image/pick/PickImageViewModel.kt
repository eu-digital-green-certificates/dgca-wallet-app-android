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

package dgca.wallet.app.android.inputrecognizer.file.image.pick

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PickImageViewModel @Inject constructor(
    private val qrCodeFetcher: QrCodeFetcher,
    private val bitmapFetcher: BitmapFetcher,
    private val fileSaver: FileSaver,
    private val protocolHandler: ProtocolHandler
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

        if (qrCodeString != null) {
            protocolHandler.handle(qrCodeString)?.let {
                return PickImageResult.CertificateRecognised(it)
            }
        }

        val file = try {
            fileSaver.saveFileFromUri(this, "images", "${System.currentTimeMillis()}.jpeg")
        } catch (exception: Exception) {
            null
        }

        return if (file?.exists() == true && file.isFile) PickImageResult.Success else PickImageResult.Failed
    }
}

sealed class PickImageResult {
    object Failed : PickImageResult()
    object Success : PickImageResult()
    class CertificateRecognised(val intent: Intent) : PickImageResult()
}
