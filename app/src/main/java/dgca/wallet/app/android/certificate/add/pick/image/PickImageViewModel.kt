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
 *  Created by osarapulov on 8/25/21 12:12 PM
 */

package dgca.wallet.app.android.certificate.add.pick.image

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.certificate.add.FileSaver
import dgca.wallet.app.android.certificate.add.QrCodeFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class PickImageResult {
    object Failed : PickImageResult()
    object Success : PickImageResult()
    class QRRecognised(val qr: String) : PickImageResult()
}

@HiltViewModel
class PickImageViewModel @Inject constructor(
    private val qrCodeFetcher: QrCodeFetcher,
    private val fileSaver: FileSaver
) : ViewModel() {
    private val _result = MutableLiveData<PickImageResult>()
    val result: LiveData<PickImageResult> = _result

    fun save(uri: Uri?) {
        viewModelScope.launch {
            val res: PickImageResult = withContext(Dispatchers.IO) {
                uri?.handle() ?: PickImageResult.Failed
            }
            _result.value = res
        }
    }

    private fun Uri.handle(): PickImageResult {
        val qrCodeString: String? = try {
            qrCodeFetcher.fetchQrCodeStringByUri(this)
        } catch (exception: Exception) {
            null
        }

        return if (qrCodeString?.isNotBlank() == true) {
            PickImageResult.QRRecognised(qrCodeString)
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