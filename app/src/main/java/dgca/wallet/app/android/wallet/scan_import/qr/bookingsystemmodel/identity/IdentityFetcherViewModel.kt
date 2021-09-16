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
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.data.IdentityDocument
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.identity.GetIdentityDocumentUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class IdentityFetcherResult {
    data class Success(val identityDocument: IdentityDocument) : IdentityFetcherResult()
    object Fail : IdentityFetcherResult()
}

class IdentityFetcherViewModel @Inject constructor(
    private val getIdentityDocumentUseCase: GetIdentityDocumentUseCase
) : ViewModel() {
    private val _identityFetcherResult = MutableLiveData<IdentityFetcherResult>()
    val identityFetcherResult: LiveData<IdentityFetcherResult> = _identityFetcherResult

    fun initialize(bookingSystemModel: BookingSystemModel) {
        viewModelScope.launch {
            val identityDocument: IdentityDocument? = try {
                getIdentityDocumentUseCase.run(bookingSystemModel.serviceIdentity)
            } catch (exception: Exception) {
                Timber.e("Error fetching identity document")
                null
            }

            _identityFetcherResult.value = if (identityDocument != null) IdentityFetcherResult.Success(identityDocument) else null
        }
    }
}