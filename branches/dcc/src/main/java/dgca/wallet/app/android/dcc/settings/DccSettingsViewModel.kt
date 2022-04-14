/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by mykhailo.nester on 4/24/21 2:54 PM
 */

package dgca.wallet.app.android.dcc.settings

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import dgca.wallet.app.android.dcc.data.ConfigRepository
import dgca.wallet.app.android.dcc.data.local.Preferences
import feature.revocation.UpdateCertificatesRevocationDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

data class RevocationUpdateResult(val timestamp: Long, val isSuccessful: Boolean? = null)

@HiltViewModel
class DccSettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val countriesRepository: CountriesRepository,
    private val preferences: Preferences,
    private val updateCertificatesRevocationDataUseCase: UpdateCertificatesRevocationDataUseCase
) : ViewModel(), LifecycleObserver {

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    private val _lastCountriesSyncLiveData = MutableLiveData<Long>(-1)
    val lastCountriesSyncLiveData: LiveData<Long> = _lastCountriesSyncLiveData

    private val _lastRevocationStateUpdateTimeStamp =
        MutableLiveData(RevocationUpdateResult(preferences.lastRevocationStateUpdateTimeStamp))
    val lastRevocationStateUpdateTimeStamp: LiveData<RevocationUpdateResult> =
        _lastRevocationStateUpdateTimeStamp

    init {
        viewModelScope.launch {
            var lastCountriesSync: Long
            val availableCountries = mutableSetOf<String>()
            withContext(Dispatchers.IO) {
                try {
                    countriesRepository.getCountries().firstOrNull()?.let {
                        availableCountries.addAll(it)
                    }
                } catch (exception: Exception) {
                    Timber.e(exception, "Error loading available countries on settings screen")
                }
                lastCountriesSync = countriesRepository.getLastCountriesSync()
            }

            _lastCountriesSyncLiveData.value = lastCountriesSync
        }
    }

    fun syncCountries() {
        viewModelScope.launch {
            _inProgress.value = true
            withContext(Dispatchers.IO) {
                try {
                    val config = configRepository.local().getConfig()
                    val versionName = "1.0.0" // TODO: update BuildConfig.VERSION_NAME
                    countriesRepository.preLoadCountries(
                        config.getCountriesUrl(versionName)
                    )
                    _lastCountriesSyncLiveData.postValue(countriesRepository.getLastCountriesSync())
                } catch (error: Throwable) {
                    Timber.e(error, "error refreshing keys")
                }
            }
            _inProgress.value = false
        }
    }

    fun updateRevocationState() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val isSuccessful = try {
                    updateCertificatesRevocationDataUseCase.run()
                    true
                } catch (exception: Exception) {
                    Timber.e(exception)
                    false
                }
                _lastRevocationStateUpdateTimeStamp.postValue(
                    RevocationUpdateResult(
                        preferences.lastRevocationStateUpdateTimeStamp,
                        isSuccessful
                    )
                )
            }
        }
    }
}
