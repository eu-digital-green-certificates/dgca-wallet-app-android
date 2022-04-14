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
 *  Created by osarapulov on 9/10/21 12:58 PM
 */

package dgca.wallet.app.android.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import dgca.wallet.app.android.base.BindingBottomSheetDialogFragment
import dgca.wallet.app.android.databinding.DialogFragmentAddNewBinding

const val ADD_REQUEST_KEY = "ADD_REQUEST"
const val CERTIFICATE_MODEL_KEY = "CERTIFICATE_MODEL"

class AddNewBottomDialogFragment : BindingBottomSheetDialogFragment<DialogFragmentAddNewBinding>() {

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentAddNewBinding =
        DialogFragmentAddNewBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.scanCertificate.setOnClickListener { setFragmentResult(
            REQUEST_KEY, bundleOf(
                RESULT_KEY to RESULT_SCAN_CODE
            )) }
        binding.scanNfcCode.setOnClickListener { setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY to RESULT_NFC)) }
        binding.importImage.setOnClickListener { setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY to RESULT_IMPORT_IMAGE)) }
        binding.importPdf.setOnClickListener { setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY to RESULT_IMPORT_PDF)) }
    }

    companion object {
        const val REQUEST_KEY = "AddNewBottomDialogFragment_REQUEST_KEY"
        const val RESULT_KEY = "AddNewBottomDialogFragment_RESULT_KEY"
        const val RESULT_SCAN_CODE = 0
        const val RESULT_NFC = 1
        const val RESULT_IMPORT_IMAGE = 2
        const val RESULT_IMPORT_PDF = 3
    }
}
