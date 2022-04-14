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
 *  Created by mykhailo.nester on 14/09/2021, 20:45
 */

package feature.ticketing.presentation.certselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.dcc.R
import com.android.app.dcc.databinding.FragmentCertificateSelectorBinding
import dgca.wallet.app.android.dcc.ui.BindingFragment
import dgca.wallet.app.android.dcc.utils.YEAR_MONTH_DAY
import dagger.hilt.android.AndroidEntryPoint
import feature.ticketing.presentation.accesstoken.TYPE_FULL
import feature.ticketing.presentation.accesstoken.TYPE_NOTHING
import feature.ticketing.presentation.accesstoken.TYPE_PARTIAL
import feature.ticketing.presentation.accesstoken.TicketingAccessTokenParcelable
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class CertificateSelectorFragment : BindingFragment<FragmentCertificateSelectorBinding>() {

    private val args by navArgs<CertificateSelectorFragmentArgs>()
    private val viewModel by viewModels<CertificateSelectorViewModel>()

    private lateinit var adapter: CertificateSelectorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        (activity as MainActivity).clearBackground()  // TODO: update

        adapter = CertificateSelectorAdapter(layoutInflater, viewModel)
        viewModel.event.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
        viewModel.init(args.bookingPortalEncryptionData)
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCertificateSelectorBinding =
        FragmentCertificateSelectorBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        viewModel.uiEvent.observe(viewLifecycleOwner) { event -> onViewModelUiEvent(event.peekContent()) }
        viewModel.certificatesContainer.observe(viewLifecycleOwner) {
            binding.title.text = resources.getQuantityString(
                R.plurals.certificates_found_title,
                it.selectableCertificateModelList.size,
                it.selectableCertificateModelList.size.toString(),
                args.bookingPortalEncryptionData.getStandardizedName()
            )
            binding.title.visibility = View.VISIBLE
            adapter.update(it.selectableCertificateModelList)
            if (it.selectedCertificate != null) {
                binding.nextButton.visibility = View.VISIBLE
            }
        }
        binding.nextButton.setOnClickListener {
            viewModel.onNextClick()
        }

        setUp(args.bookingPortalEncryptionData.accessTokenContainer.accessToken)
    }

    private fun onViewModelUiEvent(event: CertificateSelectorViewModel.CertificateViewUiEvent) {
        when (event) {
            CertificateSelectorViewModel.CertificateViewUiEvent.OnHideLoading -> binding.progressView.isVisible = false
            CertificateSelectorViewModel.CertificateViewUiEvent.OnShowLoading -> binding.progressView.isVisible = true
            CertificateSelectorViewModel.CertificateViewUiEvent.OnError -> {
            }
        }
    }

    private fun onViewModelEvent(event: CertificateSelectorViewModel.CertificateEvent) {
        when (event) {
            is CertificateSelectorViewModel.CertificateEvent.OnCertificateAdvisorSelected -> {
                val action =
                    CertificateSelectorFragmentDirections.actionCertificateSelectorFragmentToTransmissionConsentFragment(
                        args.bookingPortalEncryptionData,
                        event.certModel.filteredCertificateCard.certificateCard.certificate,
                        event.certModel.filteredCertificateCard.validTo,
                        event.certModel.filteredCertificateCard.certificateCard.qrCodeText
                    )
                findNavController().navigate(action)
            }
        }
    }

    private val certificateCodeType: Map<String, Int> =
        mapOf("r" to R.string.recovery, "v" to R.string.vaccination, "t" to R.string.test)

    private fun setUp(ticketingAccessTokenDataParcelable: TicketingAccessTokenParcelable) {
        val certificateData = ticketingAccessTokenDataParcelable.certificateData
        val name: String?
        val dateOfBirth: String?
        val departure: String?
        val arrival: String?
        val acceptedCertificateType: String?
        val category: String?
        val validationTime: String?
        val validFrom: String?
        val validTo: String?
        when (ticketingAccessTokenDataParcelable.type) {
            TYPE_FULL -> {
                name =
                    "${certificateData.standardizedGivenName} ${certificateData.standardizedFamilyName}".trim()
                dateOfBirth = certificateData.dateOfBirth
                departure = "${certificateData.cod}, ${certificateData.rod}"
                arrival = "${certificateData.coa}, ${certificateData.roa}"
                acceptedCertificateType = certificateData.greenCertificateTypes
                    .map {
                        val id = certificateCodeType[it]
                        return@map if (id != null && id > 0) getString(id) else it
                    }
                    .joinToString(separator = ", ")
                category = certificateData.category.joinToString(separator = ", ")
                validationTime =
                    DateTimeFormatter.ofPattern(YEAR_MONTH_DAY, Locale.US).format(certificateData.validationClock.toLocalDate())
                validFrom = DateTimeFormatter.ofPattern(YEAR_MONTH_DAY, Locale.US).format(certificateData.validFrom.toLocalDate())
                validTo = DateTimeFormatter.ofPattern(YEAR_MONTH_DAY, Locale.US).format(certificateData.validTo.toLocalDate())
            }
            TYPE_PARTIAL -> {
                name =
                    "${certificateData.standardizedGivenName} ${certificateData.standardizedFamilyName}".trim()
                dateOfBirth = certificateData.dateOfBirth
                departure = null
                arrival = null
                acceptedCertificateType = null
                category = null
                validationTime = null
                validFrom = null
                validTo = null
            }
            TYPE_NOTHING -> {
                name = null
                dateOfBirth = null
                departure = null
                arrival = null
                acceptedCertificateType = null
                category = null
                validationTime = null
                validFrom = null
                validTo = null
            }
            else -> throw IllegalArgumentException("Type may be from 0 to 2")
        }

        setValue(binding.standardizedNameTitle, binding.standardizedNameValue, name)
        setValue(binding.dateOfBirthTitle, binding.dateOfBirthValue, dateOfBirth)
        setValue(binding.departureTitle, binding.departureValue, departure)
        setValue(binding.arrivalTitle, binding.arrivalValue, arrival)
        setValue(binding.acceptedCertificateTypeTitle, binding.acceptedCertificateTypeValue, acceptedCertificateType)
        setValue(binding.categoryTitle, binding.categoryValue, category)
        setValue(binding.validationTimeTitle, binding.validationTimeValue, validationTime)
        setValue(binding.validFromTitle, binding.validFromValue, validFrom)
        setValue(binding.validToTitle, binding.validToValue, validTo)
    }

    fun setValue(titleView: TextView, valueView: TextView, value: String?) {
        if (value?.isNotBlank() == true) {
            valueView.text = value
            View.VISIBLE
        } else {
            View.GONE
        }.let {
            titleView.visibility = it
            valueView.visibility = it
        }
    }
}