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
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.MainActivity
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.FragmentCertificatesBinding
import dgca.wallet.app.android.nfc.NdefParser

//    TODO: code cleanup move part of logic to viewmodel
@AndroidEntryPoint
class CertificatesFragment : Fragment(), CertificateCardsAdapter.CertificateCardClickListener {

    private val viewModel by viewModels<CertificatesViewModel>()
    private var _binding: FragmentCertificatesBinding? = null
    private val binding get() = _binding!!

    private lateinit var nfcDialog: AlertDialog
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

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

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { requireActivity().finish() }
        binding.scanCode.setOnClickListener {
            val action = CertificatesFragmentDirections.actionCertificatesFragmentToCodeReaderFragment()
            findNavController().navigate(action)
        }

        viewModel.certificates.observe(viewLifecycleOwner, {
            setCertificateCards(it)
        })
        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.progressView.isVisible = it
        })
        viewModel.fetchCertificates()

        binding.nfcImport.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                initNfcReader()
            } else {
                nfcAdapter?.disableForegroundDispatch(requireActivity())
                binding.nfcStatus.text = "NFC reader off"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.nfcImport.isChecked) {
            if (nfcAdapter?.isEnabled == true) {
                initNfcReader()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(requireActivity())
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

    override fun onCertificateCardClick(certificateId: Int) {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToViewCertificateFragment(certificateId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initNfcReader() {
        if (checkNFCEnable()) {
            pendingIntent = PendingIntent.getActivity(
                requireContext(), 0,
                Intent(
                    requireContext(),
                    this.javaClass
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            )
            nfcAdapter?.enableForegroundDispatch(requireActivity(), pendingIntent, null, null)
            binding.nfcStatus.text = "NFC reader ready"
        } else {
            binding.nfcStatus.text = getString(R.string.no_nfc)
        }
    }

    fun parserNDEFMessage(messages: List<NdefMessage>) {
        val builder = StringBuilder()
        val records = NdefParser.parse(messages[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str).append("\n")
        }
        Handler().postDelayed({
            binding.nfcStatus.text = builder.toString()
        }, 500)
    }

    private fun checkNFCEnable(): Boolean {
        return if (nfcAdapter == null) {
            binding.nfcStatus.text = getString(R.string.no_nfc)
            false
        } else {
            nfcAdapter!!.isEnabled
        }
    }
}