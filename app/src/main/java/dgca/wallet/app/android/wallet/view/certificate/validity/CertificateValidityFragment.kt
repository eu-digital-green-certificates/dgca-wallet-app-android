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
 *  Created by osarapulov on 7/12/21 1:18 PM
 */

package dgca.wallet.app.android.certificate.view.certificate.validity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.source.countries.COUNTRIES_MAP
import dgca.wallet.app.android.data.local.rules.Converters
import dgca.wallet.app.android.databinding.FragmentValidityCertificateBinding
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@AndroidEntryPoint
class CertificateValidityFragment : Fragment() {
    private var _binding: FragmentValidityCertificateBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<CertificateValidityFragmentArgs>()

    private val viewModel by viewModels<CertificateValidityViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentValidityCertificateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCountriesProcessing()
        binding.iAgreeCheckValidity.setOnClickListener {
            val selectedDateTime: ZonedDateTime =
                LocalDate.of(binding.date.year, binding.date.month + 1, binding.date.dayOfMonth).atStartOfDay()
                    .atZone(UTC_ZONE_ID)
            val action =
                CertificateValidityFragmentDirections.actionCertificateValidityFragmentToRulesValidationFragment(
                    args.qrCodeText,
                    binding.yourDestinationCountry.selectedItem as String,
                    Converters().zonedDateTimeToTimestamp(selectedDateTime)
                )
            findNavController().navigate(action)
        }
    }

    private fun setUpCountriesProcessing() {
        viewModel.countries.observe(viewLifecycleOwner, { pair ->
            if (pair.first == null || pair.second == null) {
                binding.iAgreeCheckValidity.isEnabled = false
                binding.progress.visibility = View.VISIBLE
                View.GONE
            } else {
                handleCountries(pair.first!!, pair.second!!)
                binding.progress.visibility = View.GONE
                binding.iAgreeCheckValidity.isEnabled = pair!!.first!!.isNotEmpty()
                View.VISIBLE
            }.apply {
                binding.yourDestinationCountry.visibility = this
            }
        })
    }

    private fun handleCountries(countries: List<String>, selectedCountry: String) {
        if (countries.isEmpty()) {
            binding.yourDestinationCountry.visibility = View.GONE
            binding.yourDestinationCountryText.visibility = View.VISIBLE
        } else {
            binding.yourDestinationCountry.visibility = View.VISIBLE
            binding.yourDestinationCountryText.visibility = View.GONE
            val refinedCountries = countries.sortedBy { Locale("", COUNTRIES_MAP[it] ?: it).displayCountry }
            binding.yourDestinationCountry.adapter = CountriesAdapter(refinedCountries, layoutInflater)
            if (selectedCountry.isNotBlank()) {
                val selectedCountryIndex =
                    refinedCountries.indexOf(selectedCountry)
                if (selectedCountryIndex >= 0) {
                    binding.yourDestinationCountry.setSelection(selectedCountryIndex)
                }
            }
            binding.yourDestinationCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.selectCountry(refinedCountries[position].toLowerCase(Locale.ROOT))
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                }
            }
        }
    }
}