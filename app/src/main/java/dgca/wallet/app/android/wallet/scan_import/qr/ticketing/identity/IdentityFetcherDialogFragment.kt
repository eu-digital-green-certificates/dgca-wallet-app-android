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
 *  Created by osarapulov on 9/16/21 3:22 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.ticketing.identity

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
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.base.BindingDialogFragment
import dgca.wallet.app.android.databinding.DialogFragmentProgressBarBinding
import dgca.wallet.app.android.model.TicketingIdentityDocumentParcelable
import dgca.wallet.app.android.model.fromRemote
import dgca.wallet.app.android.wallet.scan_import.qr.ticketing.IdentityFetcherResult
import dgca.wallet.app.android.wallet.scan_import.qr.ticketing.IdentityFetcherViewModel

@AndroidEntryPoint
class IdentityFetcherDialogFragment : BindingDialogFragment<DialogFragmentProgressBarBinding>() {
    private val viewModel by viewModels<IdentityFetcherViewModel>()
    private val args by navArgs<IdentityFetcherDialogFragmentArgs>()

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
        viewModel.identityFetcherResult.observe(viewLifecycleOwner) {
            val ticketingIdentityDocumentParcelable: TicketingIdentityDocumentParcelable? =
                if (it is IdentityFetcherResult.Success) {
                    it.ticketingIdentityDocumentRemote.fromRemote()
                } else {
                    null
                }
            setFragmentResult(
                IdentityFetcherRequestKey,
                bundleOf(IdentityFetcherIdentityDocumentParam to ticketingIdentityDocumentParcelable)
            )
        }
        viewModel.initialize(args.ticketingCheckInParcelable)
    }

    companion object {
        const val IdentityFetcherRequestKey = "IdentityFetcherRequest"
        const val IdentityFetcherIdentityDocumentParam = "IdentityFetcherIdentityDocumentParam"
    }
}