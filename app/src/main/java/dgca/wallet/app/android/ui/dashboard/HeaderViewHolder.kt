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
 *  Created by osarapulov on 8/25/21 4:40 PM
 */

package dgca.wallet.app.android.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.ItemHeaderBinding
import dgca.wallet.app.android.ui.model.HeaderItem

class HeaderViewHolder(
    private val binding: ItemHeaderBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: HeaderItem) {
        when (item) {
            is HeaderItem.CertificatesHeader -> binding.title.setText(R.string.certificates)
            is HeaderItem.ImagesHeader -> binding.title.setText(R.string.images)
            is HeaderItem.PdfsHeader -> binding.title.setText(R.string.pdfs)
        }
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            HeaderViewHolder(ItemHeaderBinding.inflate(inflater, parent, false))
    }
}
