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
import java.security.PrivateKey
import java.security.Signature

class TicketingDgcSigner {
    /**
     * sign dcc.
     * @param data data
     * @param privateKey privateKey
     * @return signature as base64
     */
    fun signDcc(data: ByteArray, privateKey: PrivateKey): String {
        val signature: Signature = Signature.getInstance(SIG_ALG)
        signature.initSign(privateKey)
        signature.update(data)
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    companion object {
        const val SIG_ALG = "SHA256withECDSA"
    }
}