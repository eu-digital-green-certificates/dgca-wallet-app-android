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
 *  Created by osarapulov on 8/25/21 12:36 PM
 */

package dgca.wallet.app.android.certificate.add

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class DefaultFileSaver(context: Context) : FileSaver {
    private val appContext = context.applicationContext

    override fun saveFileFromUri(uri: Uri, targetDirectoryName: String, targetFileName: String): File {
        val directory = File(appContext.filesDir, targetDirectoryName).apply { if (!isDirectory || !exists()) mkdirs() }
        val file = File(directory, targetFileName)

        appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        }

        return file
    }
}