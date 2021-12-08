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
 *  Created by mykhailo.nester on 4/24/21 5:18 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.certificate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.FORMATTED_YEAR_MONTH_DAY
import dgca.wallet.app.android.YEAR_MONTH_DAY
import dgca.wallet.app.android.data.RecoveryModel
import dgca.wallet.app.android.databinding.ItemRecoveryBinding
import dgca.wallet.app.android.parseFromTo

class RecoveryViewHolder(private val binding: ItemRecoveryBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: RecoveryModel) {
        binding.diseaseValue.text = data.disease.value
        val validFrom = data.certificateValidFrom.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        val validTo = data.certificateValidUntil.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        val validFromTo = if (validFrom.isNotBlank() && validTo.isNotBlank()) "$validFrom - $validTo" else ""
        validFromTo.bindText(binding.validFromTitle, binding.validFromValue)
        binding.dateOfPositiveValue.text = data.dateOfFirstPositiveTest.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        data.countryOfVaccination.bindCountryWith(binding.countryTitle, binding.countryValue)
        data.certificateIssuer.bindText(binding.certificateIssuerTitle, binding.certificateIssuerValue)
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            RecoveryViewHolder(ItemRecoveryBinding.inflate(inflater, parent, false))
    }
}
