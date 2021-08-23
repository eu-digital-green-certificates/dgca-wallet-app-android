/*
 *  ---license-start
<<<<<<< HEAD
 *  eu-digital-green-certificates / dgca-wallet-app-android
=======
 *  eu-digital-green-certificates / dgca-verifier-app-android
>>>>>>> 3305f91 (Implemented take image functionality)
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
<<<<<<< HEAD
 *  Created by osarapulov on 8/22/21 6:32 PM
=======
 *  Created by osarapulov on 8/23/21 8:45 AM
>>>>>>> 3305f91 (Implemented take image functionality)
 */

package dgca.wallet.app.android.certificate.take.photo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.databinding.FragmentTakePhotoBinding


@AndroidEntryPoint
class TakePhotoFragment : Fragment() {
    private val viewModel by viewModels<TakePhotoViewModel>()
    private var _binding: FragmentTakePhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTakePhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.file.observe(viewLifecycleOwner) { file ->
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".provider",
                file
            )
            takePhoto.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, photoURI) })
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

    private val takePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { uri ->
        findNavController().navigateUp()
    }
}