/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by mykhailo.nester on 5/11/21 9:36 PM
 */

package dgca.wallet.app.android.data

import dgca.wallet.app.android.data.remote.ApiService
import dgca.wallet.app.android.model.ClaimRequest
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : BaseRepository(), WalletRepository {


    override suspend fun claimCertificate(request: ClaimRequest): Boolean {
        return execute {
            val response = apiService.claimCertificate(request)
            val body = response.body() ?: return@execute false

            return@execute true
        } == true
    }
}