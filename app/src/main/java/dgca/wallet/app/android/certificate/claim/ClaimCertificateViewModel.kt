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
 *  Created by mykhailo.nester on 5/12/21 12:27 AM
 */

package dgca.wallet.app.android.certificate.claim

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.*
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.wallet.app.android.BuildConfig
import dgca.wallet.app.android.Event
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.ConfigRepository
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.data.local.toCertificateModel
import dgca.wallet.app.android.data.remote.ApiResult
import dgca.wallet.app.android.data.remote.ClaimResponse
import dgca.wallet.app.android.model.ClaimRequest
import dgca.wallet.app.android.model.PublicKeyData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ClaimCertificateViewModel @Inject constructor(
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val coseService: CoseService,
    private val schemaValidator: SchemaValidator,
    private val cborService: CborService,
    private val configRepository: ConfigRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _certificate = MutableLiveData<CertificateModel?>()
    val certificate: LiveData<CertificateModel?> = _certificate

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    private val _event = MutableLiveData<Event<ClaimCertEvent>>()
    val event: LiveData<Event<ClaimCertEvent>> = _event

    private var cose: ByteArray = ByteArray(0)
    private var greenCertificate: GreenCertificate? = null

    fun init(qrCodeText: String) {
        viewModelScope.launch {
            _inProgress.value = true
            withContext(Dispatchers.IO) {
                val verificationResult = VerificationResult()
                val plainInput = prefixValidationService.decode(qrCodeText, verificationResult)
                val compressedCose = base45Service.decode(plainInput, verificationResult)
                val coseResult: ByteArray? = compressorService.decode(compressedCose, verificationResult)

                if (coseResult == null) {
                    Timber.d("Verification failed: Too many bytes read")
                    return@withContext
                }
                cose = coseResult

                val coseData = coseService.decode(cose, verificationResult)
                if (coseData == null) {
                    Timber.d("Verification failed: COSE not decoded")
                    return@withContext
                }

                schemaValidator.validate(coseData.cbor, verificationResult)
                greenCertificate = cborService.decode(coseData.cbor, verificationResult)
            }
            _inProgress.value = false
            _certificate.value = greenCertificate?.toCertificateModel()
        }
    }

    fun save(qrCode: String, tan: String) {
        viewModelScope.launch {
            _inProgress.value = true
            var claimResult: ApiResult<ClaimResponse>? = null

            withContext(Dispatchers.IO) {
                if (greenCertificate == null || cose.isEmpty()) {
                    return@withContext
                }

                val dgci = greenCertificate?.getDgci()
                val certHash = cose.getValidationDataFromCOSE().toHash()
                val tanHash = tan.toByteArray().toHash()

                val keyPairData = cose.generateKeyPair()
                val keyPair: KeyPair? = keyPairData?.keyPair
                val sigAlg = keyPairData?.algo

                if (keyPair == null || sigAlg == null) {
                    Timber.d("Key not generated")
                    return@withContext
                }

                val keyData = PublicKeyData(
                    keyPair.public.algorithm,
                    Base64.getEncoder().encodeToString(keyPair.public.encoded)
                )

                val signature = generateClaimSignature(tanHash, certHash, keyData.value, keyPair.private, sigAlg)
                val request = ClaimRequest(
                    dgci,
                    certHash,
                    tanHash,
                    keyData,
                    sigAlg,
                    signature
                )

                val config = configRepository.local().getConfig()
                claimResult = walletRepository.claimCertificate(
                    config.getClaimUrl(BuildConfig.VERSION_NAME),
                    prefixValidationService.encode(qrCode),
                    request
                )
            }
            _inProgress.value = false
            claimResult?.success?.let { _event.value = Event(ClaimCertEvent.OnCertClaimed(true)) }
            claimResult?.error?.let { _event.value = Event(ClaimCertEvent.OnCertNotClaimed(it.details)) }
        }
    }

    sealed class ClaimCertEvent {
        data class OnCertClaimed(val isClaimed: Boolean) : ClaimCertEvent()
        data class OnCertNotClaimed(val error: String) : ClaimCertEvent()
    }
}