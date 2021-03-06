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
 *  Created by osarapulov on 10/11/21 7:14 PM
 */

package feature.ticketing.data.remote.accesstoken

import feature.ticketing.data.remote.TicketingApiService
import feature.ticketing.domain.data.accesstoken.TicketingAccessTokenRequest
import feature.ticketing.domain.identity.accesstoken.TicketingAccessTokenDataRemote
import feature.ticketing.domain.identity.accesstoken.TicketingAccessTokenFetcher

class DefaultTicketingAccessTokenFetcher(private val ticketingApiService: TicketingApiService) : TicketingAccessTokenFetcher {

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fetchAccessToken(
        url: String,
        header: String,
        ticketingAccessTokenRequest: TicketingAccessTokenRequest
    ): TicketingAccessTokenDataRemote {
        val response = ticketingApiService.getAccessToken(url, header, ticketingAccessTokenRequest)
        val jwtToken: String = response.body()!!.string()
        val iv = response.headers()["x-nonce"]!!
        return TicketingAccessTokenDataRemote(jwtToken, iv)
    }
}