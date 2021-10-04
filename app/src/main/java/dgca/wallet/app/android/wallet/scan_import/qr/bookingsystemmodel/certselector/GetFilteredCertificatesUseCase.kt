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
import dgca.wallet.app.android.model.BookingPortalEncryptionData
import dgca.wallet.app.android.toZonedDateTime
import dgca.wallet.app.android.wallet.CertificatesCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.util.*


class GetFilteredCertificatesUseCase(private val walletRepository: WalletRepository) {
    suspend fun run(bookingPortalEncryptionData: BookingPortalEncryptionData): List<CertificatesCard.CertificateCard> =
        withContext(Dispatchers.IO) {
            val filteredCertificates: MutableList<CertificatesCard.CertificateCard> = mutableListOf()
            walletRepository.getCertificates()?.forEach { certificateCard ->
                if (isGreenCertificateTypeApplicable(bookingPortalEncryptionData, certificateCard)
                    && isUserDataApplicable(bookingPortalEncryptionData, certificateCard) && areDatesApplicable(
                        bookingPortalEncryptionData,
                        certificateCard
                    )
                ) {
                    filteredCertificates.add(certificateCard)
                }
            }

            return@withContext filteredCertificates.toList()
        }

    private fun isGreenCertificateTypeApplicable(
        bookingPortalEncryptionData: BookingPortalEncryptionData,
        certificateCard: CertificatesCard.CertificateCard
    ): Boolean {
        if (bookingPortalEncryptionData.accessTokenResponseContainer.accessTokenResponse.certificateData.greenCertificateTypes.contains(
                "v"
            ) && certificateCard.certificate.vaccinations?.isNotEmpty() == true
        ) return true
        if (bookingPortalEncryptionData.accessTokenResponseContainer.accessTokenResponse.certificateData.greenCertificateTypes.contains(
                "r"
            ) && certificateCard.certificate.recoveryStatements?.isNotEmpty() == true
        ) return true
        if (bookingPortalEncryptionData.accessTokenResponseContainer.accessTokenResponse.certificateData.greenCertificateTypes.contains(
                "t"
            ) && certificateCard.certificate.tests?.isNotEmpty() == true
        ) return true
        return false
    }

    private fun isUserDataApplicable(
        bookingPortalEncryptionData: BookingPortalEncryptionData,
        certificateCard: CertificatesCard.CertificateCard
    ): Boolean {
        val ticketingStandardizedGivenName: String =
            bookingPortalEncryptionData.accessTokenResponseContainer.accessTokenResponse.certificateData.standardizedGivenName
        val greenCertificateStandardizedGivenName: String? = certificateCard.certificate.person.standardisedGivenName
        if (ticketingStandardizedGivenName.isNotBlank() && (greenCertificateStandardizedGivenName.isNullOrBlank() || greenCertificateStandardizedGivenName.compareTo(
                ticketingStandardizedGivenName, ignoreCase = true
            ) != 0)
        ) return false

        val ticketingStandardizedFamilyLastName: String =
            bookingPortalEncryptionData.accessTokenResponseContainer.accessTokenResponse.certificateData.standardizedFamilyName
        val greenCertificateStandardizedFamilyName: String? = certificateCard.certificate.person.standardisedFamilyName
        if (ticketingStandardizedFamilyLastName.isNotBlank() &&
            (greenCertificateStandardizedFamilyName.isNullOrBlank() || greenCertificateStandardizedFamilyName.compareTo(
                ticketingStandardizedFamilyLastName, ignoreCase = true
            ) != 0)
        ) return false

        val ticketingDateOfBirth: ZonedDateTime? =
            bookingPortalEncryptionData.accessTokenResponseContainer.accessTokenResponse.certificateData.dateOfBirth?.toZonedDateTime()
        val greenCertificateDateOfBirth: ZonedDateTime? = certificateCard.certificate.dateOfBirth.toZonedDateTime()
        if (ticketingDateOfBirth != null && !ticketingDateOfBirth.equals(greenCertificateDateOfBirth)) return false

        return true
    }

    private fun areDatesApplicable(
        bookingPortalEncryptionData: BookingPortalEncryptionData,
        certificateCard: CertificatesCard.CertificateCard
    ): Boolean {
        val ticketingValidFrom: ZonedDateTime =
            bookingPortalEncryptionData.accessTokenResponseContainer.accessTokenResponse.certificateData.validFrom
        val greenCertificateValidFrom: ZonedDateTime? = certificateCard.certificate.getValidFrom()
        if (greenCertificateValidFrom == null || greenCertificateValidFrom.isAfter(ticketingValidFrom)) return false

        val ticketingValidTo: ZonedDateTime =
            bookingPortalEncryptionData.accessTokenResponseContainer.accessTokenResponse.certificateData.validTo
        val greenCertificateValidTo: ZonedDateTime? = certificateCard.certificate.getValidTo()
        if (greenCertificateValidTo != null && greenCertificateValidTo.isBefore(ticketingValidTo)) return false

        return true
    }
}