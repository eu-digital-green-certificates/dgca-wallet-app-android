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

package dgca.wallet.app.android.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.app.base.BaseItem
import com.android.app.base.ProcessorItemCard
import dgca.wallet.app.android.ui.model.FileCard
import dgca.wallet.app.android.ui.model.HeaderItem

class DashboardAdapter(
    private val inflater: LayoutInflater,
    private val model: DashboardViewModel,
    private val itemCards: List<BaseItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = itemCards.size

    override fun getItemViewType(position: Int): Int {
        return when (itemCards[position]) {
            is HeaderItem -> ItemViewType.HEADER_TYPE.ordinal
            is ProcessorItemCard -> ItemViewType.CERTIFICATE_TYPE.ordinal
            is FileCard -> ItemViewType.FILE_TYPE.ordinal
            else -> throw IllegalArgumentException("Not supported type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemViewType.HEADER_TYPE.ordinal -> HeaderViewHolder.create(inflater, parent)
            ItemViewType.CERTIFICATE_TYPE.ordinal -> CertificateViewHolder.create(
                inflater,
                parent,
                model
            )
            ItemViewType.FILE_TYPE.ordinal -> FileViewHolder.create(inflater, parent, model)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = itemCards[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(data as HeaderItem)
            is CertificateViewHolder -> holder.bind(data as ProcessorItemCard)
            is FileViewHolder -> holder.bind(data as FileCard)
        }
    }

    enum class ItemViewType {
        HEADER_TYPE, CERTIFICATE_TYPE, FILE_TYPE
    }
}
