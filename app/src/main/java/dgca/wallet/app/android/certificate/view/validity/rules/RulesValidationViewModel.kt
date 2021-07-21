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
 *  Created by osarapulov on 7/13/21 1:45 PM
 */

package dgca.wallet.app.android.certificate.view.validity.rules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.cbor.GreenCertificateData
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.model.CoseData
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.toBase64
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.ValidationResult
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository
import dgca.verifier.app.engine.domain.rules.GetRulesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RulesValidationViewModel @Inject constructor(
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val coseService: CoseService,
    private val cborService: CborService,
    private val engine: CertLogicEngine,
    private val getRulesUseCase: GetRulesUseCase,
    private val valueSetsRepository: ValueSetsRepository
) : ViewModel() {
    private val _validationResults = MutableLiveData<List<ValidationResult>?>()
    val validationResults: LiveData<List<ValidationResult>?> = _validationResults

    private val _inProgress = MutableLiveData<Boolean>(true)
    val inProgress: LiveData<Boolean> = _inProgress

    fun validate(qrCodeText: String, selectedCountry: String, zonedDateTime: ZonedDateTime) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val verificationResult = VerificationResult()
                val plainInput = prefixValidationService.decode(qrCodeText, verificationResult)
                val compressedCose = base45Service.decode(plainInput, verificationResult)
                val cose = compressorService.decode(compressedCose, verificationResult)

                val coseData: CoseData? = coseService.decode(cose, verificationResult)
                val greenCertificateData: GreenCertificateData? =
                    coseData?.let { cborService.decodeData(it.cbor, verificationResult) }
                val base64EncodedKid: String? = coseData?.kid?.toBase64()
                val validationResults: List<ValidationResult>? =
                    base64EncodedKid?.let { greenCertificateData.validateRules(zonedDateTime, selectedCountry, base64EncodedKid) }
                _validationResults.postValue(validationResults)
                _inProgress.postValue(false)
            }
        }
    }

    private suspend fun GreenCertificateData?.validateRules(
        zonedDateTime: ZonedDateTime,
        countryIsoCode: String,
        base64EncodedKid: String
    ): List<ValidationResult>? = this?.let {
        val engineCertificateType = this.greenCertificate.getEngineCertificateType()
        return if (countryIsoCode.isNotBlank()) {
            val issuingCountry: String =
                (if (this.issuingCountry?.isNotBlank() == true && this.issuingCountry != null) this.issuingCountry!! else this.greenCertificate.getIssuingCountry()).toLowerCase(
                    Locale.ROOT
                )
            val rules = getRulesUseCase.invoke(
                zonedDateTime,
                countryIsoCode,
                issuingCountry,
                engineCertificateType
            )
            val valueSetsMap = mutableMapOf<String, List<String>>()
            valueSetsRepository.getValueSets().forEach { valueSet ->
                val ids = mutableListOf<String>()
                valueSet.valueSetValues.fieldNames().forEach { id ->
                    ids.add(id)
                }
                valueSetsMap[valueSet.valueSetId] = ids
            }

            val externalParameter = ExternalParameter(
                validationClock = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC),
                valueSets = valueSetsMap,
                countryCode = countryIsoCode,
                exp = this.expirationTime,
                iat = this.issuedAt,
                issuerCountryCode = issuingCountry,
                kid = base64EncodedKid,
                region = "",
            )
            val validationResults = engine.validate(
                engineCertificateType,
                this.greenCertificate.schemaVersion,
                rules,
                externalParameter,
                this.hcertJson
            )

            validationResults
        } else {
            null
        }
    }
}

private fun GreenCertificate.getEngineCertificateType(): CertificateType {
    return when {
        this.recoveryStatements?.isNotEmpty() == true -> CertificateType.RECOVERY
        this.vaccinations?.isNotEmpty() == true -> CertificateType.VACCINATION
        this.tests?.isNotEmpty() == true -> CertificateType.TEST
        else -> CertificateType.TEST
    }
}