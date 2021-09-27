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
 *  Created by osarapulov on 9/16/21 2:43 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.access.token

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dgca.wallet.app.android.data.remote.ticketing.TicketingApiService
import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenRequest
import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenResponse
import dgca.wallet.app.android.model.AccessTokenResponseContainer
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.JwtTokenParser
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.security.KeyPair
import java.util.*

class GetAccessTokenUseCase(
    private val ticketingApiService: TicketingApiService,
    private val objectMapper: ObjectMapper,
    private val jwtTokenParser: JwtTokenParser
) {
    suspend fun run(
        keyPair: KeyPair,
        bookingSystemModel: BookingSystemModel,
        accessTokenService: Service,
        validationService: Service
    ): AccessTokenResponseContainer? =
        withContext(Dispatchers.IO) {
            val accessTokenRequest =
                AccessTokenRequest(validationService.id, Base64.getEncoder().encodeToString(keyPair.public.encoded))
            val response = ticketingApiService.getAccessToken(
                accessTokenService.serviceEndpoint,
                "Bearer ${bookingSystemModel.token}",
                accessTokenRequest
            )
            if (response.isSuccessful && response.code() == HttpURLConnection.HTTP_OK) {
                val iv: String = response.headers().get("x-nonce")!!
                val jwtToken: String = response.body()!!.string()
                val accessTokenResponse: AccessTokenResponse =
                    jwtTokenParser.parse(jwtToken).let { objectMapper.readValue(it.body) }
                return@withContext AccessTokenResponseContainer(accessTokenResponse, iv, jwtToken)
            } else {
                null
            }
        }
}