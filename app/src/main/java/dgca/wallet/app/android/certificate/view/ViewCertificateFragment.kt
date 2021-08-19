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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.certificate.claim.CertListAdapter
import dgca.wallet.app.android.certificate.claim.bindText
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.getCertificateListData
import dgca.wallet.app.android.databinding.FragmentCertificateViewBinding
import dgca.wallet.app.android.nfc.DCCApduService
import dgca.wallet.app.android.nfc.DCCApduService.Companion.NFC_NDEF_KEY
import dgca.wallet.app.android.nfc.DCCApduService.Companion.NFC_TAG_DCC
import dgca.wallet.app.android.nfc.DCCApduService.Companion.NFC_TAG_TAN
import dgca.wallet.app.android.nfc.showTurnOnNfcDialog
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ViewCertificateFragment : BindingFragment<FragmentCertificateViewBinding>() {

    private val args by navArgs<ViewCertificateFragmentArgs>()
    private val viewModel by viewModels<ViewCertificateViewModel>()

    private lateinit var adapter: CertListAdapter
    private var nfcAdapter: NfcAdapter? = null

    @Inject
    lateinit var shareImageIntentProvider: ShareImageIntentProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        adapter = CertListAdapter(layoutInflater)
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCertificateViewBinding =
        FragmentCertificateViewBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val minEdge = displayMetrics.widthPixels * 0.9

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        viewModel.inProgress.observe(viewLifecycleOwner, { binding.progressView.isVisible = it })
        viewModel.certificate.observe(viewLifecycleOwner, {
            val certificate = it.certificateCard.certificate
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
            binding.tan.text = getString(R.string.tan_placeholder, it.certificateCard.tan)
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
            viewModel.certificate.value?.certificateCard?.qrCodeText?.let {
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

        binding.nfcAction.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                initNFCFunction()
            } else {
                stopNfcService()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopNfcService()
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

    private fun initNFCFunction() {
        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
            binding.nfcStatus.text = getString(R.string.no_nfc)
            return
        }

        if (nfcAdapter?.isEnabled == true) {
            initNfcService()
        } else {
            showTurnOnNfcDialog()
        }
    }

    private fun initNfcService() {
        val certificate = viewModel.certificate.value?.certificateCard
        val intent = Intent(requireContext(), DCCApduService::class.java)
        val qrData = NFC_TAG_DCC + "${certificate?.qrCodeText}" + NFC_TAG_TAN + "${certificate?.tan}"
        intent.putExtra(NFC_NDEF_KEY, qrData)
        requireContext().startService(intent)


        val filter = IntentFilter(NFC_BROADCAST)
        requireContext().registerReceiver(nfcReceiver, filter)
    }

    private fun stopNfcService() {
        if (nfcAdapter?.isEnabled == true) {
            requireContext().stopService(Intent(requireContext(), DCCApduService::class.java))
        }
        binding.nfcAction.isChecked = false
        try {
            requireContext().unregisterReceiver(nfcReceiver)
        } catch (ex: Exception) {
            Timber.d("nfcReceiver not registered.")
        }
    }

    private val nfcReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.hasExtra(NFC_EXTRA_DCC_SENT)) {
                if (intent.getBooleanExtra(NFC_EXTRA_DCC_SENT, false)) {
                    viewModel.onCertificateShared()
                    Toast.makeText(requireContext(), "DCC was sent successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val NFC_BROADCAST = "dgca.wallet.app.android.certificate.view.nfc_broadcast"
        const val NFC_EXTRA_DCC_SENT = "nfc_dcc_sent"
    }
}