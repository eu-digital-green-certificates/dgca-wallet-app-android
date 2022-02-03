/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 2/3/22, 5:38 PM
 */

package dgca.wallet.app.android.revocation

import dgca.wallet.app.android.data.WalletRepository
import javax.inject.Inject

class UpdateCertificatesRevocationDataUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend fun run() {
        val notRevokedCertificatesIds = mutableSetOf<Int>()

        walletRepository.getNotRevokedCertificates()
            ?.forEach {
                notRevokedCertificatesIds.add(it.certificateId)
            }

        // TODO implement logic to set certificates revoked.

        walletRepository.setCertificatesRevokedBy(notRevokedCertificatesIds)
    }
}
