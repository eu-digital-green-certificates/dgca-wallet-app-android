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
 *  Created by osarapulov on 9/10/21 12:58 PM
 */

package dgca.wallet.app.android.dcc.data

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.android.app.base.ProcessorItemCard
import dgca.wallet.app.android.dcc.DccProcessor
import com.android.app.dcc.R
import dgca.wallet.app.android.dcc.data.local.model.CertificateEntity
import dgca.wallet.app.android.dcc.model.CertificateModel
import dgca.wallet.app.android.dcc.ui.wallet.certificates.view.CERTIFICATE_ID_PARAM_KEY
import dgca.wallet.app.android.dcc.utils.YEAR_MONTH_DAY
import dgca.wallet.app.android.dcc.utils.formatWith
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

sealed class CertificatesCard {

    @Parcelize
    data class CertificateCard(
        val certificateId: Int,
        val qrCodeText: String,
        val certificate: CertificateModel,
        val tan: String,
        val dateTaken: ZonedDateTime,
        override val isRevoked: Boolean,
    ) : CertificatesCard(), ProcessorItemCard {

        constructor(certificateEntity: CertificateEntity, certificateModel: CertificateModel) : this(
            certificateEntity.id,
            certificateEntity.qrCodeText,
            certificateModel,
            certificateEntity.tan,
            certificateEntity.dateAdded,
            certificateEntity.isRevoked
        )

        override fun itemId(): Int {
            return certificateId
        }

        override fun processorId(): String {
            return DccProcessor.DCC_PROCESSOR_ID
        }

        override fun title(res: Resources): String {
            return when {
                certificate.vaccinations?.first() != null -> res.getString(
                    R.string.vaccination_of,
                    certificate.vaccinations.first().doseNumber.toString(),
                    certificate.vaccinations.first().totalSeriesOfDoses.toString()
                )
                certificate.recoveryStatements?.isNotEmpty() == true -> res.getString(
                    R.string.recovery
                )
                certificate.tests?.isNotEmpty() == true -> res.getString(R.string.test)
                else -> ""
            }
        }

        override fun typeTitle(res: Resources): String {
            return when {
                certificate.vaccinations?.first() != null -> res.getString(R.string.vaccination)
                certificate.recoveryStatements?.isNotEmpty() == true -> res.getString(R.string.recovery)
                certificate.tests?.isNotEmpty() == true -> res.getString(R.string.test)
                else -> ""
            }
        }

        override fun typeValue(res: Resources): String {
            return if (certificate.vaccinations?.first() != null) {
                "${certificate.vaccinations.first().doseNumber}/${certificate.vaccinations.first().totalSeriesOfDoses}"
            } else {
                ""
            }
        }

        override fun subTitle(res: Resources): String {
            return certificate.getFullName()
        }

        override fun dateString(res: Resources): String {
            return getCertificateDate()
        }

        override fun actionIntent(): Intent {
            return Intent("com.android.app.dcc.View", Uri.parse("view-certificate://dcc")).putExtra(
                CERTIFICATE_ID_PARAM_KEY, certificateId
            )
        }
    }
}

fun CertificatesCard.CertificateCard.getCertificateDate(): String {
    return when {
        certificate.vaccinations?.first() != null ->
            certificate.vaccinations.first().dateOfVaccination
        certificate.recoveryStatements?.first() != null ->
            certificate.recoveryStatements.first().certificateValidFrom
        certificate.tests?.first() != null ->
            certificate.tests.first().dateTimeOfCollection.split("T").first()
        else -> dateTaken.formatWith(YEAR_MONTH_DAY)
    }
}