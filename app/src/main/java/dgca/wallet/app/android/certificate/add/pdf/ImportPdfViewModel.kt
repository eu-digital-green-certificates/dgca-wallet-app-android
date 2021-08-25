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
 *  Created by osarapulov on 8/23/21 10:16 AM
 */

package dgca.wallet.app.android.certificate.add.pdf

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ImportPdfViewModel @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {
    private val _result = MutableLiveData<Boolean>()
    val result: LiveData<Boolean> = _result

    fun save(uri: Uri?) {
        var res = false
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (uri != null) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val file = File(
                            File(context.filesDir, "images").apply { if (!isDirectory || !exists()) mkdirs() },
                            "${System.currentTimeMillis()}.pdf"
                        )
                        FileOutputStream(file).use { outputStream ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                        }
                        if (file.exists()) {
                            res = true
                        }
                    }
                }
            }
            _result.value = res
        }
    }
}