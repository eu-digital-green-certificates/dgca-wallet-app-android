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
 *  Created by mykhailo.nester on 14/09/2021, 19:54
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.certselector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.Event
import dgca.wallet.app.android.model.AccessTokenResult
import dgca.wallet.app.android.wallet.CertificatesCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CertificatesContainer(
    val selectedCertificate: CertificatesCard? = null,
    val selectableCertificateModelList: List<SelectableCertificateModel>
)

@HiltViewModel
class CertificateSelectorViewModel @Inject constructor(
    private val getFilteredCertificatesUseCase: GetFilteredCertificatesUseCase
) : ViewModel() {

    private val _certificatesContainer = MutableLiveData<CertificatesContainer>()
    val certificatesContainer: LiveData<CertificatesContainer> = _certificatesContainer

    private val _uiEvent = MutableLiveData<Event<CertificateViewUiEvent>>()
    val uiEvent: LiveData<Event<CertificateViewUiEvent>> = _uiEvent

    private val _event = MutableLiveData<Event<CertificateEvent>>()
    val event: LiveData<Event<CertificateEvent>> = _event

    private lateinit var certificateList: List<CertificatesCard.CertificateCard>
    private var selected: SelectableCertificateModel? = null

    fun init(accessTokenResult: AccessTokenResult) {
        getCertificates(accessTokenResult)
    }

    private fun getCertificates(accessTokenResult: AccessTokenResult) {
        viewModelScope.launch {
            _uiEvent.value = Event(CertificateViewUiEvent.OnShowLoading)

            withContext(Dispatchers.IO) {
                certificateList = getFilteredCertificatesUseCase.run(accessTokenResult)
            }

            _certificatesContainer.value = CertificatesContainer(null, certificateList.toSelectableCertificateModelList())
            _uiEvent.value = Event(CertificateViewUiEvent.OnHideLoading)
        }
    }

    fun onCertificateSelected(position: Int) {
        val certificatesContainer: CertificatesContainer = _certificatesContainer.value!!
        val list: MutableList<SelectableCertificateModel> =
            certificatesContainer.selectableCertificateModelList.toMutableList()
        val selectedCertificateCard: CertificatesCard.CertificateCard = certificateList[position]
        selected?.let { model ->
            val index = list.indexOfFirst { model == it }
            if (index != -1) {
                list[index] = model.copy(selected = false)
            }
        }

        val copy = list[position].copy(selected = true)
        selected = copy
        list[position] = copy

        _certificatesContainer.value = CertificatesContainer(selectedCertificateCard, list.toList())
    }

    fun onNextClick() {
        viewModelScope.launch {
            _uiEvent.value = Event(CertificateViewUiEvent.OnShowLoading)

            withContext(Dispatchers.IO) {
                // TODO: API Calls
                delay(1500)
            }
            _uiEvent.value = Event(CertificateViewUiEvent.OnHideLoading)
            selected?.let {
                _event.value = Event(CertificateEvent.OnCertificateAdvisorSelected(it))
            }
        }
    }

    private fun List<CertificatesCard.CertificateCard>.toSelectableCertificateModelList(): List<SelectableCertificateModel> {
        val selectableCertificateModelList = mutableListOf<SelectableCertificateModel>()
        forEach { certificateCard ->
            selectableCertificateModelList.add(
                SelectableCertificateModel(
                    certificateCard.certificateId.toString(),
                    certificateCard,
                    false
                )
            )
        }
        return selectableCertificateModelList
    }

    sealed class CertificateViewUiEvent {
        object OnShowLoading : CertificateViewUiEvent()
        object OnHideLoading : CertificateViewUiEvent()
        object OnError : CertificateViewUiEvent()
    }

    sealed class CertificateEvent {
        data class OnCertificateAdvisorSelected(val certModel: SelectableCertificateModel) : CertificateEvent()
    }
}