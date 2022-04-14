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
 *  Created by osarapulov on 10/11/21 7:51 PM
 */

package feature.ticketing.presentation.accesstoken

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.android.app.dcc.databinding.DialogFragmentProgressBarBinding
import dagger.hilt.android.AndroidEntryPoint
import feature.ticketing.presentation.model.BookingPortalEncryptionData
import dgca.wallet.app.android.dcc.ui.BindingDialogFragment

@AndroidEntryPoint
class BookingPortalEncryptionDataFetcherDialogFragment : BindingDialogFragment<DialogFragmentProgressBarBinding>() {
    private val viewModel by viewModels<BookingPortalEncryptionDataFetcherViewModel>()
    private val args by navArgs<BookingPortalEncryptionDataFetcherDialogFragmentArgs>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentProgressBarBinding =
        DialogFragmentProgressBarBinding.inflate(inflater, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.bookingPortalEncryptionDataResult.observe(viewLifecycleOwner) {
            val bookingPortalEncryptionData: BookingPortalEncryptionData? = if (it is BookingPortalEncryptionDataResult.Success) {
                it.bookingPortalEncryptionData
            } else {
                null
            }
            setFragmentResult(
                BookingPortalEncryptionDataRequestKey,
                bundleOf(BookingPortalEncryptionDataParamKey to bookingPortalEncryptionData)
            )
        }
        viewModel.initialize(
            args.ticketingCheckInParcelable,
            args.ticketingAccessTokenServiceParcelable,
            args.ticketingValidationServiceParcelable
        )
    }

    companion object {
        const val BookingPortalEncryptionDataRequestKey = "BookingPortalEncryptionDataRequest"
        const val BookingPortalEncryptionDataParamKey = "BookingPortalEncryptionDataParam"
    }
}