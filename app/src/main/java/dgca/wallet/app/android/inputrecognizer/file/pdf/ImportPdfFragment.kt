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
 *  Created by osarapulov on 9/10/21 1:00 PM
 */

package dgca.wallet.app.android.inputrecognizer.file.pdf

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.ui.dashboard.ADD_REQUEST_KEY
import dgca.wallet.app.android.ui.dashboard.CERTIFICATE_MODEL_KEY
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.FragmentImportPdfBinding

@AndroidEntryPoint
class ImportPdfFragment : BindingFragment<FragmentImportPdfBinding>() {

    private val viewModel by viewModels<ImportPdfViewModel>()
    private val pickPdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { uri ->
        viewModel.save(uri.data?.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickPdf.launch(Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "application/pdf"
        })
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentImportPdfBinding =
        FragmentImportPdfBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.result.observe(viewLifecycleOwner) { res ->
            when (res) {
                is ImportPdfResult.Failed -> Toast.makeText(requireContext(), R.string.error_importing_file, Toast.LENGTH_SHORT)
                    .show()
                is ImportPdfResult.CertificateRecognised ->
                    setFragmentResult(
                        ADD_REQUEST_KEY,
                        bundleOf(CERTIFICATE_MODEL_KEY to res.intent)
                    )
                else -> {
                }
            }
            findNavController().navigateUp()
        }
    }
}
