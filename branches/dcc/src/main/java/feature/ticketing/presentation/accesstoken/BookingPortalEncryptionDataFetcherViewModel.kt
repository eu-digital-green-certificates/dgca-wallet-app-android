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
 *  Created by osarapulov on 10/11/21 7:51 PM
 */

package feature.ticketing.presentation.accesstoken

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import feature.ticketing.presentation.model.BookingPortalEncryptionData
import feature.ticketing.presentation.model.TicketingCheckInParcelable
import feature.ticketing.presentation.model.TicketingValidationServiceIdentityParcelable
import feature.ticketing.domain.identity.accesstoken.GetTicketingAccessTokenUseCase
import feature.ticketing.domain.identity.accesstoken.TicketingAccessTokenResponseContainer
import feature.ticketing.domain.identity.validityserviceidentity.GetTicketingValidationServiceIdentityUseCase
import feature.ticketing.domain.identity.validityserviceidentity.TicketingValidationServiceIdentityResponse
import feature.ticketing.presentation.TicketingServiceParcelable
import feature.ticketing.presentation.model.fromRemote
import feature.ticketing.presentation.toRemote
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.inject.Inject

sealed class BookingPortalEncryptionDataResult {
    data class Success(val bookingPortalEncryptionData: BookingPortalEncryptionData) : BookingPortalEncryptionDataResult()
    object Fail : BookingPortalEncryptionDataResult()
}

@HiltViewModel
class BookingPortalEncryptionDataFetcherViewModel @Inject constructor(
    private val getTicketingAccessTokenUseCase: GetTicketingAccessTokenUseCase,
    private val getTicketingValidationServiceIdentityUseCase: GetTicketingValidationServiceIdentityUseCase
) : ViewModel() {
    private val _accessTokenFetcherResult = MutableLiveData<BookingPortalEncryptionDataResult>()
    val bookingPortalEncryptionDataResult: LiveData<BookingPortalEncryptionDataResult> = _accessTokenFetcherResult

    fun initialize(
        ticketingCheckInParcelable: TicketingCheckInParcelable,
        accessTokenTicketingServiceParcelable: TicketingServiceParcelable,
        validationTicketingServiceParcelable: TicketingServiceParcelable
    ) {
        viewModelScope.launch {
            val keyPairGen = KeyPairGenerator.getInstance("EC")
            keyPairGen.initialize(256)
            val keyPair: KeyPair = keyPairGen.generateKeyPair()

            val ticketingAccessTokenResponseContainer: TicketingAccessTokenResponseContainer? =
                fetchAccessToken(
                    keyPair,
                    ticketingCheckInParcelable,
                    accessTokenTicketingServiceParcelable,
                    validationTicketingServiceParcelable
                )
            val ticketingValidationServiceIdentityResponse: TicketingValidationServiceIdentityResponse? =
                fetchValidationServiceIdentity(validationTicketingServiceParcelable)

            _accessTokenFetcherResult.value =
                if (ticketingAccessTokenResponseContainer == null || ticketingValidationServiceIdentityResponse == null) BookingPortalEncryptionDataResult.Fail else BookingPortalEncryptionDataResult.Success(
                    prepareBookingPortalEncryptionResult(
                        keyPair,
                        ticketingAccessTokenResponseContainer,
                        ticketingValidationServiceIdentityResponse.fromRemote()
                    )
                )
        }
    }

    private suspend fun fetchAccessToken(
        keyPair: KeyPair,
        ticketingCheckInParcelable: TicketingCheckInParcelable,
        accessTokenTicketingServiceParcelable: TicketingServiceParcelable,
        validationTicketingServiceParcelable: TicketingServiceParcelable
    ): TicketingAccessTokenResponseContainer? = try {
        getTicketingAccessTokenUseCase.run(
            keyPair,
            ticketingCheckInParcelable,
            accessTokenTicketingServiceParcelable.toRemote(),
            validationTicketingServiceParcelable.toRemote()
        )
    } catch (exception: Exception) {
        Timber.e(exception, "Error fetching access token")
        null
    }

    private suspend fun fetchValidationServiceIdentity(validationTicketingServiceParcelable: TicketingServiceParcelable): TicketingValidationServiceIdentityResponse? =
        try {
            getTicketingValidationServiceIdentityUseCase.run(validationTicketingServiceParcelable.toRemote())
        } catch (exception: Exception) {
            Timber.e(exception, "Error fetching validation service idendity")
            null
        }

    private fun prepareBookingPortalEncryptionResult(
        keyPair: KeyPair,
        ticketingAccessTokenResponseContainer: TicketingAccessTokenResponseContainer,
        ticketingValidationServiceIdentity: TicketingValidationServiceIdentityParcelable
    ): BookingPortalEncryptionData {
        return BookingPortalEncryptionData(
            keyPair,
            ticketingAccessTokenResponseContainer.fromRemote(),
            ticketingValidationServiceIdentity
        )
    }
}