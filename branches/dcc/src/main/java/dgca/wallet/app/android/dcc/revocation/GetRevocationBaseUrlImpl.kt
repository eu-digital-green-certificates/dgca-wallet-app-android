/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
 *  ---
 *  Copyright (C) 2022 T-Systems International GmbH and all other contributors
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
 *  Created by osarapulov on 5/27/22, 9:18 AM
 */

package dgca.wallet.app.android.dcc.revocation

import com.android.app.dcc.BuildConfig
import dgca.wallet.app.android.dcc.data.ConfigRepository
import feature.revocation.GetRevocationBaseUrl
import javax.inject.Inject

class GetRevocationBaseUrlImpl @Inject constructor(
    private val configRepository: ConfigRepository
): GetRevocationBaseUrl {
    override suspend fun invoke(): String {
        val config = configRepository.local().getConfig()
        val versionName = BuildConfig.VERSION_NAME
        return config.getRevocationUrl(versionName)
    }
}
