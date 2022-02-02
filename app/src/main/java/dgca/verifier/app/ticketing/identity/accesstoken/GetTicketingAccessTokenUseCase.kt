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
 *  Created by osarapulov on 10/11/21 8:35 PM
 */

package dgca.verifier.app.ticketing.identity.accesstoken

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dgca.verifier.app.ticketing.JwtTokenParser
import dgca.verifier.app.ticketing.data.accesstoken.TicketingAccessTokenRequest
import dgca.verifier.app.ticketing.data.accesstoken.TicketingAccessTokenResponse
import dgca.verifier.app.ticketing.data.checkin.TicketingCheckInRemote
import dgca.verifier.app.ticketing.data.identity.TicketingServiceRemote
import dgca.wallet.app.android.model.TicketingCheckInParcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyPair
import java.util.*

class GetTicketingAccessTokenUseCase(
    private val accessTokenFetcher: TicketingAccessTokenFetcher,
    private val objectMapper: ObjectMapper,
    private val jwtTokenParser: JwtTokenParser
) {
    suspend fun run(
        keyPair: KeyPair,
        ticketingCheckInRemote: TicketingCheckInRemote,
        accessTokenService: TicketingServiceRemote,
        validationService: TicketingServiceRemote
    ): TicketingAccessTokenResponseContainer =
        withContext(Dispatchers.IO) {
            val accessTokenRequest =
                TicketingAccessTokenRequest(
                    validationService.id,
                    Base64.getEncoder().encodeToString(keyPair.public.encoded)
                )
            val ticketingAccessTokenDataRemote: TicketingAccessTokenDataRemote = accessTokenFetcher.fetchAccessToken(
                accessTokenService.serviceEndpoint,
                "Bearer ${ticketingCheckInRemote.token}",
                accessTokenRequest
            )
            val ticketingAccessTokenResponse: TicketingAccessTokenResponse =
                jwtTokenParser.parse(ticketingAccessTokenDataRemote.jwtToken).let { objectMapper.readValue(it.body) }
            return@withContext TicketingAccessTokenResponseContainer(
                ticketingAccessTokenResponse,
                ticketingAccessTokenDataRemote
            )
        }
}