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
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.MainActivity
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentBookingPortalValidationResultBinding


@AndroidEntryPoint
class BookingPortalValidationResultFragment : BindingFragment<FragmentBookingPortalValidationResultBinding>() {
    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBookingPortalValidationResultBinding =
        FragmentBookingPortalValidationResultBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).disableBackButton()

        binding.actionButton.setOnClickListener {
            findNavController().popBackStack(R.id.certificatesFragment, false)
        }
    }
}