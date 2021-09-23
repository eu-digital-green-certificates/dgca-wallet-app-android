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
 *  Created by osarapulov on 9/22/21 5:19 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission

import dgca.wallet.app.android.data.remote.ticketing.TicketingApiService
import dgca.wallet.app.android.data.remote.ticketing.access.token.ValidateRequest
import dgca.wallet.app.android.model.BookingPortalEncryptionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

class ValidationUseCase(private val ticketingApiService: TicketingApiService) {
    suspend fun run(qrString: String, bookingPortalEncryptionData: BookingPortalEncryptionData) =
        withContext(Dispatchers.IO) {
            val token = "token goes here"
            val authTokenHeader = "Bearer ${token}"
            val encodedDcc = qrString
            val sig = "sig"
            val encKey = "encKey"
            val validationRequest = ValidateRequest(dcc = encodedDcc, sig = sig, encKey = encKey)
            val res = ticketingApiService.validate(
                bookingPortalEncryptionData.accessTokenResponse.validationUrl,
                authTokenHeader,
                validationRequest
            )
            if (res.isSuccessful || res.code() != HttpURLConnection.HTTP_OK) throw IllegalStateException()
        }
}