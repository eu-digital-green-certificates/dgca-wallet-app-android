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
 *  Created by osarapulov on 5/10/21 1:57 PM
 */

package dgca.wallet.app.android.certificate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.CertificateCardViewBinding
import dgca.wallet.app.android.formatWith

class CertificateCardsAdapter(
    private val certificateCards: List<CertificateCard>,
    private val certificateCardClickListener: CertificateCardClickListener
) :
    RecyclerView.Adapter<CertificateCardsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = CertificateCardViewBinding.bind(itemView)

        companion object {
            const val YEAR_MONTH_DAY = "yyyy-MM-dd"
        }

        fun bind(certificateCard: CertificateCard, certificateCardClickListener: CertificateCardClickListener) {
            binding.titleView.text = when {
                certificateCard.certificate.vaccinations?.first() != null -> binding.root.resources.getString(
                    R.string.vaccination,
                    certificateCard.certificate.vaccinations.first().doseNumber.toString(),
                    certificateCard.certificate.vaccinations.first().totalSeriesOfDoses.toString()
                )
                certificateCard.certificate.recoveryStatements?.isNotEmpty() == true -> binding.root.resources.getString(R.string.recovery)
                certificateCard.certificate.tests?.isNotEmpty() == true -> binding.root.resources.getString(R.string.test)
                else -> ""
            }
            binding.nameView.text = certificateCard.certificate.getFullName()
            binding.scannedAtDateView.text = certificateCard.dateTaken.formatWith(YEAR_MONTH_DAY)
            binding.root.setOnClickListener { certificateCardClickListener.onCertificateCardClick(certificateCard.certificateId) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.certificate_card_view, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(certificateCards[position], certificateCardClickListener)
    }

    override fun getItemCount(): Int {
        return certificateCards.size
    }

    interface CertificateCardClickListener {
        fun onCertificateCardClick(certificateId: Int)
    }
}