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
 *  Created by osarapulov on 8/23/21 1:54 PM
 */

package dgca.wallet.app.android.vc.ui.certview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.vc.R
import com.android.app.vc.databinding.FragmentVcCertificateViewBinding
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.vc.model.DataItem
import dgca.wallet.app.android.vc.replaceKnownTypes
import dgca.wallet.app.android.vc.ui.BindingFragment
import dgca.wallet.app.android.vc.ui.VcAdapter

@AndroidEntryPoint
class VcViewCertificateFragment : BindingFragment<FragmentVcCertificateViewBinding>() {

    private lateinit var adapter: VcAdapter

    private val args by navArgs<VcViewCertificateFragmentArgs>()
    private val viewModel by viewModels<ViewCertificateViewModel>()

    private var isRawExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = VcAdapter(layoutInflater)
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVcCertificateViewBinding = FragmentVcCertificateViewBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back)
        binding.toolbar.setNavigationOnClickListener { requireActivity().finish() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.vc_settings -> {
                    val direction = VcViewCertificateFragmentDirections.actionVcViewCertificateFragmentToSettingsFragment()
                    findNavController().navigate(direction)
                }
                R.id.delete -> {
                    binding.progressBar.isVisible = true
                    viewModel.deleteItem(args.certificateId)
                }
            }

            true
        }

        binding.expandButton.setOnClickListener {
            isRawExpanded = !isRawExpanded
            binding.expandButton.setImageResource(if (isRawExpanded) R.drawable.ic_icon_minus else R.drawable.ic_icon_plus)
            binding.vcRawData.isVisible = isRawExpanded
        }
        binding.mainContentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.mainContentRecyclerView.adapter = adapter

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
        viewModel.init(args.certificateId)
    }

    private fun onViewModelEvent(event: ViewCertificateViewModel.ViewEvent) {
        when (event) {
            is ViewCertificateViewModel.ViewEvent.OnItemAvailable -> setupUi(event.headers, event.payloadItems, event.json)
            ViewCertificateViewModel.ViewEvent.OnItemNotFound -> {
                Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            is ViewCertificateViewModel.ViewEvent.OnDeleteItem -> {
                if (!event.isDeleted) {
                    Toast.makeText(requireContext(), "Item not deleted", Toast.LENGTH_SHORT).show()
                }
                requireActivity().finish()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUi(headers: MutableList<DataItem>, payloadItems: List<DataItem>, rawJson: String) {
        addHeaders(headers)
        adapter.update(payloadItems)

        binding.progressBar.isVisible = false
        binding.rawDataContainer.isVisible = true
        binding.vcRawData.text = rawJson
        binding.statusViews.isVisible = true
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
}