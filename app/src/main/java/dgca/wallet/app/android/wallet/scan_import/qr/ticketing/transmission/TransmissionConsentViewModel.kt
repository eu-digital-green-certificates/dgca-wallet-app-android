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

package dgca.wallet.app.android.wallet.scan_import.qr.ticketing.transmission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.ticketing.validation.TicketingValidationUseCase
import dgca.wallet.app.android.Event
import dgca.wallet.app.android.model.BookingPortalEncryptionData
import dgca.wallet.app.android.model.toRemote
import dgca.wallet.app.android.wallet.scan_import.qr.ticketing.accesstoken.toRemote
import dgca.wallet.app.android.wallet.scan_import.qr.ticketing.validationresult.BookingPortalValidationResult
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransmissionConsentViewModel @Inject constructor(
    private val validationUseCase: TicketingValidationUseCase
) : ViewModel() {

    private val _uiEvent = MutableLiveData<Event<TransmissionConsentUiEvent>>()
    val uiEvent: LiveData<Event<TransmissionConsentUiEvent>> = _uiEvent

    private val _event = MutableLiveData<Event<TransmissionConsentEvent>>()
    val event: LiveData<Event<TransmissionConsentEvent>> = _event

    fun onPermissionAccepted(qrString: String, bookingPortalEncryptionData: BookingPortalEncryptionData) {
        viewModelScope.launch {
            _uiEvent.value = Event(TransmissionConsentUiEvent.OnShowLoading)

            _event.value = Event(
                try {
                    val bookingPortalValidationResult: BookingPortalValidationResult =
                        validationUseCase.run(
                            qrString,
                            bookingPortalEncryptionData.keyPair,
                            bookingPortalEncryptionData.accessTokenContainer.toRemote(),
                            bookingPortalEncryptionData.ticketingValidationServiceIdentity.toRemote()
                        )
                    TransmissionConsentEvent.OnCertificateTransmitted(bookingPortalValidationResult)
                } catch (exception: Exception) {
                    TransmissionConsentEvent.OnCertificateTransmissionFailed
                }
            )
            _uiEvent.value = Event(TransmissionConsentUiEvent.OnHideLoading)
        }
    }

    sealed class TransmissionConsentUiEvent {
        object OnShowLoading : TransmissionConsentUiEvent()
        object OnHideLoading : TransmissionConsentUiEvent()
        object OnError : TransmissionConsentUiEvent()
    }

    sealed class TransmissionConsentEvent {
        class OnCertificateTransmitted(val bookingPortalValidationResult: BookingPortalValidationResult) :
            TransmissionConsentEvent()

        object OnCertificateTransmissionFailed : TransmissionConsentEvent()
    }
}