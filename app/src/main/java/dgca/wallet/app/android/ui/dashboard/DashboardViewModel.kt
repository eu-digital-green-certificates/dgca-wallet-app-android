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
 *  Created by osarapulov on 9/10/21 12:58 PM
 */

package dgca.wallet.app.android.ui.dashboard

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.app.base.BaseItem
import com.android.app.base.ProcessorItemCard
import dgca.wallet.app.android.ProtocolHandler
import dgca.wallet.app.android.base.Event
import dgca.wallet.app.android.ui.model.FileCard
import dgca.wallet.app.android.ui.model.HeaderItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val protocolHandler: ProtocolHandler
) : ViewModel() {

    private val _itemCards = MutableLiveData<List<BaseItem>>()
    val itemCards: LiveData<List<BaseItem>> = _itemCards

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    private val _certificateViewEvent = MutableLiveData<Event<CertificateViewEvent>>()
    val certificateViewEvent: LiveData<Event<CertificateViewEvent>> = _certificateViewEvent

    fun refresh(filesDir: File) {
        _inProgress.value = true
        viewModelScope.launch {
            val itemCards = mutableListOf<BaseItem>()
            withContext(Dispatchers.IO) {
                val certificates = protocolHandler.getItemCards()
                if (certificates.isNotEmpty()) {
                    itemCards.add(HeaderItem.CertificatesHeader)
                    itemCards.addAll(certificates)
                }

                val imagesDir = File(filesDir, "images")
                val imageFileCards = mutableListOf<FileCard>()
                val pdfFileCards = mutableListOf<FileCard>()
                imagesDir.listFiles()?.reversed()?.forEach { file ->
                    when (file.extension) {
                        IMAGE_FILES_EXT -> imageFileCards.add(FileCard(file))
                        PDF_FILES_EXT -> pdfFileCards.add(FileCard(file))
                        else -> {
                        }
                    }
                }

                if (imageFileCards.isNotEmpty()) {
                    itemCards.add(HeaderItem.ImagesHeader)
                    itemCards.addAll(imageFileCards)
                }

                if (pdfFileCards.isNotEmpty()) {
                    itemCards.add(HeaderItem.PdfsHeader)
                    itemCards.addAll(pdfFileCards)
                }
            }

            _inProgress.value = false
            _itemCards.value = itemCards.toList()
        }
    }

    fun onCertificateClick(position: Int) {
        val list = itemCards.value
        if (position < 0 || list == null || list.isEmpty()) {
            return
        }

        val data = list[position] as ProcessorItemCard
        _certificateViewEvent.value = Event(CertificateViewEvent.OnCertificateSelected(data.actionIntent()))
    }

    fun onFileCardClick(position: Int) {
        val list = itemCards.value
        if (position < 0 || list == null || list.isEmpty()) {
            return
        }

        val data = list[position] as FileCard
        _certificateViewEvent.value = Event(CertificateViewEvent.OnFileSelected(data.file))
    }

    fun showDeleteFileDialog(position: Int) {
        val list = itemCards.value
        if (position < 0 || list == null || list.isEmpty()) {
            return
        }

        val data = list[position] as FileCard
        _certificateViewEvent.value = Event(
            CertificateViewEvent.OnShowDeleteFile(
                position,
                data.file
            )
        )
    }

    fun showDeleteCertificateDialog(position: Int) {
        val list = itemCards.value
        if (position < 0 || list == null || list.isEmpty()) {
            return
        }

        val data = list[position] as ProcessorItemCard
        _certificateViewEvent.value = Event(
            CertificateViewEvent.OnShowDeleteCertificate(
                position,
                data
            )
        )
    }

    private fun removeItem(position: Int): List<BaseItem> {
        val list = _itemCards.value!!.toMutableList()
        val removedItem = list.removeAt(position)
        if (removedItem is FileCard) {
            if (removedItem.file.name.endsWith(IMAGE_FILES_EXT)) {
                list.removeIf { it is HeaderItem.ImagesHeader }
            } else if (removedItem.file.name.endsWith(PDF_FILES_EXT)) {
                list.removeIf { it is HeaderItem.PdfsHeader }
            }
        } else if (removedItem is ProcessorItemCard && list.none { it is ProcessorItemCard }) {
            list.removeIf { it is HeaderItem.CertificatesHeader }
        }
        return list.toList()
    }

    fun deleteFile(position: Int, file: File) {
        viewModelScope.launch {
            _inProgress.value = true
            withContext(Dispatchers.IO) {
                file.delete() || !file.exists()
                removeItem(position)
            }.let {
                _itemCards.value = it
            }
            _inProgress.value = false
        }
    }

    fun deleteCertificate(position: Int, itemCard: ProcessorItemCard) {
        viewModelScope.launch {
            _inProgress.value = true
            withContext(Dispatchers.IO) {
                protocolHandler.deleteItem(itemCard.itemId(), itemCard.processorId())
                removeItem(position)
            }.let {
                _itemCards.value = it
            }
            _inProgress.value = false
        }
    }

    sealed class CertificateViewEvent {
        data class OnCertificateSelected(val intent: Intent) : CertificateViewEvent()
        data class OnShowDeleteCertificate(val itemPosition: Int, val itemCard: ProcessorItemCard) : CertificateViewEvent()
        data class OnFileSelected(val file: File) : CertificateViewEvent()
        data class OnShowDeleteFile(val position: Int, val file: File) : CertificateViewEvent()
    }

    companion object {
        private const val IMAGE_FILES_EXT = "jpeg"
        private const val PDF_FILES_EXT = "pdf"
    }
}
