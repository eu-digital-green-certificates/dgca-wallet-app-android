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
 *  Created by osarapulov on 9/17/21 9:23 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.serviceselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentValidationServiceSelectorBinding
import dgca.wallet.app.android.model.BookingPortalEncryptionData
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.access.token.BookingPortalEncryptionDataFetcherDialogFragment
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.Service
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission.DefaultDialogFragment

@AndroidEntryPoint
class ValidationServiceSelectorFragment : BindingFragment<FragmentValidationServiceSelectorBinding>() {
    private val args by navArgs<ValidationServiceSelectorFragmentArgs>()
    private val viewModel by viewModels<ValidationServiceViewModel>()

    private lateinit var adapter: ValidationServicesSelectorAdapter

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentValidationServiceSelectorBinding =
        FragmentValidationServiceSelectorBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.validationServiceList.layoutManager = layoutManager

        adapter = ValidationServicesSelectorAdapter(layoutInflater, viewModel)
        binding.validationServiceList.adapter = adapter

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

        viewModel.validationServicesContainer.observe(viewLifecycleOwner) { validationServicesContainer ->
            binding.title.text =
                getString(
                    R.string.validation_services_found_title,
                    validationServicesContainer.selectableValidationServiceModelList.size.toString()
                )
            adapter.update(validationServicesContainer.selectableValidationServiceModelList)
            binding.nextButton.visibility = if (validationServicesContainer.selectedService != null) View.VISIBLE else View.GONE
            binding.nextButton.setOnClickListener {
                showAccessTokenFetcher(
                    args.bookingSystemModel,
                    args.identityDocument.accessTokenService,
                    validationServicesContainer.selectedService!!
                )
            }
        }

        viewModel.init(args.identityDocument.validationServices)

        binding.nextButton.setOnClickListener {
            showAccessTokenFetcher(
                args.bookingSystemModel,
                args.identityDocument.accessTokenService,
                args.identityDocument.validationServices.first()
            )
        }
    }

    private fun showAccessTokenFetcher(
        bookingSystemModel: BookingSystemModel,
        accessTokenService: Service,
        validationService: Service
    ) {
        val action =
            ValidationServiceSelectorFragmentDirections.actionValidationServiceSelectorFragmentToBookingPortalEncryptionDataFetcherDialogFragment(
                bookingSystemModel, accessTokenService, validationService
            )
        findNavController().navigate(action)
    }

    private fun showCertificatesSelector(bookingPortalEncryptionData: BookingPortalEncryptionData) {
        val action =
            ValidationServiceSelectorFragmentDirections.actionValidationServiceSelectorFragmentToCertificateSelectorFragment(
                bookingPortalEncryptionData
            )
        findNavController().navigate(action)
    }
}