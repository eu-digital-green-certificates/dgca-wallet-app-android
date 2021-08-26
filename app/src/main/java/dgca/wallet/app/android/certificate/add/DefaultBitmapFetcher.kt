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
 *  Created by osarapulov on 8/25/21 3:25 PM
 */

package dgca.wallet.app.android.certificate.add

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class DefaultBitmapFetcher(context: Context) : BitmapFetcher {
    private val appContext = context.applicationContext

    override fun loadBitmapByImageUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(appContext.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(appContext.contentResolver, uri)
        }.copy(Bitmap.Config.ARGB_8888, true)
    }

    override fun loadBitmapByPdfUri(uri: Uri): List<Bitmap> =
        appContext.contentResolver.openFileDescriptor(uri, "r")!!.use { fileDescriptor ->
            PdfRenderer(fileDescriptor).use { pdfRenderer ->
                val bitmaps = mutableListOf<Bitmap>()
                for (i in 0 until pdfRenderer.pageCount) {
                    pdfRenderer.openPage(i).use { page ->
                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmaps.add(bitmap)
                    }
                }
                bitmaps.toList()
            }
        }
}