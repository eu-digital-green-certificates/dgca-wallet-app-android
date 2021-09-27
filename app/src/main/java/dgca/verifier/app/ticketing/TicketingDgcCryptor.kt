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
 *  Created by osarapulov on 9/27/21 9:08 AM
 */

package dgca.verifier.app.ticketing

import java.security.PublicKey

data class TicketingEncryptedDgcData(
    val dataEncrypted: ByteArray,
    val encKey: ByteArray
)

interface TicketingDgcCryptor {
    fun encodeDcc(dcc: String, iv: ByteArray, publicKey: PublicKey): TicketingEncryptedDgcData
}