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
 *  Created by osarapulov on 8/25/21 12:17 PM
 */

package dgca.wallet.app.android.certificate.add

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer

class DefaultQrCodeFetcher(context: Context) : QrCodeFetcher {
    private val appContext = context.applicationContext

    override fun fetchQrCodeStringByUri(uri: Uri): String = loadBitmapByUri(appContext, uri).fetchQrCodeString()

    private fun loadBitmapByUri(context: Context, uri: Uri): Bitmap = context.contentResolver.let {
        return@let if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun Bitmap.fetchQrCodeString(): String {
        val intArray = IntArray(width * height)
        //copy pixel data from the Bitmap into the 'intArray' array
        getPixels(intArray, 0, width, 0, 0, width, height)
        val source: LuminanceSource = RGBLuminanceSource(width, height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()
        val result: Result = reader.decode(bitmap)
        return result.text
    }
}