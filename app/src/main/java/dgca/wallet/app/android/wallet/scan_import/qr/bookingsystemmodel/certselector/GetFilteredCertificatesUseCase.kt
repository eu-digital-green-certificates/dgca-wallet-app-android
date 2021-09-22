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
 *  Created by osarapulov on 9/21/21 5:27 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.certselector

import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.model.AccessTokenResult
import dgca.wallet.app.android.wallet.CertificatesCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class GetFilteredCertificatesUseCase(private val walletRepository: WalletRepository) {
    suspend fun run(accessTokenResult: AccessTokenResult): List<CertificatesCard.CertificateCard> =
        withContext(Dispatchers.IO) {
            val filteredCertificates: MutableList<CertificatesCard.CertificateCard> = mutableListOf()
            walletRepository.getCertificates()?.forEach { certificateCard ->
                if (isGreenCertificateTypeApplicable(accessTokenResult, certificateCard)
                    && isUserDataApplicable(accessTokenResult, certificateCard) && areDatesApplicable(
                        accessTokenResult,
                        certificateCard
                    )
                ) {
                    filteredCertificates.add(certificateCard)
                }
            }

            return@withContext filteredCertificates.toList()
        }

    private fun isGreenCertificateTypeApplicable(
        accessTokenResult: AccessTokenResult,
        certificateCard: CertificatesCard.CertificateCard
    ): Boolean {
        if (accessTokenResult.accessTokenResponse.certificateData.greenCertificateTypes.contains("v") && certificateCard.certificate.vaccinations?.isNotEmpty() == true) return true
        if (accessTokenResult.accessTokenResponse.certificateData.greenCertificateTypes.contains("r") && certificateCard.certificate.recoveryStatements?.isNotEmpty() == true) return true
        if (accessTokenResult.accessTokenResponse.certificateData.greenCertificateTypes.contains("t") && certificateCard.certificate.tests?.isNotEmpty() == true) return true
        return false
    }

    private fun isUserDataApplicable(
        accessTokenResult: AccessTokenResult,
        certificateCard: CertificatesCard.CertificateCard
    ): Boolean {
        if (accessTokenResult.accessTokenResponse.certificateData.firstName.isNotBlank() && accessTokenResult.accessTokenResponse.certificateData.firstName != certificateCard.certificate.person.givenName) return false
        if (accessTokenResult.accessTokenResponse.certificateData.lastName.isNotBlank() && accessTokenResult.accessTokenResponse.certificateData.lastName != certificateCard.certificate.person.familyName) return false
        if (accessTokenResult.accessTokenResponse.certificateData.dateOfBirth?.isNotBlank() == true && accessTokenResult.accessTokenResponse.certificateData.dateOfBirth != certificateCard.certificate.dateOfBirth) return false
        return true
    }

    private fun areDatesApplicable(
        accessTokenResult: AccessTokenResult,
        certificateCard: CertificatesCard.CertificateCard
    ): Boolean {
        if (true) return true
        val validFrom = certificateCard.certificate.getValidFrom()
        if (validFrom == null || validFrom.isBefore(accessTokenResult.accessTokenResponse.certificateData.validFrom)) return false
        val validTo = certificateCard.certificate.getValidTo()
        if (validTo == null || validTo.isBefore(accessTokenResult.accessTokenResponse.certificateData.validTo)) return false
        return true
    }
}