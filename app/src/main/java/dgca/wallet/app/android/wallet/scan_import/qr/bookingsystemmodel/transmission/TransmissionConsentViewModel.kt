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
 *  Created by mykhailo.nester on 14/09/2021, 20:46
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.Event
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.model.AccessTokenResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TransmissionConsentViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiEvent = MutableLiveData<Event<TransmissionConsentUiEvent>>()
    val uiEvent: LiveData<Event<TransmissionConsentUiEvent>> = _uiEvent

    private val _event = MutableLiveData<Event<TransmissionConsentEvent>>()
    val event: LiveData<Event<TransmissionConsentEvent>> = _event

    fun init() {

    }

    fun onPermissionAccepted(qrString: String, accessTokenResult: AccessTokenResult) {
        viewModelScope.launch {
            _uiEvent.value = Event(TransmissionConsentUiEvent.OnShowLoading)

            withContext(Dispatchers.IO) {
                // TODO: API Calls
                delay(1500)
            }
            _uiEvent.value = Event(TransmissionConsentUiEvent.OnHideLoading)
            _event.value = Event(TransmissionConsentEvent.OnCertificateTransmitted)
        }
    }

    fun retry() {
//        TODO: retry logic
    }

    sealed class TransmissionConsentUiEvent {
        object OnShowLoading : TransmissionConsentUiEvent()
        object OnHideLoading : TransmissionConsentUiEvent()
        object OnError : TransmissionConsentUiEvent()
    }

    sealed class TransmissionConsentEvent {
        object OnCertificateTransmitted : TransmissionConsentEvent()
        object OnCertificateTransmissionFailed : TransmissionConsentEvent()
    }
}