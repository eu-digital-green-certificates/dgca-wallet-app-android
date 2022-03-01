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
 *  Created by osarapulov on 10/11/21 7:57 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.ticketing.accesstoken

import android.os.Parcelable
import dgca.verifier.app.ticketing.data.accesstoken.TicketingCertificateDataRemote
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class TicketingCertificateDataParcelable(
    val hash: String?,
    val lang: String,
    val standardizedFamilyName: String,
    val standardizedGivenName: String,
    val dateOfBirth: String?,
    val coa: String,
    val cod: String,
    val roa: String,
    val rod: String,
    val greenCertificateTypes: List<String>,
    val category: List<String>,
    val validationClock: ZonedDateTime,
    val validFrom: ZonedDateTime,
    val validTo: ZonedDateTime
) : Parcelable {
    fun getStandardizedName(): String = "$standardizedGivenName $standardizedFamilyName".trim()
}

fun TicketingCertificateDataRemote.fromRemote(): TicketingCertificateDataParcelable = TicketingCertificateDataParcelable(
    hash = hash,
    lang = lang,
    standardizedFamilyName = standardizedFamilyName,
    standardizedGivenName = standardizedGivenName,
    dateOfBirth = dateOfBirth,
    coa = coa,
    cod = cod,
    roa = roa,
    greenCertificateTypes = greenCertificateTypes,
    rod = rod,
    category = category,
    validationClock = validationClock,
    validFrom = validFrom,
    validTo = validTo
)

fun TicketingCertificateDataParcelable.toRemote(): TicketingCertificateDataRemote = TicketingCertificateDataRemote(
    hash = hash,
    lang = lang,
    standardizedFamilyName = standardizedFamilyName,
    standardizedGivenName = standardizedGivenName,
    dateOfBirth = dateOfBirth,
    coa = coa,
    cod = cod,
    roa = roa,
    greenCertificateTypes = greenCertificateTypes,
    rod = rod,
    category = category,
    validationClock = validationClock,
    validFrom = validFrom,
    validTo = validTo
)