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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.MainActivity
import dgca.wallet.app.android.certificate.add.pick.image.PickImageFragment
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentCertificatesBinding
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class CertificatesFragment : BindingFragment<FragmentCertificatesBinding>(),
    CertificateCardsAdapter.CertificateCardClickListener,
    CertificateCardsAdapter.FileCardClickListener {

    private val viewModel by viewModels<CertificatesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).clearBackground()
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCertificatesBinding =
        FragmentCertificatesBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).disableBackButton()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { requireActivity().finish() }
        binding.scanCode.setOnClickListener { showAddNewDialog() }

        viewModel.certificates.observe(viewLifecycleOwner, { setCertificateCards(it) })
        viewModel.inProgress.observe(viewLifecycleOwner, { binding.progressView.isVisible = it })

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

        setFragmentResultListener(PickImageFragment.REQUEST_KEY) { key, bundle ->
            val qr = bundle.getString(PickImageFragment.QR_KEY)
            if (qr?.isNotBlank() == true) {
                val action = CertificatesFragmentDirections.actionCertificatesFragmentToClaimCertificateFragment(qr)
                findNavController().navigate(action)
                Timber.tag("MYTAG").d("QR = $qr")
            }
        }
    }

    private fun setCertificateCards(certificatesCards: List<CertificatesCard>) {
        if (certificatesCards.isNotEmpty()) {
            binding.certificatesView.setHasFixedSize(true)
            binding.certificatesView.layoutManager = LinearLayoutManager(requireContext())
            binding.certificatesView.adapter = CertificateCardsAdapter(certificatesCards, this, this)
            binding.certificatesView.visibility = View.VISIBLE

            binding.noAvailableOffersGroup.visibility = View.GONE
        } else {
            binding.certificatesView.visibility = View.GONE
            binding.noAvailableOffersGroup.visibility = View.VISIBLE
        }
    }

    private fun showCodeReader() {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToCodeReaderFragment()
        findNavController().navigate(action)
    }

    private fun showImportImage() {
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

    override fun onCertificateCardClick(certificateId: Int) {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToViewCertificateFragment(certificateId)
        findNavController().navigate(action)
    }

    override fun onFileCardClick(file: File) {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToViewFileFragment(file)
        findNavController().navigate(action)
    }
}