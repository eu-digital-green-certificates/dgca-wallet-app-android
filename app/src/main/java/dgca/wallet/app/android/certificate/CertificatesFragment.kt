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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dgca.wallet.app.android.databinding.FragmentCertificatesBinding
import java.time.LocalDate

class CertificatesFragment : Fragment(), CertificateCardsAdapter.CertificateCardClickListener {
    private var _binding: FragmentCertificatesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertificatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.fab.setOnClickListener {
            val action = CertificatesFragmentDirections.actionCertificatesFragmentToCodeReaderFragment()
            findNavController().navigate(action)
        }

        val certificateCards = mutableListOf(
            CertificateCard("Title1", "Name1", "Surname1", LocalDate.now(), "qrCodeText"),
            CertificateCard("Title1", "Name1", "Surname1", LocalDate.now(), "qrCodeText"),
            CertificateCard("Title1", "Name1", "Surname1", LocalDate.now(), "qrCodeText"),
            CertificateCard("Title1", "Name1", "Surname1", LocalDate.now(), "qrCodeText"),
            CertificateCard("Title1", "Name1", "Surname1", LocalDate.now(), "qrCodeText"),
            CertificateCard("Title1", "Name1", "Surname1", LocalDate.now(), "qrCodeText")
        )
        setCertificateCards(certificateCards)
    }

    private fun setCertificateCards(certificateCards: List<CertificateCard>) {
        if (certificateCards.isNotEmpty()) {
            binding.certificatesView.setHasFixedSize(true)
            binding.certificatesView.layoutManager = LinearLayoutManager(requireContext())
            binding.certificatesView.adapter = CertificateCardsAdapter(certificateCards, this)
            binding.certificatesView.visibility = View.VISIBLE

            binding.noCertificatesView.visibility = View.GONE
        }
    }

    override fun onCertificateCardClick(qrCodeText: String) {
        val action = CertificatesFragmentDirections.actionCertificatesFragmentToViewCertificateFragment(qrCodeText)
        findNavController().navigate(action)
    }
}