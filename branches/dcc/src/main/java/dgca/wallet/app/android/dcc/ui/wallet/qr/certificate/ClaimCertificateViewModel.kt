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
 *  Created by mykhailo.nester on 5/12/21 12:27 AM
 */

package dgca.wallet.app.android.dcc.ui.wallet.qr.certificate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.app.dcc.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.generateClaimSignature
import dgca.verifier.app.decoder.generateKeyPairFor
import dgca.verifier.app.decoder.getValidationDataFromCOSE
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.toHash
import dgca.wallet.app.android.dcc.Event
import dgca.wallet.app.android.dcc.data.ConfigRepository
import dgca.wallet.app.android.dcc.data.remote.ApiResult
import dgca.wallet.app.android.dcc.data.remote.claim.ClaimRequest
import dgca.wallet.app.android.dcc.data.remote.claim.ClaimResponse
import dgca.wallet.app.android.dcc.data.remote.claim.PublicKeyData
import dgca.wallet.app.android.dcc.data.remote.claim.generateRevocationKeystoreKeyAlias
import dgca.wallet.app.android.dcc.data.wallet.WalletRepository
import feature.revocation.UpdateCertificatesRevocationDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.KeyPair
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ClaimCertificateViewModel @Inject constructor(
    private val prefixValidationService: PrefixValidationService,
    private val configRepository: ConfigRepository,
    private val walletRepository: WalletRepository,
    private val updateCertificatesRevocationDataUseCase: UpdateCertificatesRevocationDataUseCase
) : ViewModel() {

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    private val _event = MutableLiveData<Event<ClaimCertEvent>>()
    val event: LiveData<Event<ClaimCertEvent>> = _event

    fun save(claimGreenCertificateModel: ClaimGreenCertificateModel, tan: String) {
        viewModelScope.launch {
            _inProgress.value = true
            var claimResult: ApiResult<ClaimResponse>? = null

            withContext(Dispatchers.IO) {
                val certHash = claimGreenCertificateModel.cose.getValidationDataFromCOSE().toHash()
                val tanHash = tan.toByteArray().toHash()

                val currentTimeStamp = System.currentTimeMillis()
                val alias = generateRevocationKeystoreKeyAlias(currentTimeStamp)
                val keyPairData = claimGreenCertificateModel.cose.generateKeyPairFor(alias)
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
                    claimGreenCertificateModel.dgci,
                    certHash,
                    tanHash,
                    keyData,
                    sigAlg,
                    signature
                )

                val config = configRepository.local().getConfig()
                claimResult = walletRepository.claimCertificate(
                    config.getClaimUrl(BuildConfig.VERSION_NAME),
                    prefixValidationService.encode(claimGreenCertificateModel.qrCodeText),
                    request,
                    currentTimeStamp
                )

                if (claimResult?.success != null) {
                    try {
                        updateCertificatesRevocationDataUseCase.run()
                    } catch (ex: Exception) {
                        Timber.e(ex, "Revocation update state after claiming")
                    }
                }
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
