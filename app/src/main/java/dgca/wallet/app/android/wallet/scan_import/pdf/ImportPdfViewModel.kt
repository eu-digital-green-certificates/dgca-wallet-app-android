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

package dgca.wallet.app.android.wallet.scan_import.pdf

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.ticketing.checkin.TicketingCheckInModelFetcher
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.local.toCertificateModel
import dgca.wallet.app.android.model.TicketingCheckInParcelable
import dgca.wallet.app.android.model.fromRemote
import dgca.wallet.app.android.wallet.scan_import.BitmapFetcher
import dgca.wallet.app.android.wallet.scan_import.FileSaver
import dgca.wallet.app.android.wallet.scan_import.GreenCertificateFetcher
import dgca.wallet.app.android.wallet.scan_import.QrCodeFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

sealed class ImportPdfResult {
    object Failed : ImportPdfResult()
    object Success : ImportPdfResult()
    class GreenCertificateRecognised(
        val qrCodeText: String,
        val dgci: String,
        val cose: ByteArray,
        val certificateModel: CertificateModel
    ) : ImportPdfResult()

    class BookingSystemModelRecognised(
        val ticketingCheckInParcelable: TicketingCheckInParcelable
    ) : ImportPdfResult()
}

@HiltViewModel
class ImportPdfViewModel @Inject constructor(
    private val bitmapFetcher: BitmapFetcher,
    private val qrCodeFetcher: QrCodeFetcher,
    private val fileSaver: FileSaver,
    private val greenCertificateFetcher: GreenCertificateFetcher,
    private val ticketingCheckInModelFetcher: TicketingCheckInModelFetcher
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

        var qrString = ""
        var cose: ByteArray? = null
        var greenCertificate: GreenCertificate? = null

        qrStrings.forEach { curQrString ->
            val greenCertificateData: Pair<ByteArray?, GreenCertificate?> =
                greenCertificateFetcher.fetchDataFromQrString(curQrString)
            if (greenCertificateData.first != null && greenCertificateData.second != null) {
                qrString = curQrString
                cose = greenCertificateData.first
                greenCertificate = greenCertificateData.second
                return@forEach
            }
        }

        return if (qrString.isNotBlank() && greenCertificate != null && cose != null) {
            ImportPdfResult.GreenCertificateRecognised(
                qrString,
                greenCertificate!!.getDgci(),
                cose!!,
                greenCertificate!!.toCertificateModel()
            )
        } else {
            runCatching {
                ticketingCheckInModelFetcher.fetchTicketingCheckInModel(qrString)
                    .let { ImportPdfResult.BookingSystemModelRecognised(it.fromRemote()) }
            }.getOrElse {
                val file = try {
                    fileSaver.saveFileFromUri(this, "images", "${System.currentTimeMillis()}.pdf")
                } catch (exception: Exception) {
                    null
                }
                if (file?.exists() == true && file.isFile) ImportPdfResult.Success else ImportPdfResult.Failed
            }
        }
    }
}