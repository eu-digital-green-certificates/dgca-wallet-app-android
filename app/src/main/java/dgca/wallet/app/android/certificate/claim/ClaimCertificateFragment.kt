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
 *  Created by mykhailo.nester on 5/12/21 12:27 AM
 */

package dgca.wallet.app.android.certificate.claim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.FORMATTED_YEAR_MONTH_DAY
import dgca.wallet.app.android.R
import dgca.wallet.app.android.YEAR_MONTH_DAY
import dgca.wallet.app.android.data.CertificateData
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.databinding.FragmentCertificateClaimBinding
import dgca.wallet.app.android.parseFromTo

@AndroidEntryPoint
class ClaimCertificateFragment : Fragment() {

    private val args by navArgs<ClaimCertificateFragmentArgs>()
    private val viewModel by viewModels<ClaimCertificateViewModel>()
    private var _binding: FragmentCertificateClaimBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CertListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CertListAdapter(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCertificateClaimBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        binding.saveBtn.setOnClickListener { viewModel.save(args.qrCodeText, args.tan) }
        viewModel.inProgress.observe(viewLifecycleOwner, { binding.progressView.isVisible = it })
        viewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
        viewModel.certificate.observe(viewLifecycleOwner, { certificate ->
            if (certificate != null) {
                showUserData(certificate)
                val list = getCertificateListData(certificate)
                adapter.update(list)
            }
        })
        viewModel.init(args.qrCodeText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showUserData(certificate: CertificateModel) {
        binding.personFullName.text =
            getString(R.string.person_full_name_placeholder, certificate.person.givenName, certificate.person.familyName)
        binding.personStandardisedFamilyName.text = certificate.person.standardisedFamilyName
        binding.personStandardisedGivenName.text = certificate.person.standardisedGivenName
        binding.dateOfBirth.text = certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
    }

    private fun getCertificateListData(certificate: CertificateModel): List<CertificateData> {
        val list = mutableListOf<CertificateData>()
        list.addAll(certificate.vaccinations ?: emptyList())
        list.addAll(certificate.tests ?: emptyList())
        list.addAll(certificate.recoveryStatements ?: emptyList())

        return list
    }

    private fun onViewModelEvent(event: ClaimCertificateViewModel.ClaimCertEvent) {
        when (event) {
            is ClaimCertificateViewModel.ClaimCertEvent.OnCertClaimed -> {
                if (event.result) {
                    Toast.makeText(requireContext(), "Certificate claimed", Toast.LENGTH_SHORT).show()
                    val action = ClaimCertificateFragmentDirections.actionClaimCertificateFragmentToCertificatesFragment()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Certificate not claimed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}