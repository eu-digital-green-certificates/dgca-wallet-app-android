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
 *  Created by osarapulov on 8/26/21 10:36 AM
 */

package dgca.wallet.app.android.certificate

import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import timber.log.Timber

class DefaultGreenCertificateFetcher(
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val coseService: CoseService,
    private val schemaValidator: SchemaValidator,
    private val cborService: CborService,
) : GreenCertificateFetcher {

    override fun fetchDataFromQrString(qrString: String): Pair<ByteArray?, GreenCertificate?> {
        val verificationResult = VerificationResult()
        val plainInput = prefixValidationService.decode(qrString, verificationResult)
        val compressedCose = base45Service.decode(plainInput, verificationResult)
        if (verificationResult.base45Decoded.not()) {
            Timber.d("Verification failed: base45 not decoded")
            return Pair(null, null)
        }

        val coseResult: ByteArray? = compressorService.decode(compressedCose, verificationResult)

        if (coseResult == null) {
            Timber.d("Verification failed: Too many bytes read")
            return Pair(null, null)
        }

        val cose: ByteArray = coseResult
        val coseData = coseService.decode(cose, verificationResult)
        if (coseData == null) {
            Timber.d("Verification failed: COSE not decoded")
            return Pair(cose, null)
        }

        schemaValidator.validate(coseData.cbor, verificationResult)
        return Pair(cose, cborService.decode(coseData.cbor, verificationResult))
    }

    override fun fetchGreenCertificateFromQrString(qrString: String): GreenCertificate? = fetchDataFromQrString(qrString).second
}