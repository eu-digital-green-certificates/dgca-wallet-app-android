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
 *  Created by osarapulov on 9/22/21 2:35 PM
 */

package feature.ticketing.presentation.model

import android.os.Parcelable
import feature.ticketing.domain.identity.validityserviceidentity.TicketingValidationServiceIdentityResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class TicketingValidationServiceIdentityParcelable(
    val id: String,
    val verificationMethods: Set<TicketingVerificationMethodParcelable>,
) : Parcelable {
    fun getEncryptionPublicKey(): TicketingPublicKeyJwkParcelable? {
        var publicKeyJwk: TicketingPublicKeyJwkParcelable? = null
        verificationMethods.forEach { verificationMethodRemote ->
            if (verificationMethodRemote.publicKeyJwk?.use == "enc") {
                publicKeyJwk = verificationMethodRemote.publicKeyJwk
                return@forEach
            }

        }
        return publicKeyJwk
    }
}

fun TicketingValidationServiceIdentityResponse.fromRemote(): TicketingValidationServiceIdentityParcelable =
    TicketingValidationServiceIdentityParcelable(
        id = id,
        verificationMethods = verificationMethods.map { it.fromRemote() }.toSet(),
    )

fun TicketingValidationServiceIdentityParcelable.toRemote(): TicketingValidationServiceIdentityResponse =
    TicketingValidationServiceIdentityResponse(
        id = id,
        verificationMethods = verificationMethods.map { it.toRemote() }.toSet(),
    )