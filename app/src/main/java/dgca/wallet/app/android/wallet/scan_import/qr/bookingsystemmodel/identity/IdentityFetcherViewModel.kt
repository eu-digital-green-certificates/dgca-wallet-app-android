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
 *  Created by osarapulov on 9/16/21 3:23 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.ticketing.identity.GetTicketingIdentityDocumentUseCase
import dgca.wallet.app.android.model.TicketingCheckInParcelable
import dgca.wallet.app.android.model.toRemote
import dgca.verifier.app.ticketing.data.identity.TicketingIdentityDocumentRemote
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class IdentityFetcherResult {
    data class Success(val ticketingIdentityDocumentRemote: TicketingIdentityDocumentRemote) : IdentityFetcherResult()
    object Fail : IdentityFetcherResult()
}

@HiltViewModel
class IdentityFetcherViewModel @Inject constructor(
    private val getTicketingIdentityDocumentUseCase: GetTicketingIdentityDocumentUseCase
) : ViewModel() {
    private val _identityFetcherResult = MutableLiveData<IdentityFetcherResult>()
    val identityFetcherResult: LiveData<IdentityFetcherResult> = _identityFetcherResult

    fun initialize(ticketingCheckInParcelable: TicketingCheckInParcelable) {
        viewModelScope.launch {
            val ticketingIdentityDocumentRemote: TicketingIdentityDocumentRemote? = try {
                getTicketingIdentityDocumentUseCase.run(ticketingCheckInParcelable.toRemote())
            } catch (exception: Exception) {
                Timber.e("Error fetching identity document")
                null
            }

            _identityFetcherResult.value = if (ticketingIdentityDocumentRemote != null) IdentityFetcherResult.Success(ticketingIdentityDocumentRemote) else null
        }
    }
}