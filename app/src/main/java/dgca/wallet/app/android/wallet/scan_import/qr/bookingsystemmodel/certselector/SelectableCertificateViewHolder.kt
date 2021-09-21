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
 *  Created by mykhailo.nester on 14/09/2021, 20:46
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.certselector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.ItemSelectableCertificateBinding

class SelectableCertificateViewHolder(
    private val binding: ItemSelectableCertificateBinding,
    private val viewModel: CertificateSelectorViewModel
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup, model: CertificateSelectorViewModel) =
            SelectableCertificateViewHolder(ItemSelectableCertificateBinding.inflate(inflater, parent, false), model)
    }

    init {
        itemView.setOnClickListener {
            viewModel.onCertificateSelected(bindingAdapterPosition)
        }
    }

    fun bind(model: SelectableCertificateModel) {
        val certificateCard = model.certificateCard
        var validUntil: String? = null
        binding.title.text = when {
            certificateCard.certificate.vaccinations?.first() != null -> {
                binding.root.resources.getString(
                    R.string.vaccination,
                    certificateCard.certificate.vaccinations.first().doseNumber.toString(),
                    certificateCard.certificate.vaccinations.first().totalSeriesOfDoses.toString()
                )
            }
            certificateCard.certificate.recoveryStatements?.isNotEmpty() == true -> {
                validUntil = certificateCard.certificate.recoveryStatements.first().certificateValidUntil
                binding.root.resources.getString(
                    R.string.recovery
                )
            }
            certificateCard.certificate.tests?.isNotEmpty() == true -> {
                binding.root.resources.getString(R.string.test)
            }
            else -> ""
        }
        binding.description.text =
            if (validUntil.isNullOrBlank()) binding.root.resources.getString(R.string.no_expiration_date) else binding.root.resources.getString(
                R.string.valid_until,
                validUntil
            )
        binding.radioButton.isChecked = model.selected
    }
}