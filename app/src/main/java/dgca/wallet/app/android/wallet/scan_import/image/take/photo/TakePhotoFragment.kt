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
 *  Created by osarapulov on 9/10/21 1:01 PM
 */

package dgca.wallet.app.android.wallet.scan_import.image.take.photo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentTakePhotoBinding
import dgca.wallet.app.android.wallet.scan_import.ADD_CLAIM_GREEN_CERTIFICATE_MODEL_KEY
import dgca.wallet.app.android.wallet.scan_import.ADD_REQUEST_KEY
import dgca.wallet.app.android.wallet.scan_import.BOOKING_SYSTEM_MODEL_KEY
import dgca.wallet.app.android.wallet.scan_import.qr.CAMERA_REQUEST_CODE
import dgca.wallet.app.android.wallet.scan_import.qr.certificate.ClaimGreenCertificateModel

@AndroidEntryPoint
class TakePhotoFragment : BindingFragment<FragmentTakePhotoBinding>() {

    private lateinit var imageUri: Uri
    private val viewModel by viewModels<TakePhotoViewModel>()
    private val takePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.handleResult()
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTakePhotoBinding =
        FragmentTakePhotoBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.uriLiveData.observe(viewLifecycleOwner) { uri ->
            imageUri = uri
            requestCameraPermission()
        }
        viewModel.result.observe(viewLifecycleOwner) { res ->
            when (res) {
                is TakePhotoResult.Failed -> Toast.makeText(requireContext(), R.string.error_importing_file, Toast.LENGTH_SHORT)
                    .show()
                is TakePhotoResult.GreenCertificateRecognised -> setFragmentResult(
                    ADD_REQUEST_KEY,
                    bundleOf(ADD_CLAIM_GREEN_CERTIFICATE_MODEL_KEY to res.toClaimCertificateModel())
                )
                is TakePhotoResult.BookingSystemModelRecognised -> setFragmentResult(
                    ADD_REQUEST_KEY,
                    bundleOf(BOOKING_SYSTEM_MODEL_KEY to res.ticketingCheckInParcelable)
                )
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takePhotoByCamera()
                } else {
                    findNavController().navigateUp()
                }
                return
            }
        }
    }

    private fun requestCameraPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) -> {
                takePhotoByCamera()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            }
        }
    }

    private fun takePhotoByCamera() {
        takePhoto.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(
                MediaStore.EXTRA_OUTPUT,
                imageUri
            )
        })
    }
}

fun TakePhotoResult.GreenCertificateRecognised.toClaimCertificateModel(): ClaimGreenCertificateModel =
    ClaimGreenCertificateModel(this.qrCodeText, this.dgci, this.cose, this.certificateModel)