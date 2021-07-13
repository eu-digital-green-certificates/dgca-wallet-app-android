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
 *  Created by osarapulov on 7/13/21 1:43 PM
 */

package dgca.wallet.app.android.certificate.view.validity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.engine.data.source.local.rules.Converters
import dgca.wallet.app.android.databinding.FragmentRulesValidatationBinding

@AndroidEntryPoint
class RulesValidationFragment : Fragment() {
    private var _binding: FragmentRulesValidatationBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<RulesValidationFragmentArgs>()

    private val viewModel by viewModels<RulesValidationViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRulesValidatationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.progress.isVisible = it
        })
        viewModel.validate(args.qrCodeText, args.selectedCountry, Converters().fromTimestamp(args.timeStamp))
    }
}