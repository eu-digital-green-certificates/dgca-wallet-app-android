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
 *  Created by osarapulov on 2/3/22, 5:38 PM
 */

package dgca.wallet.app.android.revocation

import android.security.keystore.KeyProperties
import dgca.verifier.app.decoder.getKeyPairFor
import dgca.verifier.app.decoder.model.KeyPairData
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.data.local.Converters
import dgca.wallet.app.android.data.local.Preferences
import dgca.wallet.app.android.model.generateRevocationKeystoreKeyAlias
import dgca.wallet.app.android.util.jwt.JwtTokenGenerator
import dgca.wallet.app.android.util.jwt.JwtTokenHeader
import dgca.wallet.app.android.wallet.scan_import.GreenCertificateFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateCertificatesRevocationDataUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val converters: Converters,
    private val revocationService: RevocationService,
    private val greenCertificateFetcher: GreenCertificateFetcher,
    private val jwtTokenGenerator: JwtTokenGenerator,
    private val preferences: Preferences
) {
    suspend fun run() {
        withContext(Dispatchers.IO) {
            val signatureCertificateIdMap = mutableMapOf<String, Int>()
            val revocations: MutableList<String> = mutableListOf()

            var certificates = walletRepository.getCertificates()
            if (certificates?.isNotEmpty() == true) {
                certificates.forEach {

                    // Jwt token header
                    val timeStamp = converters.zonedDateTimeToTimestamp(it.dateTaken)
                    val alias = generateRevocationKeystoreKeyAlias(timeStamp)
                    val keyPairData: KeyPairData = getKeyPairFor(alias)

                    val apiProtocolAlgo = when (keyPairData.keyPair.public.algorithm) {
                        KeyProperties.KEY_ALGORITHM_EC -> ES256
                        KeyProperties.KEY_ALGORITHM_RSA -> RSA256
                        else -> throw IllegalArgumentException()
                    }
                    val jwtTokenHeader = JwtTokenHeader(apiProtocolAlgo)

                    // Jwt token body
                    val certificateModel = it.certificate
                    val certificateIdentifier = certificateModel.getCertificateIdentifier()
                    val issuingCountry = certificateModel.getIssuingCountry()
                    val cose = greenCertificateFetcher.fetchDataFromQrString(it.qrCodeText).first

                    val uvciSha256Full =
                        certificateIdentifier?.toByteArray()?.toFullSha256HexString() ?: ""
                    val uvciSha256 = certificateIdentifier?.toByteArray()?.toSha256HexString() ?: ""
                    val coUvciSha256 =
                        (issuingCountry?.toUpperCase(Locale.getDefault()) + certificateIdentifier).toByteArray()
                            .toSha256HexString()
                    val signatureSha256 = cose?.getDccSignatureSha256() ?: ""

                    signatureCertificateIdMap[uvciSha256] = it.certificateId
                    signatureCertificateIdMap[coUvciSha256] = it.certificateId
                    signatureCertificateIdMap[signatureSha256] = it.certificateId

                    val jwtTokenBody = CertificateRevocationSignaturesEntity(
                        sub = uvciSha256Full,
                        payload = listOf(uvciSha256, coUvciSha256, signatureSha256),
                        exp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)
                    )

                    val jwtToken = jwtTokenGenerator.generateJwtToken(
                        jwtTokenHeader,
                        jwtTokenBody,
                        keyPairData
                    )
                    revocations.add(jwtToken)
                }


                val revokedCertificateSignaturesListResult =
                    revocationService.getRevocationLists(revocations)
                if (!revokedCertificateSignaturesListResult.isSuccessful) {
                    throw IllegalStateException()
                }

                val revokedCertificateIds = mutableListOf<Int>()
                revokedCertificateSignaturesListResult.body()!!.forEach {
                    val certificateId = signatureCertificateIdMap[it]!!
                    revokedCertificateIds.add(certificateId)
                }
                val notRevokedCertificatesIds = mutableSetOf<Int>()
                certificates.forEach {
                    if (revokedCertificateIds.contains(it.certificateId).not()) {
                        notRevokedCertificatesIds.add(it.certificateId)
                    }
                }

                walletRepository.setCertificatesRevokedBy(revokedCertificateIds, true)
                walletRepository.setCertificatesRevokedBy(notRevokedCertificatesIds, false)
            }

            preferences.lastRevocationStateUpdateTimeStamp =
                converters.zonedDateTimeToTimestamp(ZonedDateTime.now())
        }
    }

    companion object {
        const val ES256 = "ES256"
        const val RSA256 = "RSA256"
    }
}
