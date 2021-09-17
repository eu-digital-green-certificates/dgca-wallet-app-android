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
import dgca.wallet.app.android.model.AccessTokenResult
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.IdentityDocument
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class AccessTokenFetcherResult {
    data class Success(val accessTokenResult: AccessTokenResult) : AccessTokenFetcherResult()
    object Fail : AccessTokenFetcherResult()
}

@HiltViewModel
class AccessTokenFetcherViewModel @Inject constructor(
    private val getAccessTokenUseCase: GetAccessTokenUseCase
) : ViewModel() {
    private val _accessTokenFetcherResult = MutableLiveData<AccessTokenFetcherResult>()
    val accessTokenFetcherResult: LiveData<AccessTokenFetcherResult> = _accessTokenFetcherResult

    fun initialize(bookingSystemModel: BookingSystemModel, identityDocument: IdentityDocument) {
        viewModelScope.launch {
            val accessTokenResult: AccessTokenResult? = try {
                getAccessTokenUseCase.run(bookingSystemModel, identityDocument)
            } catch (exception: Exception) {
                Timber.e(exception, "Error fetching access token")
                null
            }

            _accessTokenFetcherResult.value =
                if (accessTokenResult == null) AccessTokenFetcherResult.Fail else AccessTokenFetcherResult.Success(
                    accessTokenResult
                )
        }
    }
}