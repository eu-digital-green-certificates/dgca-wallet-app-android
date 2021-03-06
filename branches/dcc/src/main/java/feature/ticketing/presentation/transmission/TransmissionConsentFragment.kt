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

package feature.ticketing.presentation.transmission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.app.dcc.R
import com.android.app.dcc.databinding.FragmentTransmissionConsentBinding
import dgca.wallet.app.android.dcc.ui.BindingFragment
import dgca.wallet.app.android.dcc.utils.YEAR_MONTH_DAY
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.dcc.utils.getMessage
import dgca.wallet.app.android.dcc.utils.getTitle
import feature.ticketing.presentation.transmission.DefaultDialogFragment.Companion.ACTION_NEGATIVE
import feature.ticketing.presentation.validationresult.BookingPortalValidationResult
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class TransmissionConsentFragment : BindingFragment<FragmentTransmissionConsentBinding>() {

    private val args by navArgs<TransmissionConsentFragmentArgs>()
    private val viewModel by viewModels<TransmissionConsentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        (activity as MainActivity).clearBackground() // TODO: update

        viewModel.event.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTransmissionConsentBinding =
        FragmentTransmissionConsentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title.text = args.certificateModel.getTitle(resources)
        val validTo = args.validTo
        binding.description.text = if (validTo == null) getString(R.string.no_expiration_date) else getString(
            R.string.valid_until,
            DateTimeFormatter.ofPattern(YEAR_MONTH_DAY, Locale.US).format(validTo.toLocalDate())
        )
        binding.consentValue.text =
            getString(R.string.do_you_agree_to_share_certificate, args.certificateModel.getMessage(resources))

        viewModel.uiEvent.observe(viewLifecycleOwner) { event -> onViewModelUiEvent(event.peekContent()) }

        binding.cancel.setOnClickListener {
            close()
        }

        binding.grantPermission.setOnClickListener {
            viewModel.onPermissionAccepted(args.qrString, args.bookingPortalEncryptionData)
        }

        childFragmentManager.setFragmentResultListener(DefaultDialogFragment.KEY_REQUEST, viewLifecycleOwner) { _, bundle ->
            when (bundle.getInt(DefaultDialogFragment.KEY_RESULT)) {
//                ACTION_POSITIVE -> findNavController().popBackStack(R.id.certificatesFragment, false) // TODO: update
                ACTION_NEGATIVE -> viewModel.onPermissionAccepted(args.qrString, args.bookingPortalEncryptionData)
            }
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

    private fun showBookingPortalValidationResultScree(bookingPortalValidationResult: BookingPortalValidationResult) {
        val action =
            TransmissionConsentFragmentDirections.actionTransmissionConsentFragmentToBookingPortalValidationResultFragment(
                bookingPortalValidationResult
            )
        findNavController().navigate(action)
    }

    private fun onViewModelEvent(event: TransmissionConsentViewModel.TransmissionConsentEvent) {
        when (event) {
            is TransmissionConsentViewModel.TransmissionConsentEvent.OnCertificateTransmitted -> {
                showBookingPortalValidationResultScree(event.bookingPortalValidationResult)
            }
            TransmissionConsentViewModel.TransmissionConsentEvent.OnCertificateTransmissionFailed -> {
                val params = DefaultDialogFragment.BuildOptions(
                    message = getString(R.string.cert_transfer_failed),
                    positiveBtnText = getString(R.string.ok),
                    negativeBtnText = getString(R.string.retry)
                )
                DefaultDialogFragment.newInstance(params).show(childFragmentManager, DefaultDialogFragment.TAG)
            }
        }
    }

    private fun close() {
//        findNavController().popBackStack(R.id.certificatesFragment, false) // TODO: update
    }
}
