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
 *  Created by osarapulov on 10/11/21 6:03 PM
 */

package dgca.verifier.app.ticketing.identity

import dgca.verifier.app.ticketing.data.checkin.TicketingCheckInRemote
import dgca.verifier.app.ticketing.data.identity.TicketingIdentityDocumentRemote
import dgca.verifier.app.ticketing.data.identity.TicketingServiceRemote
import dgca.verifier.app.ticketing.data.identity.ServiceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTicketingIdentityDocumentUseCase(private val ticketingIdentityDocumentFetcher: TicketingIdentityDocumentFetcher) {
    suspend fun run(ticketingCheckInRemoteModel: TicketingCheckInRemote): TicketingIdentityDocumentRemote? =
        withContext(Dispatchers.IO) {
            ticketingIdentityDocumentFetcher.fetchIdentityDocument(ticketingCheckInRemoteModel.serviceIdentity)
                .let { identityResponse ->
                    var ticketingAccessTokenService: TicketingServiceRemote? = null
                    val ticketingValidationServices = mutableListOf<TicketingServiceRemote>()
                    identityResponse.servicesRemoteTicketing.forEach { serviceRemote ->
                        when (serviceRemote.type) {
                            ServiceType.ACCESS_TOKEN_SERVICE.type -> {
                                if (ticketingAccessTokenService != null) {
                                    throw IllegalArgumentException("Only one access token service should be available")
                                } else {
                                    ticketingAccessTokenService = serviceRemote
                                }
                            }
                            ServiceType.VALIDATION_SERVICE.type -> ticketingValidationServices.add(serviceRemote)
                            else -> {
                            }
                        }
                    }
                    if (ticketingAccessTokenService != null && ticketingValidationServices.isNotEmpty()) TicketingIdentityDocumentRemote(
                        ticketingAccessTokenService!!,
                        ticketingValidationServices
                    ) else null
                }
        }
}