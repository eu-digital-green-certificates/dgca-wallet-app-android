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

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.identity

import dgca.wallet.app.android.data.remote.ticketing.identity.ServiceTypeRemote
import dgca.wallet.app.android.data.remote.ticketing.identity.TicketingApiService
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.IdentityDocument
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.Service
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.toService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetIdentityDocumentUseCase(private val ticketingApiService: TicketingApiService) {
    suspend fun run(bookingSystemModel: BookingSystemModel): IdentityDocument? = withContext(Dispatchers.IO) {
        ticketingApiService.getIdentity(bookingSystemModel.serviceIdentity).body()?.let { identityResponse ->
            var accessTokenService: Service? = null
            val validationServices = mutableSetOf<Service>()
            identityResponse.servicesRemote.forEach { serviceRemote ->
                when (serviceRemote.type) {
                    ServiceTypeRemote.ACCESS_TOKEN_SERVICERemote.type -> {
                        if (accessTokenService != null) {
                            throw IllegalArgumentException("Only one access token service should be available")
                        } else {
                            accessTokenService = serviceRemote.toService()
                        }
                    }
                    ServiceTypeRemote.VALIDATION_SERVICERemote.type -> validationServices.add(serviceRemote.toService())
                    else -> {
                        Timber.d("Found service of type: '${serviceRemote.type}'")
                    }
                }
            }
            if (accessTokenService != null && validationServices.isNotEmpty()) IdentityDocument(
                accessTokenService!!,
                validationServices
            ) else null
        }
    }
}