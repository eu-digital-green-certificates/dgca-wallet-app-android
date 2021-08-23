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
 *  Created by osarapulov on 8/23/21 1:54 PM
 */

package dgca.wallet.app.android.certificate.view.certificate

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.certificate.claim.CertListAdapter
import dgca.wallet.app.android.certificate.claim.bindText
import dgca.wallet.app.android.certificate.view.certificate.ViewCertificateFragmentArgs
import dgca.wallet.app.android.certificate.view.certificate.ViewCertificateFragmentDirections
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.getCertificateListData
import dgca.wallet.app.android.databinding.FragmentCertificateViewBinding
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class ViewCertificateFragment : Fragment() {
    private val args by navArgs<ViewCertificateFragmentArgs>()
    private val viewModel by viewModels<ViewCertificateViewModel>()
    private var _binding: FragmentCertificateViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CertListAdapter

    @Inject
    lateinit var shareImageIntentProvider: ShareImageIntentProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        adapter = CertListAdapter(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCertificateViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val minEdge = displayMetrics.widthPixels * 0.9

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        viewModel.inProgress.observe(viewLifecycleOwner, { binding.progressView.isVisible = it })
        viewModel.certificate.observe(viewLifecycleOwner, {
            val certificate = it.certificatesCard.certificate
            binding.title.text = when {
                certificate.vaccinations?.first() != null -> binding.root.resources.getString(
                    R.string.vaccination,
                    certificate.vaccinations.first().doseNumber.toString(),
                    certificate.vaccinations.first().totalSeriesOfDoses.toString()
                )
                certificate.recoveryStatements?.isNotEmpty() == true -> binding.root.resources.getString(R.string.recovery)
                certificate.tests?.isNotEmpty() == true -> binding.root.resources.getString(R.string.test)
                else -> ""
            }

            binding.qrCode.setImageBitmap(it.qrCode)
            binding.tan.text = getString(R.string.tan_placeholder, it.certificatesCard.tan)
            showUserData(certificate)
            adapter.update(certificate.getCertificateListData())
        })
        viewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
        viewModel.setCertificateId(args.certificateId, minEdge.toInt())
        binding.checkValidity.setOnClickListener {
            viewModel.certificate.value?.certificatesCard?.qrCodeText?.let {
                val action = ViewCertificateFragmentDirections.actionViewCertificateFragmentToCertificateValidityFragment(it)
                findNavController().navigate(action)
            }
        }
        binding.shareImage.setOnClickListener { viewModel.shareImage(requireContext().filesDir) }
        binding.sharePdf.setOnClickListener { viewModel.sharePdf(requireContext().filesDir) }
        viewModel.shareImageFile.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (it is FilePreparationResult.FileResult) {
                    launchImageSharing(it.file)
                } else {
                    showFilePreparationError()
                }
            }
        }

        viewModel.sharePdfFile.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (it is FilePreparationResult.FileResult) {
                    launchPdfImageSharing(it.file)
                } else {
                    showFilePreparationError()
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.findItem(R.id.delete).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete) {
            viewModel.deleteCert(args.certificateId)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun launchImageSharing(fileForSharing: File) {
        startActivity(
            Intent.createChooser(
                shareImageIntentProvider.getShareImageIntent(fileForSharing),
                getString(R.string.share_image_title)
            )
        )
    }

    private fun launchPdfImageSharing(fileForSharing: File) {
        startActivity(
            Intent.createChooser(
                shareImageIntentProvider.getShareImageIntent(fileForSharing),
                getString(R.string.share_pdf_title)
            )
        )
    }

    private fun showFilePreparationError() {
        Toast.makeText(requireContext(), R.string.file_preparation_error, Toast.LENGTH_SHORT).show()
    }

    private fun showUserData(certificate: CertificateModel) {
        certificate.getFullName().bindText(binding.nameTitle, binding.personFullName)

//        val dateOfBirthday = certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
//        if (dateOfBirthday.isNotBlank()) {
//            binding.dateOfBirth.text = dateOfBirthday
//            View.VISIBLE
//        } else {
//            View.GONE
//        }.apply {
//            binding.dateOfBirthTitle.visibility = this
//            binding.dateOfBirth.visibility = this
//        }
    }

    private fun onViewModelEvent(event: ViewCertificateViewModel.ViewCertEvent) {
        when (event) {
            is ViewCertificateViewModel.ViewCertEvent.OnCertDeleted -> findNavController().popBackStack()
        }
    }
}