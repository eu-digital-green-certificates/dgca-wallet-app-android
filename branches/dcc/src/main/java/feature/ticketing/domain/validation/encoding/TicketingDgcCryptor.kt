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
 *  Created by osarapulov on 10/11/21 7:59 PM
 */

package feature.ticketing.domain.validation.encoding

import java.security.PublicKey

data class TicketingEncryptedDgcData(
    val dataEncrypted: ByteArray,
    val encKey: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TicketingEncryptedDgcData

        if (!dataEncrypted.contentEquals(other.dataEncrypted)) return false
        if (!encKey.contentEquals(other.encKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dataEncrypted.contentHashCode()
        result = 31 * result + encKey.contentHashCode()
        return result
    }
}

interface TicketingDgcCryptor {
    fun encodeDcc(dcc: String, iv: ByteArray, publicKey: PublicKey): TicketingEncryptedDgcData
}