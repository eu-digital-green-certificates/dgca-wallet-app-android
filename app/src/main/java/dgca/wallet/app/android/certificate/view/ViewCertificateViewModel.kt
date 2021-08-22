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
 *  Created by osarapulov on 5/10/21 11:48 PM
 */

package dgca.wallet.app.android.certificate.view

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.Event
import dgca.wallet.app.android.certificate.CertificatesCard
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.qr.QrCodeConverter
import dgca.wallet.app.android.toFile
import dgca.wallet.app.android.toPdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

data class CertificateViewCard(val certificatesCard: CertificatesCard.CertificateCard, val qrCode: Bitmap)

sealed class FilePreparationResult {
    class FileResult(val file: File) : FilePreparationResult()
    class ErrorResult(val error: Exception?) : FilePreparationResult()
}

@HiltViewModel
class ViewCertificateViewModel @Inject constructor(
    private val qrCodeConverter: QrCodeConverter,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _certificate = MutableLiveData<CertificateViewCard>()
    val certificate: LiveData<CertificateViewCard> = _certificate

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    private val _event = MutableLiveData<Event<ViewCertEvent>>()
    val event: LiveData<Event<ViewCertEvent>> = _event

    private val _shareImageFile = MutableLiveData<Event<FilePreparationResult>>()
    val shareImageFile: LiveData<Event<FilePreparationResult>> = _shareImageFile

    private val _sharePdfFile = MutableLiveData<Event<FilePreparationResult>>()
    val sharePdfFile: LiveData<Event<FilePreparationResult>> = _sharePdfFile

    fun setCertificateId(certificateId: Int, qrCodeSize: Int) {
        viewModelScope.launch {
            _inProgress.value = true
            var qrCode: Bitmap

            val certificateCard = walletRepository.getCertificatesById(certificateId)
            if (certificateCard == null) {
                _inProgress.value = false
                return@launch
            }

            withContext(Dispatchers.IO) {
                qrCode = qrCodeConverter.convertStringIntoQrCode(certificateCard.qrCodeText, qrCodeSize)
            }
            _certificate.value = CertificateViewCard(certificateCard, qrCode)
            _inProgress.value = false
        }
    }

    fun deleteCert(certificateId: Int) {
        viewModelScope.launch {
            val result = walletRepository.deleteCertificateById(certificateId)
            _event.value = Event(ViewCertEvent.OnCertDeleted(result))
        }
    }

    fun shareImage(parentFile: File) {
        viewModelScope.launch {
            _inProgress.value = true
            var fileForSharing: File? = null
            var error: Exception? = null
            withContext(Dispatchers.IO) {
                try {
                    fileForSharing = certificate.value!!.qrCode.toFile(
                        parentFile,
                        "images/${File.separator}image_for_sharing.jpg"
                    )
                } catch (exception: Exception) {
                    error = exception
                    Timber.e(exception, "Was not able to prepare image for sharing")
                }
            }
            _inProgress.value = false
            _shareImageFile.postValue(
                Event(
                    if (fileForSharing != null) {
                        FilePreparationResult.FileResult(fileForSharing!!)
                    } else {
                        FilePreparationResult.ErrorResult(error)
                    }
                )
            )
        }
    }

    fun sharePdf(parentFile: File) {
        viewModelScope.launch {
            _inProgress.value = true
            var fileForSharing: File? = null
            var error: Exception? = null
            withContext(Dispatchers.IO) {
                try {
                    fileForSharing = certificate.value!!.qrCode.toPdfDocument().toFile(
                        parentFile,
                        "images/${File.separator}pdf_for_sharing.pdf"
                    )
                } catch (exception: Exception) {
                    error = exception
                    Timber.e(exception, "Was not able to prepare pdf for sharing")
                }
            }
            _inProgress.value = false
            _sharePdfFile.postValue(
                Event(
                    if (fileForSharing != null) {
                        FilePreparationResult.FileResult(fileForSharing!!)
                    } else {
                        FilePreparationResult.ErrorResult(error)
                    }
                )
            )
        }
    }

    sealed class ViewCertEvent {
        data class OnCertDeleted(val isDeleted: Boolean) : ViewCertEvent()
    }
}