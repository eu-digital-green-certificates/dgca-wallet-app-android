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
 *  Created by osarapulov on 8/25/21 4:40 PM
 */

package dgca.wallet.app.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dgca.wallet.app.android.base.BindingDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.databinding.DialogFragmentDeleteCertificateBinding

const val DELETE_CERTIFICATE_REQUEST_KEY = "DELETE_CERTIFICATE_REQUEST"
const val DELETE_CERTIFICATE_ITEM_POSITION_RESULT_PARAM = "DELETE_CERTIFICATE_ITEM_POSITION_RESULT_PARAM"
const val DELETE_CERTIFICATE_ITEM_CARD_RESULT_PARAM = "DELETE_CERTIFICATE_ITEM_CARD_RESULT_PARAM"

@AndroidEntryPoint
class DeleteCertificateDialogFragment : BindingDialogFragment<DialogFragmentDeleteCertificateBinding>() {

    private val args: DeleteCertificateDialogFragmentArgs by navArgs()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentDeleteCertificateBinding =
        DialogFragmentDeleteCertificateBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.cancel.setOnClickListener { findNavController().navigateUp() }
        binding.delete.setOnClickListener {
            findNavController().navigateUp()
            setFragmentResult(
                DELETE_CERTIFICATE_REQUEST_KEY,
                bundleOf(
                    DELETE_CERTIFICATE_ITEM_POSITION_RESULT_PARAM to args.position,
                    DELETE_CERTIFICATE_ITEM_CARD_RESULT_PARAM to args.itemCard
                )
            )
        }

        binding.typeTitle.text = args.itemCard.typeTitle(resources)
        binding.typeValue.text = args.itemCard.typeValue(resources)
        binding.dateValue.text = args.itemCard.dateString(resources)
        binding.fromValue.text = args.itemCard.subTitle(resources)
    }
}
