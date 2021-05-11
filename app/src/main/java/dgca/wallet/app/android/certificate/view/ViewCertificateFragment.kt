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
 *  Created by osarapulov on 5/11/21 2:35 PM
 */

package dgca.wallet.app.android.certificate.view

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.FragmentCertificateViewBinding

@AndroidEntryPoint
class ViewCertificateFragment : Fragment() {
    private val args by navArgs<ViewCertificateFragmentArgs>()
    private val viewModel by viewModels<ViewCertificateViewModel>()
    private var _binding: FragmentCertificateViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCertificateViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val minEdge = Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels)
        viewModel.setCertificateId(args.certificateId, minEdge)
        viewModel.certificate.observe(viewLifecycleOwner, {
            binding.title.text = when {
                it.certificateCard.certificate.vaccinations?.first() != null -> binding.root.resources.getString(
                    R.string.vaccination,
                    it.certificateCard.certificate.vaccinations.first().doseNumber.toString()
                )
                it.certificateCard.certificate.recoveryStatements?.isNotEmpty() == true -> binding.root.resources.getString(R.string.recovery)
                it.certificateCard.certificate.tests?.isNotEmpty() == true -> binding.root.resources.getString(R.string.test)
                else -> ""
            }

            binding.qrCode.setImageBitmap(it.qrCode)

            binding.name.text = getString(
                R.string.name_surname,
                it.certificateCard.certificate.person.givenName,
                it.certificateCard.certificate.person.familyName
            )

            binding.list.visibility = View.VISIBLE
        })
    }
}