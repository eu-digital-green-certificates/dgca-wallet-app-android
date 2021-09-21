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

import dgca.verifier.app.decoder.model.KeyPairData
import dgca.wallet.app.android.data.remote.ticketing.TicketingApiService
import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenRequest
import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenResponse
import dgca.wallet.app.android.model.AccessTokenResult
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.model.PublicKeyData
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.*

class GetAccessTokenUseCase(private val ticketingApiService: TicketingApiService) {
    suspend fun run(
        bookingSystemModel: BookingSystemModel,
        accessTokenService: Service,
        validationService: Service
    ): AccessTokenResult? =
        withContext(Dispatchers.IO) {
            val keyPairGen = KeyPairGenerator.getInstance("EC")
            keyPairGen.initialize(256)
            val keyPairData = KeyPairData("SHA256withECDSA", keyPairGen.generateKeyPair())
            val keyPair: KeyPair = keyPairData.keyPair

            val publicKeyData = PublicKeyData(
                keyPair.public.algorithm,
                Base64.getEncoder().encodeToString(keyPair.public.encoded)
            )

            val accessTokenRequest = AccessTokenRequest(validationService.id, publicKeyData.value)
            val accessTokenResponse: Response<AccessTokenResponse> = ticketingApiService.getAccessToken(
                accessTokenService.serviceEndpoint,
                "Bearer ${bookingSystemModel.token}",
                accessTokenRequest
            )
            val accessTokenResponseBody: AccessTokenResponse? = accessTokenResponse.body()
            accessTokenResponseBody?.let {
                AccessTokenResult(
                    it.vc.firstName,
                    it.vc.lastName,
                    it.vc.dateOfBirth,
                    it.vc.greenCertificateTypes,
                    it.vc.validFrom,
                    it.vc.validTo,
                    keyPair.private
                )
            }
        }
}