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
 *  Created by osarapulov on 9/22/21 5:01 PM
 */

package dgca.wallet.app.android.data.remote.ticketing.access.token

import com.fasterxml.jackson.annotation.JsonProperty

data class ValidateRequest(
    @JsonProperty("kid")
    var kid: String = "9P6CdU/nRyU=",
    @JsonProperty("dcc")
    var dcc: String,
    @JsonProperty("sig")
    var sig: String,
    @JsonProperty("encKey")
    var encKey: String,
    @JsonProperty("encScheme")
    var encScheme: String = "RSAOAEPWithSHA256AES",
    @JsonProperty("sigAlg")
    var sigAlg: String = "SHA256withECDSA"
)