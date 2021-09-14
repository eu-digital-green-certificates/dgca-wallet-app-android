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
import androidx.navigation.fragment.navArgs
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentBookingSystemConsentBinding
import dgca.wallet.app.android.model.BookingSystemModel

class BookingSystemConsentFragment : BindingFragment<FragmentBookingSystemConsentBinding>() {
    private val args by navArgs<BookingSystemConsentFragmentArgs>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBookingSystemConsentBinding =
        FragmentBookingSystemConsentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateView(args.bookingSystemModel)
    }

    private fun populateView(bookingSystemModel: BookingSystemModel) {
        binding.headerTitle.text = getString(R.string.qr_code_from, bookingSystemModel.serviceIdentity)
        binding.subjectValue.text = bookingSystemModel.subject
        binding.serviceProviderValue.text = bookingSystemModel.serviceProvider
        binding.consentValue.text = bookingSystemModel.consent
    }
}