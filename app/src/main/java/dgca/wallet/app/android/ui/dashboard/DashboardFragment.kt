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
 *  Created by osarapulov on 8/25/21 4:40 PM
 */

package dgca.wallet.app.android.ui.dashboard

import android.content.Intent
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
import com.android.app.base.ProcessorItemCard
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentDashboardBinding
import dgca.wallet.app.android.inputrecognizer.INPUT_RECOGNISER_DATA_KEY
import dgca.wallet.app.android.inputrecognizer.INPUT_RECOGNISER_DATA_REQUEST_KEY
import dgca.wallet.app.android.inputrecognizer.file.image.ImportImageDialogFragment
import dgca.wallet.app.android.protocolhandler.PROTOCOL_HANDLER_REQUEST_KEY
import dgca.wallet.app.android.protocolhandler.PROTOCOL_HANDLER_RESULT_KEY
import dgca.wallet.app.android.ui.*
import java.io.File

@AndroidEntryPoint
class DashboardFragment : BindingFragment<FragmentDashboardBinding>() {

    private val viewModel by viewModels<DashboardViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).clearBackground()
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDashboardBinding =
        FragmentDashboardBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { requireActivity().finish() }
        binding.scanCode.setOnClickListener { showAddNewDialog() }
        binding.certificatesList.setHasFixedSize(true)
        binding.certificatesList.layoutManager = LinearLayoutManager(requireContext())

        setFragmentResultListener(INPUT_RECOGNISER_DATA_REQUEST_KEY) { _, bundle ->
            bundle.getString(INPUT_RECOGNISER_DATA_KEY)?.let {
                findNavController().navigate(
                    DashboardFragmentDirections.actionCertificatesFragmentToProtocolHandlerDialogFragment(it)
                )
            }
        }

        setFragmentResultListener(PROTOCOL_HANDLER_REQUEST_KEY) { _, bundle ->
            findNavController().navigateUp()
            bundle.getParcelable<Intent>(PROTOCOL_HANDLER_RESULT_KEY)?.let { startActivityForResult(it, 0x0) }
        }

        viewModel.itemCards.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.certificatesList.visibility = View.GONE
                binding.noAvailableOffersGroup.visibility = View.VISIBLE
            } else {
                binding.certificatesList.adapter = DashboardAdapter(layoutInflater, viewModel, it)
                binding.certificatesList.visibility = View.VISIBLE
                binding.noAvailableOffersGroup.visibility = View.GONE
            }
        }
        viewModel.inProgress.observe(viewLifecycleOwner) { binding.progressBar.isVisible = it }

        setFragmentResultListener(AddNewBottomDialogFragment.REQUEST_KEY) { key, bundle ->
            findNavController().navigateUp()
            when (bundle.getInt(AddNewBottomDialogFragment.RESULT_KEY)) {
                AddNewBottomDialogFragment.RESULT_SCAN_CODE -> showScanQrCode()
                AddNewBottomDialogFragment.RESULT_NFC -> showNfcScan()
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
        setFragmentResultListener(ADD_REQUEST_KEY) { _, bundle ->
            bundle.getParcelable<Intent>(CERTIFICATE_MODEL_KEY)?.let {
                startActivity(it)
            }
        }

        setFragmentResultListener(DELETE_CERTIFICATE_REQUEST_KEY) { _, bundle ->
            val itemPosition = bundle.getInt(DELETE_CERTIFICATE_ITEM_POSITION_RESULT_PARAM)
            val itemCard: ProcessorItemCard = bundle.getParcelable(
                DELETE_CERTIFICATE_ITEM_CARD_RESULT_PARAM
            )!!
            viewModel.deleteCertificate(itemPosition, itemCard)
        }

        setFragmentResultListener(DELETE_FILE_REQUEST_KEY) { _, bundle ->
            val position = bundle.getInt(DELETE_FILE_POSITION_RESULT_PARAM)
            val file: File = bundle.getSerializable(DELETE_FILE_FILE_RESULT_PARAM) as File
            viewModel.deleteFile(position, file)
        }

        viewModel.refresh(requireContext().filesDir)
        viewModel.certificateViewEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.refresh(requireContext().filesDir)
    }

    private fun showDeleteCertificateConfirmationDialog(position: Int, itemCard: ProcessorItemCard) {
        val action = DashboardFragmentDirections.actionCertificatesFragmentToDeleteCertificateDialogFragment(position, itemCard)
        findNavController().navigate(action)
    }

    private fun showDeleteFileConfirmationDialog(position: Int, file: File) {
        val action = DashboardFragmentDirections.actionCertificatesFragmentToDeleteFileDialogFragment(position, file)
        findNavController().navigate(action)
    }

    private fun onViewModelEvent(event: DashboardViewModel.CertificateViewEvent) {
        when (event) {
            is DashboardViewModel.CertificateViewEvent.OnCertificateSelected -> onCertificateCardClick(event.intent)
            is DashboardViewModel.CertificateViewEvent.OnFileSelected -> onFileCardClick(event.file)
            is DashboardViewModel.CertificateViewEvent.OnShowDeleteCertificate -> showDeleteCertificateConfirmationDialog(
                event.itemPosition,
                event.itemCard
            )
            is DashboardViewModel.CertificateViewEvent.OnShowDeleteFile -> showDeleteFileConfirmationDialog(
                event.position,
                event.file
            )
        }
    }

    private fun showScanQrCode() {
        findNavController().navigate(DashboardFragmentDirections.actionCertificatesFragmentToQrReaderFragment())
    }

    private fun showNfcScan() {
        findNavController().navigate(DashboardFragmentDirections.actionCertificatesFragmentToNfcFragment())
    }

    private fun showImportImage() {
        findNavController().navigate(DashboardFragmentDirections.actionCertificatesFragmentToImagePhotoDialogFragment())
    }

    private fun showAddNewDialog() {
        val action = DashboardFragmentDirections.actionCertificatesFragmentToAddNewDialogFragment()
        findNavController().navigate(action)
    }

    private fun showTakePhoto() {
        val action = DashboardFragmentDirections.actionCertificatesFragmentToTakePhotoFragment()
        findNavController().navigate(action)
    }

    private fun showPickImage() {
        val action = DashboardFragmentDirections.actionCertificatesFragmentToPickImageFragment()
        findNavController().navigate(action)
    }

    private fun showImportPdf() {
        val action = DashboardFragmentDirections.actionCertificatesFragmentToImportPdfFragment()
        findNavController().navigate(action)
    }

    private fun onCertificateCardClick(intent: Intent) {
        startActivityForResult(intent, 0x0)
    }

    private fun onFileCardClick(file: File) {
        val action = DashboardFragmentDirections.actionCertificatesFragmentToViewFileFragment(file)
        findNavController().navigate(action)
    }
}
