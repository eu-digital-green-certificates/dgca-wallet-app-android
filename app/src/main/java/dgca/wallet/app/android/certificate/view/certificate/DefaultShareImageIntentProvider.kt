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
 *  Created by osarapulov on 8/23/21 1:54 PM
 */

package dgca.wallet.app.android.certificate.view.certificate

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import javax.inject.Inject

class DefaultShareImageIntentProvider @Inject constructor(private val context: Context) : ShareImageIntentProvider {
    override fun getShareImageIntent(file: File): Intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        val uri: Uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
        putExtra(Intent.EXTRA_STREAM, uri)
    }
}