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

package dgca.verifier.app.ticketing.accesstoken

import com.fasterxml.jackson.annotation.JsonProperty
import dgca.verifier.app.ticketing.data.identity.TicketingPublicKeyJwkRemote
import dgca.verifier.app.ticketing.data.identity.TicketingVerificationMethodRemote

data class TicketingValidationServiceIdentityResponse(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("verificationMethod")
    val ticketingVerificationMethodsRemote: Set<TicketingVerificationMethodRemote>,
) {
}