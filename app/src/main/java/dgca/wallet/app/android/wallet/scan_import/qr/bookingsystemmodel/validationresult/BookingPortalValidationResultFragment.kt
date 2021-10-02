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
 *  Created by osarapulov on 10/1/21 1:11 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.validationresult

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.MainActivity
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentBookingPortalValidationResultBinding


@AndroidEntryPoint
class BookingPortalValidationResultFragment : BindingFragment<FragmentBookingPortalValidationResultBinding>() {
    private val args by navArgs<BookingPortalValidationResultFragmentArgs>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBookingPortalValidationResultBinding =
        FragmentBookingPortalValidationResultBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).disableBackButton()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { close() }

        binding.actionButton.setOnClickListener { close() }

        val title = getString(
            when (args.bookingPortalValidationResult) {
                BookingPortalValidationResult.Valid -> R.string.valid_certificate_title
                BookingPortalValidationResult.Invalid -> R.string.invalid_certificate_title
                is BookingPortalValidationResult.LimitedValidity -> R.string.certificate_has_limitation_title
            }
        )
        binding.title.text = title

        val text = getString(
            when (args.bookingPortalValidationResult) {
                BookingPortalValidationResult.Valid -> R.string.valid_certificate_message
                BookingPortalValidationResult.Invalid -> R.string.invalid_certificate_message
                is BookingPortalValidationResult.LimitedValidity -> R.string.certificate_has_limitation_message
            }
        )
        binding.message.text = text

        val icon = when (args.bookingPortalValidationResult) {
            BookingPortalValidationResult.Valid -> R.drawable.icon_large_valid
            BookingPortalValidationResult.Invalid -> R.drawable.icon_large_invalid
            is BookingPortalValidationResult.LimitedValidity -> R.drawable.icon_large_warning
        }
        binding.icon.setImageResource(icon)

        when (args.bookingPortalValidationResult) {
            BookingPortalValidationResult.Valid -> binding.icon.backgroundTintList =
                ResourcesCompat.getColorStateList(resources, R.color.green, null)
            BookingPortalValidationResult.Invalid -> binding.icon.imageTintList =
                ResourcesCompat.getColorStateList(resources, R.color.red, null)
            is BookingPortalValidationResult.LimitedValidity -> {
                binding.rulesList.layoutManager = LinearLayoutManager(requireContext())
                binding.rulesList.adapter =
                    BookingPortalLimitedValidityResultItemsAdapter(
                        layoutInflater,
                        (args.bookingPortalValidationResult as BookingPortalValidationResult.LimitedValidity).bookingPortalLimitedValidityResultItems
                    )
            }
        }
    }

    private fun close() {
        findNavController().popBackStack(R.id.certificatesFragment, false)
    }
}