/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2022 T-Systems International GmbH and all other contributors
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
 *  Created by osarapulov on 2/11/22, 6:17 PM
 */

package dgca.wallet.app.android.util.jwt

import android.util.Base64
import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.decoder.model.KeyPairData
import dgca.verifier.app.decoder.toBase64
import dgca.wallet.app.android.util.base64.Base64Coder
import java.nio.charset.StandardCharsets
import java.security.Signature

class DefaultJwtTokenGenerator(
    private val objectMapper: ObjectMapper,
    private val base64Coder: Base64Coder
) : JwtTokenGenerator {

    override fun generateJwtToken(
        jwtTokenHeader: JwtTokenHeader,
        jwtTokenBody: Any,
        keyPairData: KeyPairData
    ): String {
        val jwtTokenHeaderJson = objectMapper.writeValueAsString(jwtTokenHeader)
        val base64EncodedJwtTokenHeaderJson: String = base64Coder.toBase64(jwtTokenHeaderJson, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val jwtTokenBodyJson: String = objectMapper.writeValueAsString(jwtTokenBody)
        val base64EncodedJwtTokenBodyJson: String = base64Coder.toBase64(jwtTokenBodyJson, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val beginningOfJwtToken = "$base64EncodedJwtTokenHeaderJson.$base64EncodedJwtTokenBodyJson"
        val sign: ByteArray = beginningOfJwtToken.signWith(keyPairData)
        val base64EncodedSignature = base64Coder.toBase64(sign, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        return "$beginningOfJwtToken.$base64EncodedSignature"
    }

    private fun String.signWith(keyPairData: KeyPairData): ByteArray {
        val signature = Signature.getInstance(keyPairData.algo)
        signature.initSign(keyPairData.keyPair.private)
        signature.update(this.toByteArray(StandardCharsets.UTF_8))
        return signature.sign()
    }
}
