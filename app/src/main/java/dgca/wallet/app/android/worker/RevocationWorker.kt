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

package dgca.wallet.app.android.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.revocation.UpdateCertificatesRevocationDataUseCase
import timber.log.Timber

@HiltWorker
class RevocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workParams: WorkerParameters,
    private val updateCertificatesRevocationDataUseCase: UpdateCertificatesRevocationDataUseCase
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {
        Timber.d("Revocation list loading start")
        return try {
            updateCertificatesRevocationDataUseCase.run()
            Timber.d("Revocation loading succeeded")
            Result.success()
        } catch (error: Throwable) {
            Timber.d("Revocation loading retry")
            Result.retry()
        }
    }
}
