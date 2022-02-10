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

import android.util.Base64
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.data.local.Converters
import dgca.wallet.app.android.model.generateRevocationKeystoreKeyAlias
import dgca.wallet.app.android.wallet.scan_import.GreenCertificateFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateCertificatesRevocationDataUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val converters: Converters,
    private val revocationService: RevocationService,
    private val greenCertificateFetcher: GreenCertificateFetcher
) {
    suspend fun run() {
        withContext(Dispatchers.IO) {
            val notRevokedCertificatesIds = mutableSetOf<Int>()
            val revocations: MutableList<String> = mutableListOf()

            val certificates = walletRepository.getNotRevokedCertificates()
            if (certificates?.isNotEmpty() == true) {
                val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore")
                ks.load(null)
                certificates.forEach {
                    notRevokedCertificatesIds.add(it.certificateId)

                    val timeStamp = converters.zonedDateTimeToTimestamp(it.dateTaken)
                    val alias = generateRevocationKeystoreKeyAlias(timeStamp)
                    val entry: KeyStore.Entry = ks.getEntry(alias, null)
                    val privateKey: PrivateKey = (entry as KeyStore.PrivateKeyEntry).privateKey
                    val publicKey: PublicKey = ks.getCertificate(alias).publicKey
                    val keyPair: KeyPair = KeyPair(publicKey, privateKey)
                    Timber.tag("MYTAG").d("KeyPair: $keyPair")

                    val certificateModel = it.certificate
                    val certificateIdentifier = certificateModel.getCertificateIdentifier()
                    val issuingCountry = certificateModel.getIssuingCountry()
                    val cose = greenCertificateFetcher.fetchDataFromQrString(it.qrCodeText).first
                    val uvciSha256 = certificateIdentifier?.toByteArray()?.toSha256HexString() ?: ""
                    val coUvciSha256 = (issuingCountry + certificateIdentifier).toByteArray().toSha256HexString()
                    val signatureSha256 = cose?.getDccSignatureSha256() ?: ""

                    val certificateRevocationSignaturesEntity = CertificateRevocationSignaturesEntity(
                        sub = uvciSha256,
                        payload = listOf(uvciSha256, coUvciSha256, signatureSha256),
                        exp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)
                    )

                    val json = jacksonObjectMapper().writeValueAsString(certificateRevocationSignaturesEntity)
                    val signature = Signature.getInstance("SHA256withECDSA")
                    signature.initSign(privateKey)
                    signature.update(json.toString().toByteArray(StandardCharsets.UTF_8))
                    val sigData = signature.sign()

                    val encodedEntity = Base64.encodeToString(sigData, Base64.NO_WRAP)
                    revocations.add(encodedEntity)
                }


                val res = revocationService.getRevocationLists(revocations)
                Timber.tag("MYTAG").d("Res: $res")

                // TODO implement logic to set certificates revoked.

                walletRepository.setCertificatesRevokedBy(notRevokedCertificatesIds)
            }
        }
    }
}
