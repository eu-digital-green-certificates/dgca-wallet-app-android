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
 *  Created by mykhailo.nester on 14/09/2021, 20:46
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.MainActivity
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentTransmissionConsentBinding

@AndroidEntryPoint
class TransmissionConsentFragment : BindingFragment<FragmentTransmissionConsentBinding>() {

    private val args by navArgs<TransmissionConsentFragmentArgs>()
    private val viewModel by viewModels<TransmissionConsentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).clearBackground()

        viewModel.event.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
        viewModel.init()
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTransmissionConsentBinding =
        FragmentTransmissionConsentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title.text = args.certModel.title
        binding.description.text = getString(R.string.valid_until, args.certModel.validUntil)

        viewModel.uiEvent.observe(viewLifecycleOwner) { event -> onViewModelUiEvent(event.peekContent()) }

        binding.grantPermission.setOnClickListener {
            viewModel.onPermissionAccepted()
        }
    }

    private fun onViewModelUiEvent(event: TransmissionConsentViewModel.TransmissionConsentUiEvent) {
        when (event) {
            TransmissionConsentViewModel.TransmissionConsentUiEvent.OnHideLoading -> binding.progressView.isVisible = false
            TransmissionConsentViewModel.TransmissionConsentUiEvent.OnShowLoading -> binding.progressView.isVisible = true
            TransmissionConsentViewModel.TransmissionConsentUiEvent.OnError -> {
            }
        }
    }

    private fun onViewModelEvent(event: TransmissionConsentViewModel.TransmissionConsentEvent) {
        when (event) {
            TransmissionConsentViewModel.TransmissionConsentEvent.OnCertificateTransmitted -> {
                showDialog(
                    getString(R.string.cert_transferred),
                    true,
                    positiveBtnText = getString(R.string.ok),
                    action = {
                        findNavController().popBackStack(R.id.certificatesFragment, false)
                    }
                )
            }
            TransmissionConsentViewModel.TransmissionConsentEvent.OnCertificateTransmissionFailed -> {
                showDialog(
                    getString(R.string.cert_transfer_failed),
                    positiveBtnText = getString(R.string.ok),
                    negativeBtnText = getString(R.string.retry),
                    action = {
                        viewModel.retry()
                    }
                )
            }
        }
    }

    private fun showDialog(
        message: String,
        isOneAction: Boolean = false,
        positiveBtnText: String = "",
        negativeBtnText: String = "",
        action: () -> Unit
    ) {
        val builder = AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(positiveBtnText) { dialog, _ ->
                action.invoke()
                dialog.dismiss()
            }

        if (!isOneAction) {
            builder.setNegativeButton(negativeBtnText) { dialog, _ -> dialog.dismiss() }
        }


        builder.create()
        builder.show()
    }
}