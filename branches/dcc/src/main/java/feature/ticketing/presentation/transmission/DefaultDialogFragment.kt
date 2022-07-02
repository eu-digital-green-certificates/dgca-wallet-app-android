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
 *  Created by mykhailo.nester on 16/09/2021, 11:36
 */

package feature.ticketing.presentation.transmission

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import kotlinx.parcelize.Parcelize

class DefaultDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val buildOptions = requireArguments().getParcelable<BuildOptions>(KEY_BUILD_OPTIONS)

        val builder = AlertDialog.Builder(requireContext())
            .setMessage(buildOptions?.message)
            .setPositiveButton(buildOptions?.positiveBtnText) { dialog, _ ->
                setFragmentResult(KEY_REQUEST, bundleOf(KEY_RESULT to ACTION_POSITIVE))
                dialog.dismiss()
            }
        isCancelable = false

        if (buildOptions?.isOneButton == false) {
            builder.setNegativeButton(buildOptions.negativeBtnText) { dialog, _ ->
                setFragmentResult(KEY_REQUEST, bundleOf(KEY_RESULT to ACTION_NEGATIVE))
                dialog.dismiss()
            }
        }
        return builder.create()
    }

    companion object {
        const val TAG = "BaseDialogFragment"
        const val KEY_REQUEST = "dialog_request"
        const val KEY_RESULT = "dialog_result"
        const val KEY_BUILD_OPTIONS = "key_build_options"
        const val ACTION_NEGATIVE = 0
        const val ACTION_POSITIVE = 1

        fun newInstance(buildOptions: BuildOptions) = DefaultDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_BUILD_OPTIONS, buildOptions)
            }
        }
    }

    @Parcelize
    data class BuildOptions(
        val message: String = "",
        val positiveBtnText: String = "",
        val negativeBtnText: String = "",
        val isOneButton: Boolean = false
    ) : Parcelable
}