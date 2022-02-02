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
 *  Created by mykhailo.nester on 5/12/21 12:27 AM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.certificate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.getCertificateListData
import dgca.wallet.app.android.databinding.FragmentCertificateClaimBinding

@AndroidEntryPoint
class ClaimCertificateFragment : BindingFragment<FragmentCertificateClaimBinding>() {

    private val args by navArgs<ClaimCertificateFragmentArgs>()
    private val viewModel by viewModels<ClaimCertificateViewModel>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCertificateClaimBinding =
        FragmentCertificateClaimBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter =
            CertListAdapter(layoutInflater, args.claimCertificateModel.certificateModel.getCertificateListData())

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(TanFragment.TAN_KEY)
            ?.observe(viewLifecycleOwner) { tan ->
                viewModel.save(args.claimCertificateModel, tan)
            }

        binding.saveBtn.setOnClickListener {
            val action = ClaimCertificateFragmentDirections.actionClaimCertificateFragmentToTanFragment()
            findNavController().navigate(action)
        }
        viewModel.inProgress.observe(viewLifecycleOwner, { binding.progressView.isVisible = it })
        viewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }

        showUserData(args.claimCertificateModel.certificateModel)
    }

    private fun CertificateModel.getType() = when {
        vaccinations?.isNotEmpty() == true -> getString(
            R.string.vaccination_of,
            this.vaccinations.first().doseNumber.toString(),
            this.vaccinations.first().totalSeriesOfDoses.toString()
        )
        recoveryStatements?.isNotEmpty() == true -> getString(R.string.recovery)
        tests?.isNotEmpty() == true -> getString(R.string.test)
        else -> ""
    }

    private fun showUserData(certificate: CertificateModel) {
        certificate.getType().bindText(binding.certificateTypeTitle, binding.certificateTypeValue)
        certificate.getFullName().bindText(binding.nameTitle, binding.personFullName)
    }

    private fun onViewModelEvent(event: ClaimCertificateViewModel.ClaimCertEvent) {
        when (event) {
            is ClaimCertificateViewModel.ClaimCertEvent.OnCertClaimed -> {
                if (event.isClaimed) {
                    Toast.makeText(requireContext(), getString(R.string.certificate_claimed), Toast.LENGTH_SHORT).show()
                    val action = ClaimCertificateFragmentDirections.actionClaimCertificateFragmentToCertificatesFragment()
                    findNavController().navigate(action)
                }
            }
            is ClaimCertificateViewModel.ClaimCertEvent.OnCertNotClaimed -> {
                Toast.makeText(requireContext(), getString(R.string.check_the_tan_and_try_again), Toast.LENGTH_SHORT).show()
            }
        }
    }
}