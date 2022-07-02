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
 *  Created by osarapulov on 4/30/21 5:01 PM
 */

package dgca.wallet.app.android.dcc.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dgca.wallet.app.android.dcc.data.ConfigRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import timber.log.Timber

@HiltWorker
class CountriesLoadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workParams: WorkerParameters,
    private val configRepository: ConfigRepository,
    private val countriesRepository: CountriesRepository
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {
        Timber.d("countries loading start")
        return try {
            val config = configRepository.local().getConfig()
            val versionName = "1.0.0" // TODO: update BuildConfig.VERSION_NAME
            countriesRepository.preLoadCountries(
                config.getCountriesUrl(versionName)
            )
            Timber.d("countries loading succeeded")
            Result.success()
        } catch (error: Throwable) {
            Timber.d("countries loading retry")
            Result.retry()
        }
    }
}