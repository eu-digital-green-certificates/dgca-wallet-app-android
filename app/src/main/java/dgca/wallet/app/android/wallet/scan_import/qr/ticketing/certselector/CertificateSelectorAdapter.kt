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
 *  Created by mykhailo.nester on 14/09/2021, 20:45
 */

package dgca.wallet.app.android.wallet.scan_import.qr.ticketing.certselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.R

class CertificateSelectorAdapter(
    private val inflater: LayoutInflater,
    private val viewModel: CertificateSelectorViewModel,
) : RecyclerView.Adapter<SelectableCertificateViewHolder>() {

    private var items = emptyList<SelectableCertificateModel>()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableCertificateViewHolder =
        SelectableCertificateViewHolder.create(inflater, parent, viewModel)

    override fun onBindViewHolder(holder: SelectableCertificateViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_selectable_certificate

    fun update(list: List<SelectableCertificateModel>) {
        notifyChanges(items, list)
        items = list
    }

    fun RecyclerView.Adapter<out RecyclerView.ViewHolder>.notifyChanges(
        oldList: List<SelectableCertificateModel>,
        newList: List<SelectableCertificateModel>
    ) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
                return Bundle()
            }
        })

        diff.dispatchUpdatesTo(this)
    }
}
