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
 *  Created by osarapulov on 7/12/21 1:22 PM
 */

package dgca.wallet.app.android.certificate.view.validity

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import dgca.wallet.app.android.data.local.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CertificateValidityViewModel @Inject constructor(
    countriesRepository: CountriesRepository,
    private val preferences: Preferences
) : ViewModel() {
    private val _countries: MediatorLiveData<Pair<List<String>?, String?>> = MediatorLiveData()
    val countries: LiveData<Pair<List<String>?, String?>> = _countries
    private val _selectedCountry: MutableLiveData<String?> = MutableLiveData<String?>()

    fun selectCountry(countryIsoCode: String) {
        preferences.selectedCountryIsoCode = countryIsoCode
        _selectedCountry.value = countryIsoCode
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _selectedCountry.postValue(preferences.selectedCountryIsoCode)
            }
        }

        _countries.addSource(countriesRepository.getCountries().asLiveData()) {
            _countries.value = Pair(it, _countries.value?.second)
        }

        _countries.addSource(_selectedCountry) {
            if (it != countries.value?.second) {
                _countries.value = Pair(_countries.value?.first, it ?: "")
            }
        }
    }
}