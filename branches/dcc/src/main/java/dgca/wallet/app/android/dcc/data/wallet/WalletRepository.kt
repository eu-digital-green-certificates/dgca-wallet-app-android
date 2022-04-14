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
 *  Created by mykhailo.nester on 5/7/21 5:23 PM
 */

package dgca.wallet.app.android.dcc.data.wallet

import dgca.wallet.app.android.dcc.data.CertificatesCard
import dgca.wallet.app.android.dcc.data.remote.ApiResult
import dgca.wallet.app.android.dcc.data.remote.claim.ClaimRequest
import dgca.wallet.app.android.dcc.data.remote.claim.ClaimResponse

interface WalletRepository {

    suspend fun claimCertificate(url: String, qrCode: String, request: ClaimRequest, timeStamp: Long): ApiResult<ClaimResponse>

    suspend fun getCertificates(): List<CertificatesCard.CertificateCard>?

    suspend fun getCertificatesById(certificateId: Int): CertificatesCard.CertificateCard?

    suspend fun deleteCertificateById(certificateId: Int): Boolean

    suspend fun setCertificatesRevokedBy(ids: Collection<Int>, isRevoked: Boolean)
}
