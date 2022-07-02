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
 *  Created by osarapulov on 4/4/22, 12:25 AM
 */

package dgca.wallet.app.android.dcc

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.*
import com.android.app.base.Processor
import com.android.app.base.ProcessorItemCard
import com.android.app.base.RESULT_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.wallet.app.android.dcc.data.wallet.WalletRepository
import dgca.wallet.app.android.dcc.worker.*
import feature.ticketing.domain.checkin.TicketingCheckInModelFetcher
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DccProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val coseService: CoseService,
    private val walletRepository: WalletRepository,
    private val ticketingCheckInModelFetcher: TicketingCheckInModelFetcher
) : Processor {
    override fun id(): String {
        return DCC_PROCESSOR_ID
    }

    override fun prefetchData() {
        Timber.d("Prefetching data...")
        WorkManager.getInstance(context).apply {
            schedulePeriodicWorker<ConfigsLoadingWorker>(WORKER_CONFIGS)
            schedulePeriodicWorker<RulesLoadWorker>(WORKER_RULES)
            schedulePeriodicWorker<CountriesLoadWorker>(WORKER_COUNTRIES)
            schedulePeriodicWorker<ValueSetsLoadWorker>(WORKER_VALUESETS)
            schedulePeriodicWorker<RevocationWorker>(WORKER_REVOCATION)
        }
    }

    override fun isApplicable(input: String): Intent? {
        if (isTicketingCode(input)) {
            return Intent("com.android.app.dcc.View", Uri.parse("verifier://dcc")).apply {
                putExtra(RESULT_KEY, input)
            }
        }

        val verificationResult = VerificationResult()
        val plainInput = prefixValidationService.decode(input, verificationResult)
        val compressedCose = base45Service.decode(plainInput, verificationResult)
        if (verificationResult.base45Decoded.not()) {
            Timber.d("Verification failed: base45 not decoded")
            return null
        }

        val cose: ByteArray? = compressorService.decode(compressedCose, verificationResult)
        if (cose == null) {
            Timber.d("Verification failed: Too many bytes read")
            return null
        }


        val coseData = coseService.decode(cose, verificationResult)
        if (coseData == null) {
            Timber.d("Verification failed: COSE not decoded")
            return null
        }

        val kid = coseData.kid
        if (kid == null) {
            Timber.d("Verification failed: cannot extract kid from COSE")
            return null
        }

        return Intent("com.android.app.dcc.View", Uri.parse("verifier://dcc")).apply {
            putExtra(RESULT_KEY, input)
        }
    }

    override suspend fun getItemCards(): List<ProcessorItemCard>? {
        return walletRepository.getCertificates()
    }

    override fun getSettingsIntent(): Pair<String, Intent> {
        return Pair("Dcc", Intent("com.android.app.dcc.View", Uri.parse("settings://dcc")))
    }

    override suspend fun deleteItem(itemCard: Int) {
        walletRepository.deleteCertificateById(itemCard)
    }

    private fun isTicketingCode(input: String): Boolean {
        return runCatching {
            ticketingCheckInModelFetcher.fetchTicketingCheckInModel(input)
            true
        }.getOrElse {
            Timber.e(it)
            false
        }
    }

    private inline fun <reified T : ListenableWorker> WorkManager.schedulePeriodicWorker(workerId: String) =
        this.enqueueUniquePeriodicWork(
            workerId, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<T>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MAX_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        )

    companion object {
        internal const val DCC_PROCESSOR_ID = "DCC"
        const val WORKER_CONFIGS = "workerConfigs"
        const val WORKER_RULES = "workerRules"
        const val WORKER_COUNTRIES = "workerCountries"
        const val WORKER_VALUESETS = "workerValueSets"
        const val WORKER_REVOCATION = "workerRevocation"
    }
}
