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
 *  Created by osarapulov on 8/17/21 8:11 AM
 */

package dgca.wallet.app.android

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun Bitmap.toFile(parentFile: File, relativePath: String): File {
    val targetFile = File(parentFile, relativePath)
    val absoluteFileParent = targetFile.parentFile
    if (absoluteFileParent == null || ((!absoluteFileParent.exists() || !absoluteFileParent.isDirectory) && !absoluteFileParent.mkdirs())) {
        throw IOException("Unable to create file for the Bitmap")
    }
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    FileOutputStream(targetFile).use {
        it.write(bytes.toByteArray())
    }
    return targetFile
}

fun Bitmap.toPdfDocument(): PdfDocument {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(this.width, this.height, 1).create()
    val page = document.startPage(pageInfo)

    page.canvas.drawPaint(Paint().apply { color = Color.WHITE })
    page.canvas.drawBitmap(this, 0.toFloat(), 0.toFloat(), null)

    document.finishPage(page)
    return document
}

fun PdfDocument.toFile(parentFile: File, relativePath: String): File {
    val targetFile = File(parentFile, relativePath)
    val absoluteFileParent = targetFile.parentFile
    if (absoluteFileParent == null || ((!absoluteFileParent.exists() || !absoluteFileParent.isDirectory) && !absoluteFileParent.mkdirs())) {
        throw IOException("Unable to create file for the Bitmap")
    }
    FileOutputStream(targetFile).use {
        this.writeTo(it)
    }
    return targetFile
}