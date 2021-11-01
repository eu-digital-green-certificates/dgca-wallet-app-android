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

import android.graphics.Bitmap
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer

class DefaultQrCodeFetcher : QrCodeFetcher {

    override fun fetchQrCodeString(sourceBitmap: Bitmap): String {
        val intArray = IntArray(sourceBitmap.width * sourceBitmap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        sourceBitmap.getPixels(intArray, 0, sourceBitmap.width, 0, 0, sourceBitmap.width, sourceBitmap.height)
        val source: LuminanceSource = RGBLuminanceSource(sourceBitmap.width, sourceBitmap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()
        val result: Result = reader.decode(bitmap)

        return result.text
    }
}