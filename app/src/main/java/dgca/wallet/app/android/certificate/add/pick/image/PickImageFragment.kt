/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 8/25/21 12:12 PM
 */

package dgca.wallet.app.android.certificate.add.pick.image

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.FragmentPickImageBinding


@AndroidEntryPoint
class PickImageFragment : Fragment() {
    companion object {
        const val REQUEST_KEY = "PickImageFragment_REQUEST_KEY"
        const val QR_KEY = "PickImageFragment_QR_KEY"
    }

    private val viewModel by viewModels<PickImageViewModel>()
    private var _binding: FragmentPickImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickImage.launch(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/jpeg"
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPickImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.result.observe(viewLifecycleOwner) { res ->
            when (res) {
                is PickImageResult.Failed -> Toast.makeText(requireContext(), R.string.error_importing_file, Toast.LENGTH_SHORT)
                    .show()
                is PickImageResult.QRRecognised -> setFragmentResult(REQUEST_KEY, bundleOf(QR_KEY to res.qr))
                else -> {
                }
            }
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { uri ->
        viewModel.save(uri.data?.data)
    }
}