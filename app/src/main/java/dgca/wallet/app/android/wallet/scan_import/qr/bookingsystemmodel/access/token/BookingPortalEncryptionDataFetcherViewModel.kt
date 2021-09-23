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

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.access.token

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.model.KeyPairData
import dgca.wallet.app.android.data.remote.ticketing.access.token.AccessTokenResponse
import dgca.wallet.app.android.data.remote.ticketing.access.token.ValidationServiceIdentityResponse
import dgca.wallet.app.android.model.BookingPortalEncryptionData
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.Service
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
    private val getAccessTokenUseCase: GetAccessTokenUseCase,
    private val getValidationServiceIdentityUseCase: GetValidationServiceIdentityUseCase
) : ViewModel() {
    private val _accessTokenFetcherResult = MutableLiveData<BookingPortalEncryptionDataResult>()
    val bookingPortalEncryptionDataResult: LiveData<BookingPortalEncryptionDataResult> = _accessTokenFetcherResult

    fun initialize(bookingSystemModel: BookingSystemModel, accessTokenService: Service, validationService: Service) {
        viewModelScope.launch {
            val keyPairGen = KeyPairGenerator.getInstance("EC")
            keyPairGen.initialize(256)
            val keyPairData = KeyPairData("SHA256withECDSA", keyPairGen.generateKeyPair())
            val keyPair: KeyPair = keyPairData.keyPair

            val accessTokenResponse: AccessTokenResponse? =
                fetchAccessToken(keyPair, bookingSystemModel, accessTokenService, validationService)
            val validationServiceIdentityResponse: ValidationServiceIdentityResponse? =
                fetchValidationServiceIdentity(validationService)

            _accessTokenFetcherResult.value =
                if (accessTokenResponse == null || validationServiceIdentityResponse == null) BookingPortalEncryptionDataResult.Fail else BookingPortalEncryptionDataResult.Success(
                    prepareAccessTokenResult(keyPair, accessTokenResponse, validationServiceIdentityResponse)
                )
        }
    }

    private suspend fun fetchAccessToken(
        keyPair: KeyPair,
        bookingSystemModel: BookingSystemModel,
        accessTokenService: Service,
        validationService: Service
    ): AccessTokenResponse? = try {
        getAccessTokenUseCase.run(keyPair, bookingSystemModel, accessTokenService, validationService)
    } catch (exception: Exception) {
        Timber.e(exception, "Error fetching access token")
        null
    }

    private suspend fun fetchValidationServiceIdentity(validationService: Service): ValidationServiceIdentityResponse? = try {
        getValidationServiceIdentityUseCase.run(validationService)
    } catch (exception: Exception) {
        Timber.e(exception, "Error fetching validation service idendity")
        null
    }

    private fun prepareAccessTokenResult(
        keyPair: KeyPair,
        accessTokenResponse: AccessTokenResponse,
        validationServiceIdentityResponse: ValidationServiceIdentityResponse
    ): BookingPortalEncryptionData {
        return BookingPortalEncryptionData(keyPair, accessTokenResponse, validationServiceIdentityResponse)
    }
}