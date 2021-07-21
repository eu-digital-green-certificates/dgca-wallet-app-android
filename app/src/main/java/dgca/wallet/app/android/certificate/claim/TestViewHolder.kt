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
 *  Created by mykhailo.nester on 4/24/21 5:18 PM
 */

package dgca.wallet.app.android.certificate.claim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.data.TestModel
import dgca.wallet.app.android.databinding.ItemTestBinding
import dgca.wallet.app.android.toFormattedDateTime

class TestViewHolder(private val binding: ItemTestBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: TestModel) {
        binding.testResultValue.text = data.resultType.value
        binding.dateOfCollectionValue.text = data.dateTimeOfCollection.toFormattedDateTime()
        val dateTimeOfTestResult = data.dateTimeOfTestResult?.toFormattedDateTime()
        if (dateTimeOfTestResult?.isNotBlank() == true) {
            binding.dateOfTestResultValue.text = dateTimeOfTestResult
            View.VISIBLE
        } else {
            View.GONE
        }.apply {
            binding.dateOfTestResultTitle.visibility = this
            binding.dateOfTestResultValue.visibility = this
        }

        binding.diseaseValue.text = data.disease.value
        binding.typeOfTestValue.text = data.typeOfTest.value
        binding.countryValue.text = data.countryOfVaccination
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            TestViewHolder(ItemTestBinding.inflate(inflater, parent, false))
    }
}
