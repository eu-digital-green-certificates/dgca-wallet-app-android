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
 *  Created by osarapulov on 7/12/21 1:22 PM
 */

package dgca.wallet.app.android.certificate.view.validity

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.cbor.GreenCertificateData
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import dgca.wallet.app.android.data.local.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/*-
 * ---license-start
 * eu-digital-green-certificates / dgc-certlogic-android
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 *
 * Created by osarapulov on 12.07.21 13:22
 */
@HiltViewModel
class CertificateValidityViewModel @Inject constructor(
    private val countriesRepository: CountriesRepository,
    private val preferences: Preferences,
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val coseService: CoseService,
    private val cborService: CborService,
) : ViewModel() {
    private val _countries: MediatorLiveData<Triple<List<String>, String?, GreenCertificateData?>> = MediatorLiveData()
    val countries: LiveData<Triple<List<String>, String?, GreenCertificateData?>> = _countries
    private val _selectedCountry: LiveData<String?> = liveData {
        emit(preferences.selectedCountryIsoCode)
    }
    private val _greenCertificateData = MutableLiveData<GreenCertificateData>()
    val greenCertificateData: LiveData<GreenCertificateData> = _greenCertificateData

    fun selectCountry(countryIsoCode: String) {
        preferences.selectedCountryIsoCode = countryIsoCode
    }

    init {
        _countries.addSource(countriesRepository.getCountries().asLiveData()) {
            _countries.value = Triple(it, _countries.value?.second, _countries.value?.third)
        }

        _countries.addSource(_selectedCountry) {
            _countries.value = Triple(_countries.value?.first ?: emptyList(), it ?: "", _countries.value?.third)
        }

        _countries.addSource(_greenCertificateData) {
            _countries.value =
                Triple(_countries.value?.first ?: emptyList(), _countries.value?.second, it)
        }
    }

    fun init(qrCodeText: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val verificationResult = VerificationResult()
                val plainInput = prefixValidationService.decode(qrCodeText, verificationResult)
                val compressedCose = base45Service.decode(plainInput, verificationResult)
                val cose = compressorService.decode(compressedCose, verificationResult)

                coseService.decode(cose, verificationResult)?.let {
                    _greenCertificateData.postValue(cborService.decodeData(it.cbor, verificationResult))
                }
            }
        }
    }

}