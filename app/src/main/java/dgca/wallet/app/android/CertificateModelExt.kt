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
 *  Created by osarapulov on 9/21/21 6:45 PM
 */

package dgca.wallet.app.android

import android.content.res.Resources
import dgca.wallet.app.android.data.CertificateModel

fun CertificateModel.getTitle(res: Resources): String = when {
    vaccinations?.first() != null -> res.getString(
        R.string.vaccination,
        vaccinations.first().doseNumber.toString(),
        vaccinations.first().totalSeriesOfDoses.toString()
    )
    recoveryStatements?.isNotEmpty() == true -> res.getString(
        R.string.recovery
    )
    tests?.isNotEmpty() == true -> res.getString(R.string.test)
    else -> ""
}

fun CertificateModel.getMessage(res: Resources): String = when {
    vaccinations?.first() != null -> res.getString(
        R.string.vaccination_certificate,
        vaccinations.first().doseNumber.toString(),
        vaccinations.first().totalSeriesOfDoses.toString()
    )
    recoveryStatements?.isNotEmpty() == true -> res.getString(
        R.string.recovery_certificate
    )
    tests?.isNotEmpty() == true -> res.getString(R.string.test_certificate)
    else -> ""
}