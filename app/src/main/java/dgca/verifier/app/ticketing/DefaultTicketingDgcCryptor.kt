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
 *  Created by osarapulov on 9/27/21 9:09 AM
 */

package dgca.verifier.app.ticketing

import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission.SUPPORTED_ENCRYPTION_SCHEMA
import java.nio.charset.StandardCharsets
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class DefaultTicketingDgcCryptor : TicketingDgcCryptor {
    private val dccCryptService = DccRsaOaepWithSha256AesGcmCryptService()

    override fun encodeDcc(dcc: String, iv: ByteArray, publicKey: PublicKey): TicketingEncryptedDgcData {
        return dccCryptService.encryptData(
            dcc.toByteArray(StandardCharsets.UTF_8),
            publicKey,
            SUPPORTED_ENCRYPTION_SCHEMA, iv
        )
    }
}

interface CryptSchema {
    fun encryptData(data: ByteArray, publicKey: PublicKey, iv: ByteArray): TicketingEncryptedDgcData
}

class RsaOaepWithSha256AesGcm : CryptSchema {
    /**
     * encrypt Data.
     * @param data data
     * @param publicKey publicKey
     * @param iv iv
     * @return EncryptedData
     */
    override fun encryptData(data: ByteArray, publicKey: PublicKey, iv: ByteArray): TicketingEncryptedDgcData {
        if (iv.size > 16 || iv.size < 16 || iv.size % 8 > 0) {
            throw InvalidKeySpecException()
        }
        val keyGen: KeyGenerator = KeyGenerator.getInstance("AES")
        keyGen.init(256) // for example
        val secretKey: SecretKey = keyGen.generateKey()
        val gcmParameterSpec = GCMParameterSpec(iv.size * 8, iv)
        val cipher: Cipher = Cipher.getInstance(DATA_CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
        val dataEncrypted = cipher.doFinal(data)

        // encrypt RSA key
        val keyCipher: Cipher = Cipher.getInstance(KEY_CIPHER)
        val oaepParameterSpec = OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
        )
        keyCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpec)
        val secretKeyBytes: ByteArray = secretKey.encoded
        val encKey = keyCipher.doFinal(secretKeyBytes)
        return TicketingEncryptedDgcData(dataEncrypted, encKey)
    }

    companion object {
        const val KEY_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        const val DATA_CIPHER = "AES/GCM/NoPadding"
    }
}

class DccRsaOaepWithSha256AesGcmCryptService {
    var cryptSchema: CryptSchema = RsaOaepWithSha256AesGcm()

    /**
     * encrypt Data.
     * @param data data
     * @param publicKey publicKey
     * @param encSchema encSchema
     * @param iv iv
     * @return EncryptedData
     */
    fun encryptData(data: ByteArray, publicKey: PublicKey, encSchema: String, iv: ByteArray): TicketingEncryptedDgcData {
        return if (encSchema == SUPPORTED_ENCRYPTION_SCHEMA) {
            cryptSchema.encryptData(data, publicKey, iv)
        } else {
            throw IllegalStateException("encryption schema not supported $encSchema")
        }
    }
}