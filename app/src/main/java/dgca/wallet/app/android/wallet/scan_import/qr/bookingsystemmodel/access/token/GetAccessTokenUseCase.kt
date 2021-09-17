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

import dgca.wallet.app.android.data.remote.ticketing.TicketingApiService
import dgca.wallet.app.android.data.remote.ticketing.ServiceType
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.AccessTokenService
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.IdentityDocument
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.ValidationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetAccessTokenUseCase(private val ticketingApiService: TicketingApiService) {
    suspend fun run(identityUrl: String): IdentityDocument? = withContext(Dispatchers.IO) {
        ticketingApiService.getIdentity(identityUrl).body()?.let { identityResponse ->
            var accessTokenService: AccessTokenService? = null
            val validationServices = mutableSetOf<ValidationService>()
            identityResponse.verificationMethods.forEach { verificationMethod ->
                when (verificationMethod.type) {
                    ServiceType.ACCESS_TOKEN_SERVICE.type -> {
                        if (accessTokenService != null) {
                            throw IllegalArgumentException("Only one access token service should be available")
                        } else {
                            accessTokenService = AccessTokenService()
                        }
                    }
                    ServiceType.VALIDATION_SERVICE.type -> validationServices.add(ValidationService())
                    else -> {
                        Timber.d("Found validation service of type: '${verificationMethod.type}'")
                    }
                }
            }
            if (accessTokenService != null && validationServices.isNotEmpty()) IdentityDocument(
                accessTokenService!!,
                validationServices
            ) else null
        }.let {
            IdentityDocument(AccessTokenService(), setOf(ValidationService()))
        }
    }
}