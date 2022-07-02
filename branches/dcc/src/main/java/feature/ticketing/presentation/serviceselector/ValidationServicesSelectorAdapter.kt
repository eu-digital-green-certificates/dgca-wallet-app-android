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
 *  Created by osarapulov on 9/20/21 12:43 PM
 */

package feature.ticketing.presentation.serviceselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.app.dcc.databinding.ItemValidationServiceBinding

class ValidationServicesSelectorAdapter(
    private val inflater: LayoutInflater,
    private val viewModel: ValidationServiceViewModel
) : RecyclerView.Adapter<ValidationServicesSelectorAdapter.ViewHolder>() {
    private var items: List<SelectableValidationServiceModel> = emptyList()

    class ViewHolder(private val binding: ItemValidationServiceBinding, viewModel: ValidationServiceViewModel) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                viewModel.onValidationServiceSelected(bindingAdapterPosition)
            }
        }

        fun bind(selectableValidationServiceModel: SelectableValidationServiceModel) {
            binding.title.text = selectableValidationServiceModel.title
            binding.description.text = selectableValidationServiceModel.subTitle
            binding.radioButton.isChecked = selectableValidationServiceModel.selected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = create(inflater, parent, viewModel)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(list: List<SelectableValidationServiceModel>) {
        notifyChanges(items, list)
        items = list
    }

    private fun RecyclerView.Adapter<out RecyclerView.ViewHolder>.notifyChanges(
        oldList: List<SelectableValidationServiceModel>,
        newList: List<SelectableValidationServiceModel>
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

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup, viewModel: ValidationServiceViewModel) =
            ViewHolder(ItemValidationServiceBinding.inflate(inflater, parent, false), viewModel)
    }
}