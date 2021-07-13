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
 *  Created by osarapulov on 7/12/21 1:18 PM
 */

package dgca.wallet.app.android.certificate.view.validity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.databinding.FragmentValidityCertificateBinding
import java.util.*

/*-
 * ---license-start
 * eu-digital-green-certificates / dgc-certlogic-android
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 *
 * Created by osarapulov on 12.07.21 13:18
 */
@AndroidEntryPoint
class CertificateValidityFragment : Fragment() {
    private var _binding: FragmentValidityCertificateBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<CertificateValidityFragmentArgs>()

    private val viewModel by viewModels<CertificateValidityViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentValidityCertificateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.init(args.qrCodeText)
        setUpCountriesProcessing()
    }

    private fun setUpCountriesProcessing() {
        viewModel.countries.observe(viewLifecycleOwner, { triple ->
            if (triple.first.isEmpty() || triple.second == null || triple.third == null) {
                binding.iAgreeCheckValidity.isEnabled = false
                View.GONE
            } else {
                val countries = triple.first
                val refinedCountries = countries.map { COUNTRIES_MAP[it] ?: it }
                    .sortedBy { Locale("", it).displayCountry }
                binding.yourDestinationCountry.adapter = CountriesAdapter(refinedCountries, layoutInflater)
                if (triple.second!!.isNotBlank()) {
                    val selectedCountryIndex =
                        refinedCountries.indexOf(triple.second!!)
                    if (selectedCountryIndex >= 0) {
                        binding.yourDestinationCountry.setSelection(selectedCountryIndex)
                    }
                }
                binding.yourDestinationCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View,
                        position: Int,
                        id: Long
                    ) {
                        viewModel.selectCountry(refinedCountries[position].toLowerCase(Locale.ROOT))
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                    }
                }
                binding.iAgreeCheckValidity.isEnabled = true
                View.VISIBLE
            }.apply {
                binding.yourDestinationCountry.visibility = this
            }
        })
    }

    companion object {
        private val COUNTRIES_MAP = mapOf("el" to "gr")
    }
}