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
 *  Created by osarapulov on 9/17/21 7:59 PM
 */

package dgca.wallet.app.android.data.remote.ticketing

import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenRequest
import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenResponse
import dgca.wallet.app.android.data.remote.ticketing.access.token.ValidationServiceIdentityResponse
import dgca.wallet.app.android.data.remote.ticketing.identity.IdentityResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TicketingApiService {
    @GET
    suspend fun getIdentity(@Url url: String): Response<IdentityResponse>

    @Headers(
        "X-Version: 1.0.0",
        "content-type: application/json"
    )
    @POST
    suspend fun getAccessToken(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body body: AccessTokenRequest
    ): Response<ResponseBody>

    @GET
    suspend fun getValidationServiceIdentity(@Url url: String): Response<ValidationServiceIdentityResponse>
}
