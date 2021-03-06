/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-wallet-app-android
 *  ---
 *  Copyright (C) 2022 T-Systems International GmbH and all other contributors
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
 *  Created by mykhailo.nester on 06/04/2022, 13:48
 */

package dgca.wallet.app.android.vc.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.vc.R
import com.android.app.vc.databinding.FragmentVcVerificationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.vc.getStringFromJsonFile
import dgca.wallet.app.android.vc.model.DataItem
import dgca.wallet.app.android.vc.replaceKnownTypes

@AndroidEntryPoint
class VcVerificationFragment : BindingFragment<FragmentVcVerificationBinding>() {

    private lateinit var adapter: VcAdapter

    private val viewModel by viewModels<VcViewModel>()
    private val args by navArgs<VcVerificationFragmentArgs>()

    private var isQrValid: Boolean = false
    private var isRawExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = VcAdapter(layoutInflater)
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentVcVerificationBinding =
        FragmentVcVerificationBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contextJson = requireContext().getStringFromJsonFile(R.raw.vc_context_example)
        viewModel.setContextJson(contextJson)
        viewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }

        viewModel.validate(args.qrCodeText)

        binding.actionBtn.setOnClickListener {
            if (isQrValid) {
                viewModel.saveItem()
            } else {
                requireActivity().finish()
            }
        }
        binding.expandButton.setOnClickListener {
            isRawExpanded = !isRawExpanded
            binding.expandButton.setImageResource(if (isRawExpanded) R.drawable.ic_icon_minus else R.drawable.ic_icon_plus)
            binding.vcRawData.isVisible = isRawExpanded
        }

        binding.mainContentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.mainContentRecyclerView.adapter = adapter
    }

    private fun onViewModelEvent(event: VcViewModel.ViewEvent) {
        when (event) {
            is VcViewModel.ViewEvent.OnError -> handleError(event.type, event.rawPayloadData)
            is VcViewModel.ViewEvent.OnVerified -> showVerified(
                event.headers,
                event.payloadItems,
                event.json
            )
            is VcViewModel.ViewEvent.OnIssuerNotTrusted -> showConfirmationDialog(event.issuerDomain)
            is VcViewModel.ViewEvent.OnSaveEvent -> if (event.isSaved) {
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Failed to save item, please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleError(error: VcViewModel.ErrorType, rawPayloadData: String) {
        isQrValid = false
        val errorText = when (error) {
            VcViewModel.ErrorType.JWS_STRUCTURE_NOT_VALID -> "JWS structure not valid"
            VcViewModel.ErrorType.KID_NOT_INCLUDED -> "Key id (KID) not included"
            VcViewModel.ErrorType.ISSUER_NOT_RECOGNIZED -> "Issuer not recognized"
            VcViewModel.ErrorType.ISSUER_NOT_INCLUDED -> "Issuer not included"
            VcViewModel.ErrorType.TIME_BEFORE_NBF -> "Time before issuance date"
            VcViewModel.ErrorType.VC_EXPIRED -> "Verifiable credential expired"
            VcViewModel.ErrorType.NO_JWK_FOR_KID -> "JWK not found for this KID"
            VcViewModel.ErrorType.INVALID_SIGNATURE -> "Invalid signature"
            VcViewModel.ErrorType.PAYLOAD_NOT_PARSED -> "Failed to parse payload"
        }

        binding.rawDataContainer.isVisible = rawPayloadData.isNotEmpty()
        binding.vcRawData.text = rawPayloadData
        binding.progressBar.isVisible = false
        binding.statusDetailed.isVisible = true
        binding.statusDetailed.text = errorText
        binding.statusViews.isVisible = true

        binding.actionBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
        binding.actionBtn.text = getString(R.string.close)
        binding.actionBtn.isVisible = true
    }

    @SuppressLint("SetTextI18n")
    private fun showVerified(headers: MutableList<DataItem>, payloadItems: List<DataItem>, rawJson: String) {
        isQrValid = true
        addHeaders(headers)
        adapter.update(payloadItems)

        binding.progressBar.isVisible = false
        binding.rawDataContainer.isVisible = true
        binding.vcRawData.text = rawJson
        binding.statusViews.isVisible = true

        binding.actionBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
        binding.actionBtn.text = getString(R.string.save)
        binding.actionBtn.isVisible = true
    }

    private fun addHeaders(headers: MutableList<DataItem>) {
        if (headers.isEmpty()) {
            return
        }

        binding.headers.isVisible = true
        headers.forEach { header ->
            val viewHeader = layoutInflater.inflate(R.layout.header_title, binding.headers, false)
            viewHeader.findViewById<TextView>(R.id.vc_header).text = header.title
            binding.headers.addView(viewHeader)
            val viewValue = layoutInflater.inflate(R.layout.header_value, binding.headers, false)
            var result = ""
            var list = header.value
            if (header.title.contains("type", true)) {
                list = list.replaceKnownTypes()
            }

            list.forEach { result += "$it " }
            viewValue.findViewById<TextView>(R.id.vc_header_value).text = result
            binding.headers.addView(viewValue)
        }
    }

    private fun showConfirmationDialog(issuerDomain: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.issuer_not_trusted, issuerDomain))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.approve)) { dialog, _ ->
                viewModel.issuerApproved()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish()
            }
            .show()
    }
}

