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
 *  Created by osarapulov on 9/12/21 3:50 PM
 */

package dgca.wallet.app.android.dcc.ui.wallet.qr.certificate

import android.os.Parcelable
import dgca.wallet.app.android.dcc.model.CertificateModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClaimGreenCertificateModel(
    val qrCodeText: String,
    val dgci: String,
    val cose: ByteArray,
    val certificateModel: CertificateModel
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClaimGreenCertificateModel

        if (qrCodeText != other.qrCodeText) return false
        if (dgci != other.dgci) return false
        if (!cose.contentEquals(other.cose)) return false
        if (certificateModel != other.certificateModel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = qrCodeText.hashCode()
        result = 31 * result + dgci.hashCode()
        result = 31 * result + cose.contentHashCode()
        result = 31 * result + certificateModel.hashCode()
        return result
    }
}
