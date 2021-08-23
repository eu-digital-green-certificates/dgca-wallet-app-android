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
 *  Created by osarapulov on 8/23/21 1:56 PM
 */

package dgca.wallet.app.android.certificate.view.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ViewFileViewModel @Inject constructor() : ViewModel() {
    private val _event = MutableLiveData<Event<ViewFileEvent>>()
    val event: LiveData<Event<ViewFileEvent>> = _event

    private val _image = MutableLiveData<Bitmap?>()
    val image: LiveData<Bitmap?> = _image

    fun init(file: File, width: Double) {
        viewModelScope.launch {
            var bitmap: Bitmap? = null
            withContext(Dispatchers.IO) {
                bitmap = when (file.extension) {
                    "jpeg" -> {
                        BitmapFactory.decodeFile(file.absolutePath)
                    }
                    "pdf" -> {
                        prepareBitmapFromPdf(file, width)
                    }
                    else -> throw IllegalArgumentException()
                }
            }
            _image.value = bitmap
        }
    }

    private fun prepareBitmapFromPdf(file: File, width: Double): Bitmap {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
            PdfRenderer(fd).use { pdfRenderer ->
                pdfRenderer.openPage(0).use { page ->
                    return Bitmap.createBitmap(width.toInt(), (width * 1.2941).toInt(), Bitmap.Config.ARGB_8888).apply {
                        page.render(this, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }
                }
            }
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            var res: Boolean
            withContext(Dispatchers.IO) {
                res = file.delete() || !file.exists()
            }
            _event.value = Event(ViewFileEvent.OnFileDeleted(res))
        }
    }

    sealed class ViewFileEvent {
        data class OnFileDeleted(val isDeleted: Boolean) : ViewFileEvent()
    }

    override fun onCleared() {
        _image.value?.recycle()
    }
}