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
 *  Created by osarapulov on 9/13/21 2:22 PM
 */

package feature.ticketing.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.app.dcc.R
import com.android.app.dcc.databinding.FragmentBookingSystemConsentBinding
import dgca.wallet.app.android.dcc.ui.BindingFragment
import feature.ticketing.presentation.identity.IdentityFetcherDialogFragment
import feature.ticketing.presentation.accesstoken.BookingPortalEncryptionDataFetcherDialogFragment
import feature.ticketing.presentation.model.BookingPortalEncryptionData
import feature.ticketing.presentation.model.TicketingCheckInParcelable
import feature.ticketing.presentation.model.TicketingIdentityDocumentParcelable
import feature.ticketing.presentation.transmission.DefaultDialogFragment

class BookingSystemConsentFragment : BindingFragment<FragmentBookingSystemConsentBinding>() {

    private val args by navArgs<BookingSystemConsentFragmentArgs>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBookingSystemConsentBinding =
        FragmentBookingSystemConsentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.deny.setOnClickListener {
            findNavController().navigateUp()
        }

        populateView(args.ticketingCheckInParcelable)

        binding.allow.setOnClickListener {
            val action =
                BookingSystemConsentFragmentDirections.actionBookingSystemConsentFragmentToIdentityFetcherDialogFragment(args.ticketingCheckInParcelable)
            findNavController().navigate(action)
        }

        setFragmentResultListener(IdentityFetcherDialogFragment.IdentityFetcherRequestKey) { key, bundle ->
            findNavController().navigateUp()
            val ticketingIdentityDocumentParcelable: TicketingIdentityDocumentParcelable? =
                bundle.getParcelable(IdentityFetcherDialogFragment.IdentityFetcherIdentityDocumentParam)
            if (ticketingIdentityDocumentParcelable == null || ticketingIdentityDocumentParcelable.validationServices.isEmpty()) {
                val params = DefaultDialogFragment.BuildOptions(
                    message = getString(R.string.something_went_wrong),
                    positiveBtnText = getString(R.string.ok),
                    isOneButton = true
                )
                DefaultDialogFragment.newInstance(params).show(childFragmentManager, DefaultDialogFragment.TAG)
            } else if (ticketingIdentityDocumentParcelable.validationServices.size == 1) {
                showAccessTokenFetcher(
                    args.ticketingCheckInParcelable,
                    ticketingIdentityDocumentParcelable.accessTokenService,
                    ticketingIdentityDocumentParcelable.validationServices.first()
                )
            } else {
                showValidationServiceSelector(args.ticketingCheckInParcelable, ticketingIdentityDocumentParcelable)
            }
        }

        setFragmentResultListener(BookingPortalEncryptionDataFetcherDialogFragment.BookingPortalEncryptionDataRequestKey) { key, bundle ->
            findNavController().navigateUp()
            val bookingPortalEncryptionData: BookingPortalEncryptionData? =
                bundle.getParcelable(BookingPortalEncryptionDataFetcherDialogFragment.BookingPortalEncryptionDataParamKey)
            if (bookingPortalEncryptionData != null) {
                showCertificatesSelector(bookingPortalEncryptionData)
            } else {
                val params = DefaultDialogFragment.BuildOptions(
                    message = getString(R.string.something_went_wrong),
                    positiveBtnText = getString(R.string.ok),
                    isOneButton = true
                )
                DefaultDialogFragment.newInstance(params).show(childFragmentManager, DefaultDialogFragment.TAG)
            }
        }
    }

    private fun showValidationServiceSelector(
        ticketingCheckInParcelable: TicketingCheckInParcelable,
        ticketingIdentityDocumentParcelable: TicketingIdentityDocumentParcelable
    ) {
        val action =
            BookingSystemConsentFragmentDirections.actionBookingSystemConsentFragmentToValidationServiceSelectorFragment(
                ticketingCheckInParcelable, ticketingIdentityDocumentParcelable
            )
        findNavController().navigate(action)
    }

    private fun showAccessTokenFetcher(
        ticketingCheckInParcelable: TicketingCheckInParcelable,
        accessTokenTicketingServiceParcelable: TicketingServiceParcelable,
        validationTicketingServiceParcelable: TicketingServiceParcelable
    ) {
        val action =
            BookingSystemConsentFragmentDirections.actionBookingSystemConsentFragmentToBookingPortalEncryptionDataFetcherDialogFragment(
                ticketingCheckInParcelable, accessTokenTicketingServiceParcelable, validationTicketingServiceParcelable
            )
        findNavController().navigate(action)
    }

    private fun showCertificatesSelector(bookingPortalEncryptionData: BookingPortalEncryptionData) {
        val action =
            BookingSystemConsentFragmentDirections.actionBookingSystemConsentFragmentToCertificateSelectorFragment(
                bookingPortalEncryptionData
            )
        findNavController().navigate(action)
    }

    private fun populateView(ticketingCheckInParcelable: TicketingCheckInParcelable) {
        binding.headerTitle.text = getString(R.string.qr_code_from, ticketingCheckInParcelable.serviceIdentity)
        binding.subjectValue.text = ticketingCheckInParcelable.subject
        binding.serviceProviderValue.text = ticketingCheckInParcelable.serviceProvider
        binding.consentValue.text = ticketingCheckInParcelable.consent
    }
}