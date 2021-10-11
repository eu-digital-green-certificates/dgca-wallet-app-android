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

package dgca.verifier.app.ticketing.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dgca.verifier.app.decoder.base64ToX509Certificate
import dgca.verifier.app.ticketing.JwtObject
import dgca.verifier.app.ticketing.JwtTokenParser
import dgca.verifier.app.ticketing.data.identity.TicketingPublicKeyJwkRemote
import dgca.verifier.app.ticketing.identity.accesstoken.TicketingAccessTokenResponseContainer
import dgca.verifier.app.ticketing.identity.validityserviceidentity.TicketingValidationServiceIdentityResponse
import dgca.verifier.app.ticketing.validation.encoding.TicketingValidationRequestProvider
import dgca.wallet.app.android.data.remote.ticketing.validate.BookingPortalValidationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyPair
import java.security.PublicKey

class TicketingValidationUseCase(
    private val ticketingValidationRequestProvider: TicketingValidationRequestProvider,
    private var validationResultFetcher: TicketingValidationResultFetcher,
    private val jwtTokenParser: JwtTokenParser,
    private val objectMapper: ObjectMapper
) {

    suspend fun run(
        qrString: String,
        keyPair: KeyPair,
        accessTokenResponseContainer: TicketingAccessTokenResponseContainer,
        validationServiceIdentityResponse: TicketingValidationServiceIdentityResponse
    ): BookingPortalValidationResponse =
        withContext(Dispatchers.IO) {
            val token = accessTokenResponseContainer.ticketingAccessTokenDataRemote.jwtToken
            val authTokenHeader = "Bearer $token"
            val publicKeyJwk: TicketingPublicKeyJwkRemote =
                validationServiceIdentityResponse.getEncryptionPublicKey()!!
            val publicKey: PublicKey = publicKeyJwk.x5c.base64ToX509Certificate()!!.publicKey
            val validationRequest = ticketingValidationRequestProvider.provideTicketValidationRequest(
                qrString,
                publicKeyJwk.kid,
                publicKey,
                accessTokenResponseContainer.ticketingAccessTokenDataRemote.iv,
                keyPair.private
            )

            val resToken = validationResultFetcher.fetchValidationResult(
                accessTokenResponseContainer.accessToken.validationUrl,
                authTokenHeader,
                validationRequest
            )

            val jwtToken: JwtObject = jwtTokenParser.parse(resToken)
            return@withContext objectMapper.readValue(jwtToken.body)
        }
}
