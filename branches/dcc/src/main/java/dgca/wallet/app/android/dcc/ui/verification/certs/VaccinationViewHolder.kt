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

package dgca.wallet.app.android.dcc.ui.verification.certs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.app.dcc.databinding.ItemVaccinationBinding
import dgca.wallet.app.android.dcc.model.VaccinationModel
import dgca.wallet.app.android.dcc.utils.*

class VaccinationViewHolder(private val binding: ItemVaccinationBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: VaccinationModel) {
        binding.dateValue.text = data.dateOfVaccination.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        binding.diseaseValue.text = data.disease.value
        data.manufacturer.value.bindText(binding.manufacturerTitle, binding.manufacturerValue)
        data.vaccine.value.bindText(binding.vaccineOrProphylaxisTitle, binding.vaccineOrProphylaxisValue)
        data.countryOfVaccination.bindCountryWith(binding.countryTitle, binding.countryValue)
        data.certificateIssuer.bindText(binding.certificateIssuerTitle, binding.certificateIssuerValue)
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            VaccinationViewHolder(ItemVaccinationBinding.inflate(inflater, parent, false))
    }
}
