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
 *  Created by osarapulov on 10/2/21 5:48 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.ticketing.validationresult

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.ItemLimitedValidityResultBinding

class BookingPortalLimitedValidityResultItemsAdapter(
    private val inflater: LayoutInflater,
    ruleResultItems: List<BookingPortalRuleResultItem>
) :
    RecyclerView.Adapter<BookingPortalLimitedValidityResultItemsAdapter.CardViewHolder>() {

    private val ruleValidationResultCards: MutableList<BookingPortalRuleResultItem> =
        ruleResultItems.toMutableList()

    class CardViewHolder(private val binding: ItemLimitedValidityResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private fun BookingPortalLimitedValidityResult.getLocalizedText(context: Context): String = context.getString(
            when (this) {
                BookingPortalLimitedValidityResult.OK -> R.string.passed
                BookingPortalLimitedValidityResult.NOK -> R.string.failed
                BookingPortalLimitedValidityResult.CHK -> R.string.open
            }
        )

        fun bind(bookingPortalRuleResultItem: BookingPortalRuleResultItem) {
            binding.ruleVerificationResultHeader.text =
                bookingPortalRuleResultItem.result.getLocalizedText(
                    itemView.context
                )

            binding.ruleVerificationResultHeader.setTextColor(
                ResourcesCompat.getColor(
                    itemView.resources,
                    when (bookingPortalRuleResultItem.result) {
                        BookingPortalLimitedValidityResult.OK -> R.color.green
                        BookingPortalLimitedValidityResult.CHK -> R.color.grey_50
                        BookingPortalLimitedValidityResult.NOK -> R.color.red
                    },
                    null
                )
            )
            binding.identifier.text = bookingPortalRuleResultItem.identifier
            binding.type.text = bookingPortalRuleResultItem.type
            binding.details.text = bookingPortalRuleResultItem.details
        }

        companion object {
            fun create(inflater: LayoutInflater, parent: ViewGroup) =
                CardViewHolder(ItemLimitedValidityResultBinding.inflate(inflater, parent, false))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder =
        CardViewHolder.create(inflater, parent)

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(ruleValidationResultCards[position])
    }

    override fun getItemCount(): Int = ruleValidationResultCards.size
}