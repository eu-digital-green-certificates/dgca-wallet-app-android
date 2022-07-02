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
 *  Created by osarapulov on 8/25/21 4:40 PM
 */

package dgca.wallet.app.android.ui.model

import com.android.app.base.BaseItem
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FileCard(val file: File, val dateTaken: LocalDate) : BaseItem {

    constructor(file: File) : this(
        file,
        Instant.ofEpochMilli(file.name.split('.').first().toLong()).atZone(ZoneId.systemDefault()).toLocalDate()
    )
}

sealed class HeaderItem : BaseItem {
    object ImagesHeader : HeaderItem()
    object PdfsHeader : HeaderItem()
    object CertificatesHeader : HeaderItem()
}
