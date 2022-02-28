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
 *  Created by mykhailo.nester on 5/11/21 9:36 PM
 */

package dgca.wallet.app.android.data

import dgca.verifier.app.decoder.CertificateDecoder
import dgca.verifier.app.decoder.CertificateDecodingResult
import dgca.wallet.app.android.wallet.CertificatesCard
import dgca.wallet.app.android.data.local.CertificateDao
import dgca.wallet.app.android.data.local.CertificateEntity
import dgca.wallet.app.android.data.local.Converters
import dgca.wallet.app.android.data.local.toCertificateModel
import dgca.wallet.app.android.data.remote.ApiResult
import dgca.wallet.app.android.data.remote.ApiService
import dgca.wallet.app.android.data.remote.ClaimResponse
import dgca.wallet.app.android.model.ClaimRequest
import dgca.wallet.app.android.security.KeyStoreCryptor
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val certificateDao: CertificateDao,
    private val keyStoreCryptor: KeyStoreCryptor,
    private val certificateDecoder: CertificateDecoder,
    private val converters: Converters
) : BaseRepository(), WalletRepository {

    override suspend fun claimCertificate(
        url: String, qrCode: String, request: ClaimRequest, timeStamp: Long
    ): ApiResult<ClaimResponse> {
        return doApiBackgroundWork { apiService.claimCertificate(url, request) }.also { result ->
            result.success?.let {
                val tan = result.rawResponse?.body()?.tan ?: ""
                val codeEncrypted = keyStoreCryptor.encrypt(qrCode)
                val tanEncrypted = keyStoreCryptor.encrypt(tan)

                if (codeEncrypted != null && tanEncrypted != null) {
                    certificateDao.insert(
                        CertificateEntity(
                            qrCodeText = codeEncrypted,
                            tan = tanEncrypted,
                            dateAdded = converters.timestampToZonedDateTime(timeStamp)
                        )
                    )
                }
            }
        }
    }

    override suspend fun getCertificates(): List<CertificatesCard.CertificateCard>? {
        return execute {
            certificateDao.getAll()
                .map { encryptedCertificate -> decodeCertificate(encryptedCertificate) }
                .filter { it.second is CertificateDecodingResult.Success }
                .map { Pair(it.first, it.second as CertificateDecodingResult.Success) }
                .map { it.toCertificateCard() }
        }
    }

    override suspend fun getCertificatesById(certificateId: Int): CertificatesCard.CertificateCard? {
        return execute {
            certificateDao.getById(certificateId)?.let { certificateEntity ->
                decodeCertificate(certificateEntity).let {
                    if (it.second is CertificateDecodingResult.Success) {
                        Pair(it.first, it.second as CertificateDecodingResult.Success).toCertificateCard()
                    } else {
                        null
                    }
                }
            }
        }
    }

    override suspend fun deleteCertificateById(certificateId: Int): Boolean {
        return execute {
            certificateDao.delete(certificateId) == 1
        } == true
    }

    private fun decodeCertificate(encryptedCertificate: CertificateEntity): Pair<CertificateEntity, CertificateDecodingResult> {
        val certificate =
            encryptedCertificate.copy(
                qrCodeText = keyStoreCryptor.decrypt(encryptedCertificate.qrCodeText)!!,
                tan = keyStoreCryptor.decrypt(encryptedCertificate.tan)!!
            )
        // We assume that we do not store invalid QR codes, thus here, no errors should appear.
        return Pair(certificate, certificateDecoder.decodeCertificate(certificate.qrCodeText))
    }

    private fun Pair<CertificateEntity, CertificateDecodingResult.Success>.toCertificateCard(): CertificatesCard.CertificateCard {
        val certificateModel = this.second.greenCertificate.toCertificateModel()
        return CertificatesCard.CertificateCard(this.first, certificateModel)
    }
}