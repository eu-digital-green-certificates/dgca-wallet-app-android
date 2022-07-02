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

package feature.ticketing.data.remote.identity

import feature.ticketing.domain.identity.TicketingIdentityDocumentFetcher
import feature.ticketing.domain.data.identity.TicketingIdentityDocumentResponse
import feature.ticketing.data.remote.TicketingApiService

class DefaultTicketingIdentityDocumentFetcher(private val ticketingApiService: TicketingApiService) :
    TicketingIdentityDocumentFetcher {
    override suspend fun fetchIdentityDocument(url: String): TicketingIdentityDocumentResponse =
        ticketingApiService.getIdentity(url).body()!!
}