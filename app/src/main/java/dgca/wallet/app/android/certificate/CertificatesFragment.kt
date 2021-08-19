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

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.MainActivity
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.certificate.claim.ClaimCertificateViewModel
import dgca.wallet.app.android.databinding.FragmentCertificatesBinding
import dgca.wallet.app.android.nfc.DCCApduService
import dgca.wallet.app.android.nfc.NdefParser
import dgca.wallet.app.android.nfc.showTurnOnNfcDialog

@AndroidEntryPoint
class CertificatesFragment : BindingFragment<FragmentCertificatesBinding>(),
    CertificateCardsAdapter.CertificateCardClickListener {

    private val viewModel by viewModels<CertificatesViewModel>()
    private val claimCertViewModel by viewModels<ClaimCertificateViewModel>()

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).clearBackground()
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCertificatesBinding =
        FragmentCertificatesBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).disableBackButton()
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { requireActivity().finish() }

        binding.scanCode.setOnClickListener {
            val action = CertificatesFragmentDirections.actionCertificatesFragmentToCodeReaderFragment()
            findNavController().navigate(action)
        }
        binding.nfcImport.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                initNfcReader()
            } else {
                stopNfcReader()
            }
        }

        viewModel.certificates.observe(viewLifecycleOwner, { setCertificateCards(it) })
        viewModel.inProgress.observe(viewLifecycleOwner, { binding.progressView.isVisible = it })
        claimCertViewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { onViewModelEvent(it) }
        }

        viewModel.fetchCertificates()
    }

    override fun onPause() {
        super.onPause()
        stopNfcReader()
    }

    override fun onCertificateCardClick(certificateId: Int) {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToViewCertificateFragment(certificateId)
        findNavController().navigate(action)
    }

    fun onNdefMessagesReceived(messages: List<NdefMessage>) {
        if (messages.isEmpty()) {
            return
        }

        val builder = StringBuilder()
        val records = NdefParser.parse(messages[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str)
        }

        val result = builder.toString()
        val qrCodeText = result.substring(DCCApduService.NFC_TAG_DCC.length, result.indexOf(DCCApduService.NFC_TAG_TAN))
        val tan = result.substring(result.indexOf(DCCApduService.NFC_TAG_TAN) + DCCApduService.NFC_TAG_TAN.length, result.length)

        claimCertViewModel.verifyAndStore(qrCodeText, tan)
    }

    private fun setCertificateCards(certificateCards: List<CertificateCard>) {
        if (certificateCards.isNotEmpty()) {
            binding.certificatesView.setHasFixedSize(true)
            binding.certificatesView.layoutManager = LinearLayoutManager(requireContext())
            binding.certificatesView.adapter = CertificateCardsAdapter(certificateCards, this)
            binding.certificatesView.visibility = View.VISIBLE

            binding.noAvailableOffersGroup.visibility = View.GONE
        } else {
            binding.certificatesView.visibility = View.GONE
            binding.noAvailableOffersGroup.visibility = View.VISIBLE
        }
    }

    private fun initNfcReader() {
        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
            binding.nfcStatus.text = getString(R.string.no_nfc)
            return
        }

        if (nfcAdapter?.isEnabled == true) {
            pendingIntent = PendingIntent.getActivity(
                requireContext(), 0,
                Intent(requireContext(), this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            )
            nfcAdapter?.enableForegroundDispatch(requireActivity(), pendingIntent, null, null)
            binding.nfcStatus.text = getString(R.string.nfc_import_reader_enabled)
        } else {
            showTurnOnNfcDialog()
        }
    }

    private fun stopNfcReader() {
        binding.nfcImport.isChecked = false
        binding.nfcStatus.text = getString(R.string.nfc_import_reader_disabled)
        nfcAdapter?.disableForegroundDispatch(requireActivity())
    }

    private fun onViewModelEvent(event: ClaimCertificateViewModel.ClaimCertEvent) {
        when (event) {
            is ClaimCertificateViewModel.ClaimCertEvent.OnCertClaimed -> {
                if (event.isClaimed) {
                    viewModel.fetchCertificates()
                    Toast.makeText(requireContext(), "Certificate claimed", Toast.LENGTH_SHORT).show()
                }
            }
            is ClaimCertificateViewModel.ClaimCertEvent.OnCertNotClaimed ->
                Toast.makeText(requireContext(), getString(R.string.check_the_tan_and_try_again), Toast.LENGTH_SHORT).show()
        }
    }
}