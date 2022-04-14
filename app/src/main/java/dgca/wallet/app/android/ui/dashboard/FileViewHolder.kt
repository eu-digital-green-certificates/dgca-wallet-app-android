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
import dgca.wallet.app.android.databinding.CertificateCardFileBinding
import dgca.wallet.app.android.extension.YEAR_MONTH_DAY
import dgca.wallet.app.android.extension.formatWith
import dgca.wallet.app.android.ui.model.FileCard

class FileViewHolder(
    private val binding: CertificateCardFileBinding,
    private val viewModel: DashboardViewModel
) : RecyclerView.ViewHolder(binding.root) {

    init {
        itemView.setOnClickListener { viewModel.onFileCardClick(bindingAdapterPosition) }
        itemView.setOnLongClickListener {
            viewModel.showDeleteFileDialog(bindingAdapterPosition)
            return@setOnLongClickListener true
        }
    }

    fun bind(fileCard: FileCard) {
        binding.titleView.text = fileCard.file.name
        binding.scannedAtDateView.text = fileCard.dateTaken.formatWith(YEAR_MONTH_DAY)
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup, model: DashboardViewModel) =
            FileViewHolder(CertificateCardFileBinding.inflate(inflater, parent, false), model)
    }
}
