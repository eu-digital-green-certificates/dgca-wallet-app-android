/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2022 T-Systems International GmbH and all other contributors
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
 *  Created by osarapulov on 2/14/22, 5:22 PM
 */

package dgca.wallet.app.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.data.local.Preferences
import dgca.wallet.app.android.revocation.UpdateCertificatesRevocationDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: Preferences,
    private val updateCertificatesRevocationDataUseCase: UpdateCertificatesRevocationDataUseCase
) : ViewModel() {

    private val _lastRevocationStateUpdateTimeStamp = MutableLiveData(preferences.lastRevocationStateUpdateTimeStamp)
    val lastRevocationStateUpdateTimeStamp: LiveData<Long> = _lastRevocationStateUpdateTimeStamp

    fun updateRevocationState() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateCertificatesRevocationDataUseCase.run()
                _lastRevocationStateUpdateTimeStamp.postValue(preferences.lastRevocationStateUpdateTimeStamp)
            }
        }
    }
}
