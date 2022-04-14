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

package dgca.wallet.app.android.ui.settings

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.databinding.ItemSettingsBinding

interface SettingClickListener {
    fun onSettingClick(intent: Intent)
}

class SettingsAdapter(
    private val inflater: LayoutInflater,
    private val settings: List<Pair<String, Intent>>,
    private val settingClickListener: SettingClickListener
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemSettingsBinding.inflate(
                inflater,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val setting = settings[position]
        holder.binding.title.text = setting.first
        holder.binding.title.setOnClickListener {
            settingClickListener.onSettingClick(setting.second)
        }
    }

    override fun getItemCount(): Int = settings.size

    inner class ViewHolder(val binding: ItemSettingsBinding) : RecyclerView.ViewHolder(binding.root)
}
