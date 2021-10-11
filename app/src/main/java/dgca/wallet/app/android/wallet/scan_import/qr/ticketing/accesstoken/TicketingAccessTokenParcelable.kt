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
import dgca.verifier.app.ticketing.data.accesstoken.TicketingAccessTokenResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class TicketingAccessTokenParcelable(
    val jti: String?,
    val iss: String,
    val iat: Long,
    val sub: String,
    val validationUrl: String,
    val exp: Long,
    val t: Long,
    val v: String,
    val certificateData: TicketingCertificateDataParcelable
) : Parcelable

fun TicketingAccessTokenResponse.fromRemote(): TicketingAccessTokenParcelable = TicketingAccessTokenParcelable(
    jti = jti,
    iss = iss,
    iat = iat,
    sub = sub,
    validationUrl = validationUrl,
    exp = exp,
    t = t,
    certificateData = certificateData.fromRemote(),
    v = v
)

fun TicketingAccessTokenParcelable.toRemote(): TicketingAccessTokenResponse = TicketingAccessTokenResponse(
    jti = jti,
    iss = iss,
    iat = iat,
    sub = sub,
    validationUrl = validationUrl,
    exp = exp,
    t = t,
    certificateData = certificateData.toRemote(),
    v = v
)