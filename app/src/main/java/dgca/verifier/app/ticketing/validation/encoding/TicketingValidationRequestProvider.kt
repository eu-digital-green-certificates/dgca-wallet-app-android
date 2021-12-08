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

package dgca.verifier.app.ticketing.validation.encoding

import android.util.Base64
import dgca.verifier.app.ticketing.data.validate.TicketingValidateRequest
import java.security.PrivateKey
import java.security.PublicKey

class TicketingValidationRequestProvider(
    private val ticketingDgcCryptor: TicketingDgcCryptor,
    private val ticketingDgcSigner: TicketingDgcSigner,
) {
    fun provideTicketValidationRequest(
        dgcQrString: String,
        kid: String, publicKey: PublicKey,
        base64EncodedIv: String, privateKey: PrivateKey
    ): TicketingValidateRequest {
        val iv = Base64.decode(base64EncodedIv, Base64.NO_WRAP)
        val ticketingEncryptedDgcData: TicketingEncryptedDgcData = ticketingDgcCryptor.encodeDcc(dgcQrString, iv, publicKey)
        val dcc = Base64.encodeToString(ticketingEncryptedDgcData.dataEncrypted, Base64.NO_WRAP)
        val encKey = Base64.encodeToString(ticketingEncryptedDgcData.encKey, Base64.NO_WRAP)
        val sig = ticketingDgcSigner.signDcc(ticketingEncryptedDgcData.dataEncrypted, privateKey)
        return TicketingValidateRequest(
            kid = kid,
            dcc = dcc,
            sig = sig,
            encKey = encKey,
        )
    }
}