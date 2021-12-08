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
 *  Created by osarapulov on 10/11/21 7:56 PM
 */

package dgca.verifier.app.ticketing.data.validate

import com.fasterxml.jackson.annotation.JsonProperty

data class TicketingValidateRequest(
    @JsonProperty("kid")
    val kid: String,
    @JsonProperty("dcc")
    val dcc: String,
    @JsonProperty("sig")
    val sig: String,
    @JsonProperty("encKey")
    val encKey: String,
    @JsonProperty("encScheme")
    val encScheme: String = "RSAOAEPWithSHA256AESGCM",
    @JsonProperty("sigAlg")
    val sigAlg: String = "SHA256withECDSA"
)