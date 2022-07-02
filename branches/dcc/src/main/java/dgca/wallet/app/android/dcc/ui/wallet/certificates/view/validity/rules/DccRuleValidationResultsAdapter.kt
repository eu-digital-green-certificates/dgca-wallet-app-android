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
 *  Created by osarapulov on 6/18/21 8:57 AM
 */

package dgca.wallet.app.android.dcc.ui.wallet.certificates.view.validity.rules

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.app.dcc.R
import com.android.app.dcc.databinding.ItemRuleValidationResultBinding
import dgca.verifier.app.engine.Result
import java.util.*

class DccRuleValidationResultsAdapter(
    private val inflater: LayoutInflater,
    dccRuleValidationResultCards: Collection<DccRuleValidationResultCard>
) :
    RecyclerView.Adapter<DccRuleValidationResultsAdapter.CardViewHolder>() {

    private val dccRuleValidationResultCards: MutableList<DccRuleValidationResultCard> =
        dccRuleValidationResultCards.toMutableList()

    class CardViewHolder(private val binding: ItemRuleValidationResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private fun Result.getLocalizedText(context: Context): String = context.getString(
            when (this) {
                Result.PASSED -> R.string.passed
                Result.FAIL -> R.string.failed
                Result.OPEN -> R.string.open
            }
        )

        fun bind(dccRuleValidationResultCard: DccRuleValidationResultCard) {
            binding.ruleVerificationResultHeader.text =
                dccRuleValidationResultCard.result.getLocalizedText(
                    itemView.context
                )

            binding.ruleVerificationResultHeader.setTextColor(
                ResourcesCompat.getColor(
                    itemView.resources,
                    when (dccRuleValidationResultCard.result) {
                        Result.PASSED -> R.color.green
                        Result.OPEN -> R.color.grey_50
                        Result.FAIL -> R.color.red
                    },
                    null
                )
            )
            binding.description.text = dccRuleValidationResultCard.description
            binding.result.text = itemView.resources.getString(
                when (dccRuleValidationResultCard.result) {
                    Result.PASSED -> R.string.passed_for
                    Result.OPEN -> R.string.open_for
                    Result.FAIL -> R.string.failed_for
                },
                Locale("", dccRuleValidationResultCard.countryIsoCode).displayCountry
            )
            if (dccRuleValidationResultCard.current.isNotBlank()) {
                binding.current.text = dccRuleValidationResultCard.current
                View.VISIBLE
            } else {
                View.GONE
            }.apply {
                binding.ruleVerificationCurrentTitle.visibility = this
                binding.current.visibility = this
            }
        }

        companion object {
            fun create(inflater: LayoutInflater, parent: ViewGroup) =
                CardViewHolder(ItemRuleValidationResultBinding.inflate(inflater, parent, false))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder =
        CardViewHolder.create(inflater, parent)

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(dccRuleValidationResultCards[position])
    }

    override fun getItemCount(): Int = dccRuleValidationResultCards.size
}
