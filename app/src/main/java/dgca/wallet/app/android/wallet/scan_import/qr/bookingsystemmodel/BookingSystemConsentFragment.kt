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

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentBookingSystemConsentBinding
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.IdentityDocument
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.identity.IdentityFetcherDialogFragment
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission.DefaultDialogFragment

class BookingSystemConsentFragment : BindingFragment<FragmentBookingSystemConsentBinding>() {
    private val args by navArgs<BookingSystemConsentFragmentArgs>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBookingSystemConsentBinding =
        FragmentBookingSystemConsentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.deny.setOnClickListener {
            findNavController().navigateUp()
        }

        populateView(args.bookingSystemModel)

        binding.allow.setOnClickListener {
            val action =
                BookingSystemConsentFragmentDirections.actionBookingSystemConsentFragmentToIdentityFetcherDialogFragment(args.bookingSystemModel)
            findNavController().navigate(action)
        }

        setFragmentResultListener(IdentityFetcherDialogFragment.IdentityFetcherRequestKey) { key, bundle ->
            findNavController().navigateUp()
            val identityDocument: IdentityDocument? =
                bundle.getParcelable(IdentityFetcherDialogFragment.IdentityFetcherIdentityDocumentParam)
            if (identityDocument != null) {
                showAccessTokenFetcher(identityDocument)
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

    private fun showAccessTokenFetcher(identityDocument: IdentityDocument) {
        val action =
            BookingSystemConsentFragmentDirections.actionBookingSystemConsentFragmentToAccessTokenFetcherDialogFragment(
                identityDocument
            )
        findNavController().navigate(action)
    }

    private fun showCertificatesSelector(identityDocument: IdentityDocument) {
        val action =
            BookingSystemConsentFragmentDirections.actionBookingSystemConsentFragmentToCertificateSelectorFragment()
        findNavController().navigate(action)
    }

    private fun populateView(bookingSystemModel: BookingSystemModel) {
        binding.headerTitle.text = getString(R.string.qr_code_from, bookingSystemModel.serviceIdentity)
        binding.subjectValue.text = bookingSystemModel.subject
        binding.serviceProviderValue.text = bookingSystemModel.serviceProvider
        binding.consentValue.text = bookingSystemModel.consent
    }
}