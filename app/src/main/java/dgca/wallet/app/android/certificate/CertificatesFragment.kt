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
 *  Created by osarapulov on 5/7/21 8:59 AM
 */

package dgca.wallet.app.android.certificate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.MainActivity
import dgca.wallet.app.android.databinding.FragmentCertificatesBinding
import java.io.File
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class CertificatesFragment : Fragment(), CertificateCardsAdapter.CertificateCardClickListener {

    private val viewModel by viewModels<CertificatesViewModel>()
    private var _binding: FragmentCertificatesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).clearBackground()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        (activity as MainActivity).disableBackButton()
        _binding = FragmentCertificatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { requireActivity().finish() }
        binding.scanCode.setOnClickListener { showAddNewDialog() }

        viewModel.certificates.observe(viewLifecycleOwner, {
            setCertificateCards(it)
        })
        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.progressView.isVisible = it
        })
        viewModel.fetchCertificates()

        setFragmentResultListener(AddNewBottomDialogFragment.REQUEST_KEY) { key, bundle ->
            findNavController().navigateUp()
            when (bundle.getInt(AddNewBottomDialogFragment.RESULT_KEY)) {
                AddNewBottomDialogFragment.RESULT_SCAN_CODE -> showCodeReader()
                AddNewBottomDialogFragment.RESULT_IMPORT_IMAGE -> showImportImage()
                AddNewBottomDialogFragment.RESULT_IMPORT_PDF -> showImportPdf()
                else -> throw IllegalStateException()
            }
        }

        setFragmentResultListener(ImportImageDialogFragment.REQUEST_KEY) { key, bundle ->
            findNavController().navigateUp()
            when (bundle.getInt(ImportImageDialogFragment.RESULT_KEY)) {
                ImportImageDialogFragment.RESULT_TAKE_PHOTO -> showTakePhoto()
                ImportImageDialogFragment.RESULT_PICK_FROM_GALLERY -> showPickImage()
                else -> throw IllegalStateException()
            }
        }
    }

    private fun setCertificateCards(certificatesCards: List<CertificatesCard>) {
        if (certificatesCards.isNotEmpty()) {
            binding.certificatesView.setHasFixedSize(true)
            binding.certificatesView.layoutManager = LinearLayoutManager(requireContext())
            binding.certificatesView.adapter = CertificateCardsAdapter(certificatesCards, this)
            binding.certificatesView.visibility = View.VISIBLE

            binding.noAvailableOffersGroup.visibility = View.GONE
        } else {
            binding.certificatesView.visibility = View.GONE
            binding.noAvailableOffersGroup.visibility = View.VISIBLE
        }
    }

    override fun onCertificateCardClick(certificateId: Int) {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToViewCertificateFragment(certificateId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showCodeReader() {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToCodeReaderFragment()
        findNavController().navigate(action)
    }

    fun showImportImage() {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToImagePhotoDialogFragment()
        findNavController().navigate(action)
    }

    private fun showAddNewDialog() {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToAddNewDialogFragment()
        findNavController().navigate(action)
    }

    private fun showTakePhoto() {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToTakePhotoFragment()
        findNavController().navigate(action)
    }

    private fun showPickImage() {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToPickImageFragment()
        findNavController().navigate(action)
    }

    private fun showImportPdf() {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToImportPdfFragment()
        findNavController().navigate(action)
    }
}