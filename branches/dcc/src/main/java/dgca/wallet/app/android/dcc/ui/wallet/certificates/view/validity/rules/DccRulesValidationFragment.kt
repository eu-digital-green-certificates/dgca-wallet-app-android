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
 *  Created by osarapulov on 7/13/21 3:42 PM
 */

package dgca.wallet.app.android.dcc.ui.wallet.certificates.view.validity.rules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.dcc.R
import dgca.wallet.app.android.dcc.data.local.rules.Converters
import com.android.app.dcc.databinding.FragmentRulesValidationBinding
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.engine.Result

@AndroidEntryPoint
class DccRulesValidationFragment : Fragment() {
    private var _binding: FragmentRulesValidationBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<DccRulesValidationFragmentArgs>()

    private val viewModel by viewModels<RulesValidationViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRulesValidationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.progress.isVisible = it
        })
        viewModel.validate(args.qrCodeText, args.selectedCountry, Converters().fromTimestampToZonedDateTime(args.timeStamp))
        binding.rulesList.layoutManager = LinearLayoutManager(requireContext())
        viewModel.validationResults.observe(viewLifecycleOwner, {

            var isCertificateValid = it != null

            val ruleValidationResultCards = mutableListOf<DccRuleValidationResultCard>()
            it?.forEach { validationResult ->
                ruleValidationResultCards.add(
                    validationResult.toRuleValidationResultCard(requireContext())
                )
                isCertificateValid = isCertificateValid && validationResult.result == Result.PASSED
            }

            binding.title.visibility = View.VISIBLE
            binding.title.setText(if (isCertificateValid) R.string.valid_certificate_title else R.string.certificate_has_limitation_title)

            binding.message.visibility = View.VISIBLE
            binding.message.setText(if (isCertificateValid) R.string.valid_certificate_message else R.string.certificate_has_limitation_message)

            binding.icon.visibility = View.VISIBLE
            binding.icon.setImageResource(if (isCertificateValid) R.drawable.icon_large_valid else R.drawable.icon_large_warning)
            if (isCertificateValid) {
                binding.icon.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.green, null)
            } else {
                binding.rulesList.adapter =
                    DccRuleValidationResultsAdapter(layoutInflater, ruleValidationResultCards)
            }

        })
    }
}