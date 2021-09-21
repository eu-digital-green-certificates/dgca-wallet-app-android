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
 *  Created by mykhailo.nester on 14/09/2021, 20:45
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.certselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.MainActivity
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentCertificateSelectorBinding

@AndroidEntryPoint
class CertificateSelectorFragment : BindingFragment<FragmentCertificateSelectorBinding>() {

    private val args by navArgs<CertificateSelectorFragmentArgs>()
    private val viewModel by viewModels<CertificateSelectorViewModel>()

    private lateinit var adapter: CertificateSelectorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).clearBackground()

        adapter = CertificateSelectorAdapter(layoutInflater, viewModel)
        viewModel.event.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
        viewModel.init(args.accessTokenResult)
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCertificateSelectorBinding =
        FragmentCertificateSelectorBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        viewModel.uiEvent.observe(viewLifecycleOwner) { event -> onViewModelUiEvent(event.peekContent()) }
        viewModel.certificatesContainer.observe(viewLifecycleOwner) {
            binding.title.text = getString(R.string.certificates_found_title, it.selectableCertificateModelList.size.toString())
            adapter.update(it.selectableCertificateModelList)
            if (it.selectedCertificate != null) {
                binding.nextButton.visibility = View.VISIBLE
            }
        }
        binding.nextButton.setOnClickListener {
            viewModel.onNextClick()
        }
    }

    private fun onViewModelUiEvent(event: CertificateSelectorViewModel.CertificateViewUiEvent) {
        when (event) {
            CertificateSelectorViewModel.CertificateViewUiEvent.OnHideLoading -> binding.progressView.isVisible = false
            CertificateSelectorViewModel.CertificateViewUiEvent.OnShowLoading -> binding.progressView.isVisible = true
            CertificateSelectorViewModel.CertificateViewUiEvent.OnError -> {
            }
        }
    }

    private fun onViewModelEvent(event: CertificateSelectorViewModel.CertificateEvent) {
        when (event) {
            is CertificateSelectorViewModel.CertificateEvent.OnCertificateAdvisorSelected -> {
                val action =
                    CertificateSelectorFragmentDirections.actionCertificateSelectorFragmentToTransmissionConsentFragment(
                        event.certModel.certificateCard.certificate,
                        event.certModel.certificateCard.qrCodeText
                    )
                findNavController().navigate(action)
            }
        }
    }
}