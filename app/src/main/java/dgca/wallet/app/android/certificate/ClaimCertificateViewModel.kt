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
 *  Created by mykhailo.nester on 5/11/21 9:00 PM
 */

package dgca.wallet.app.android.certificate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.decoder.toBase64
import dgca.wallet.app.android.data.CertificateModel
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.data.local.toCertificateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ClaimCertificateViewModel @Inject constructor(
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val cryptoService: CryptoService,
    private val coseService: CoseService,
    private val schemaValidator: SchemaValidator,
    private val cborService: CborService,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _verificationResult = MutableLiveData<VerificationResult>()
    val verificationResult: LiveData<VerificationResult> = _verificationResult

    private val _certificate = MutableLiveData<CertificateModel?>()
    val certificate: LiveData<CertificateModel?> = _certificate

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    fun save(qrCode: String, tan: String) {
        viewModelScope.launch {
            _inProgress.value = true
            var greenCertificate: GreenCertificate? = null
            val verificationResult = VerificationResult()

            withContext(Dispatchers.IO) {
                val plainInput = prefixValidationService.decode(qrCode, verificationResult)
                val compressedCose = base45Service.decode(plainInput, verificationResult)
                val cose = compressorService.decode(compressedCose, verificationResult)

                val coseData = coseService.decode(cose, verificationResult)
                if (coseData == null) {
                    Timber.d("Verification failed: COSE not decoded")
                    return@withContext
                }

                val kid = coseData.kid
                if (kid == null) {
                    Timber.d("Verification failed: cannot extract kid from COSE")
                    return@withContext
                }

                schemaValidator.validate(coseData.cbor, verificationResult)
                greenCertificate = cborService.decode(coseData.cbor, verificationResult)

//                val certificate = walletRepository.claimCertificate(kid.toBase64())
//                if (certificate == null) {
//                    Timber.d("Verification failed: failed to load certificate")
//                    return@withContext
//                }
//                cryptoService.validate(cose, certificate, verificationResult)

// Claim Cert
                val tan = "test".toUpperCase(Locale.getDefault())
                val tanHash = MessageDigest.getInstance("SHA-256")
                    .digest(tan.toByteArray())
                    .toBase64()

                val certHash = MessageDigest.getInstance("SHA-256")
                    .digest(coseData.cbor)
                    .toBase64()

                val generator: KeyPairGenerator = KeyPairGenerator.getInstance("EC")
                generator.initialize(ECGenParameterSpec("secp256r1"))
                val keyPair: KeyPair = generator.generateKeyPair()
                val publicKey: ECPublicKey = keyPair.public as ECPublicKey
                val privateKey: ECPrivateKey = keyPair.private as ECPrivateKey

                val pubKey = publicKey.encoded.toBase64()

                val toBeSigned = tanHash + certHash + pubKey
                val toBeSignedData = toBeSigned.encodeToByteArray()

                val signature = Signature.getInstance("SHA256withECDSA")
                signature.initSign(privateKey)
                signature.update(toBeSignedData)
                val sign = signature.sign()

                val signEncoded = sign.toBase64()
                val sigAlg = "SHA256withECDSA"
//                val certificateIdentifier = greenCertificate?.getCi()

//                let toBeSigned = tanHash + certHash + pubKey
//    let toBeSignedData = Data(toBeSigned.encode())
//    Enclave.sign(data: toBeSignedData, with: cert.keyPair, using: .ecdsaSignatureMessageX962SHA256) { sign, err in
//      guard let sign = sign, err == nil else {
//        return
//      }
//
//      let keyParam: [String: Any] = [
//        "type": "EC",
//        "value": pubKey,
//      ]
//      let param: [String: Any] = [
//        "DGCI": cert.uvci,
//        "TANHash": tanHash,
//        "certhash": certHash,
//        "publicKey": keyParam,
//        "signature": sign.base64EncodedString(),
//        "sigAlg": "SHA256withECDSA",
//      ]
//      AF.request(serverURI + claimEndpoint, method: .post, parameters: param, encoding: JSONEncoding.default, headers: nil, interceptor: nil, requestModifier: nil).response {
//        guard
//          case .success(_) = $0.result,
//          let status = $0.response?.statusCode,
//          status == 204
//        else {
//          completion?(false)
//          return
//        }
//        completion?(true)
//      }
//    }
            }

            _inProgress.value = false
            _verificationResult.value = verificationResult
            _certificate.value = greenCertificate?.toCertificateModel()
        }
    }
}