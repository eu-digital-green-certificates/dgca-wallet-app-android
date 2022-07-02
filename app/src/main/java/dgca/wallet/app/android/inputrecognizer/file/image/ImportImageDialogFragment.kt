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

package dgca.wallet.app.android.inputrecognizer.file.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import dgca.wallet.app.android.base.BindingBottomSheetDialogFragment
import dgca.wallet.app.android.databinding.DialogFragmentImportImageBinding

class ImportImageDialogFragment : BindingBottomSheetDialogFragment<DialogFragmentImportImageBinding>() {

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentImportImageBinding =
        DialogFragmentImportImageBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.takePhoto.setOnClickListener { setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY to RESULT_TAKE_PHOTO)) }
        binding.pickFromGallery.setOnClickListener {
            setFragmentResult(
                REQUEST_KEY,
                bundleOf(RESULT_KEY to RESULT_PICK_FROM_GALLERY)
            )
        }
    }

    companion object {
        const val REQUEST_KEY = "TakeImageDialogFragment_REQUEST_KEY"
        const val RESULT_KEY = "TakeImageDialogFragment_RESULT_KEY"
        const val RESULT_TAKE_PHOTO = 0
        const val RESULT_PICK_FROM_GALLERY = 1
    }
}
