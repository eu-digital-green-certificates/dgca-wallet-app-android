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
 *  Created by osarapulov on 10/11/21 7:17 PM
 */

package dgca.verifier.app.ticketing.accesstoken

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenRequest
import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenResponse
import dgca.wallet.app.android.data.remote.ticketing.accesstoken.fromRemote
import dgca.wallet.app.android.model.AccessTokenResponseContainer
import dgca.wallet.app.android.model.TicketingCheckInParcelable
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.JwtTokenParser
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.TicketingServiceParcelable
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
        ticketingCheckInParcelable: TicketingCheckInParcelable,
        accessTokenTicketingServiceParcelable: TicketingServiceParcelable,
        validationTicketingServiceParcelable: TicketingServiceParcelable
    ): AccessTokenResponseContainer =
        withContext(Dispatchers.IO) {
            val accessTokenRequest =
                AccessTokenRequest(
                    validationTicketingServiceParcelable.id,
                    Base64.getEncoder().encodeToString(keyPair.public.encoded)
                )
            val ticketingAccessTokenData: TicketingAccessTokenData = accessTokenFetcher.fetchAccessToken(
                accessTokenTicketingServiceParcelable.serviceEndpoint,
                "Bearer ${ticketingCheckInParcelable.token}",
                accessTokenRequest
            )
            val accessTokenResponse: AccessTokenResponse =
                jwtTokenParser.parse(ticketingAccessTokenData.jwtToken).let { objectMapper.readValue(it.body) }
            return@withContext AccessTokenResponseContainer(accessTokenResponse.fromRemote(), ticketingAccessTokenData)
        }
}