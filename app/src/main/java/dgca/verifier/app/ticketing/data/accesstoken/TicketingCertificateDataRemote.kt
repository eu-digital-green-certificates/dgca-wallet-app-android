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
 *  Created by osarapulov on 9/17/21 8:09 PM
 */

package dgca.verifier.app.ticketing.data.accesstoken

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

data class TicketingCertificateDataRemote(
    @JsonProperty("hash")
    val hash: String?,
    @JsonProperty("lang")
    val lang: String,
    @JsonProperty("fnt")
    val standardizedFamilyName: String,
    @JsonProperty("gnt")
    val standardizedGivenName: String,
    @JsonProperty("dob")
    val dateOfBirth: String?,
    @JsonProperty("coa")
    val coa: String,
    @JsonProperty("cod")
    val cod: String,
    @JsonProperty("roa")
    val roa: String,
    @JsonProperty("rod")
    val rod: String,
    @JsonProperty("type")
    val greenCertificateTypes: List<String>,
    @JsonProperty("category")
    val category: List<String>,
    @JsonProperty("validationClock")
    val validationClock: ZonedDateTime,
    @JsonProperty("validFrom")
    val validFrom: ZonedDateTime,
    @JsonProperty("validTo")
    val validTo: ZonedDateTime
)