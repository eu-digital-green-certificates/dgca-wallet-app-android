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
 *  Created by osarapulov on 4/30/21 1:33 AM
 */

package dgca.wallet.app.android.security

import android.util.Base64
import timber.log.Timber
import java.security.GeneralSecurityException
import java.security.Key
import java.security.KeyPair
import javax.crypto.Cipher
import kotlin.math.ceil

/**
 * Wrapper for {@SecretKey} that provide ability to encrypt/decrypt data using it.
 */
class SecurityKeyWrapper(private val keyPair: KeyPair) {

    fun encrypt(token: String?): String? {
        if (token == null) return null
        try {
            val cipher = getCipher(Cipher.ENCRYPT_MODE, keyPair.public)
            val byteArray = token.toByteArray()
            val chunksAmount = ceil(byteArray.size.toFloat() / BLOCK_SIZE).toInt()
            val chunks = Array(chunksAmount) { ByteArray(BLOCK_SIZE) { EMPTY_BYTE } }
            val outputChunk = ByteArray(OUTPUT_BLOCK_SIZE * chunksAmount)
            for (i in 0..byteArray.lastIndex) {
                val curChunk = i / BLOCK_SIZE
                val pos = i % BLOCK_SIZE
                chunks[curChunk][pos] = byteArray[i]
            }
            var counter = 0
            for (i in 0 until chunksAmount) {
                val encrypted: ByteArray = cipher.doFinal(chunks[i])
                encrypted.forEach { encryptedByte ->
                    outputChunk[counter++] = encryptedByte
                }
            }
            return Base64.encodeToString(outputChunk, Base64.DEFAULT)
        } catch (e: GeneralSecurityException) {
            Timber.w(e)
        }
        return null
    }

    fun decrypt(encryptedToken: String?): String? {
        if (encryptedToken == null) return null
        try {
            val cipher = getCipher(Cipher.DECRYPT_MODE, keyPair.private)
            val decoded: ByteArray = Base64.decode(encryptedToken, Base64.DEFAULT)

            val chunksAmount = ceil(decoded.size.toFloat() / OUTPUT_BLOCK_SIZE).toInt()
            var counter = 0
            val originalSum = ByteArray(chunksAmount * BLOCK_SIZE) { EMPTY_BYTE }
            for (i in 0 until chunksAmount) {
                val chunk: ByteArray = ByteArray(OUTPUT_BLOCK_SIZE)
                for (j in 0 until OUTPUT_BLOCK_SIZE) {
                    chunk[j] = decoded[i * OUTPUT_BLOCK_SIZE + j]
                }
                val original: ByteArray = cipher.doFinal(chunk)
                original.forEach { originalByte ->
                    originalSum[counter++] = originalByte
                }
            }

            var resSize = originalSum.size
            while (resSize > 0 && originalSum[resSize - 1] == EMPTY_BYTE) {
                resSize--
            }
            val res = ByteArray(resSize)
            for (i in 0 until resSize) {
                res[i] = originalSum[i]
            }
            return String(res)
        } catch (e: GeneralSecurityException) {
            Timber.w(e)
        }
        return null
    }

    @Throws(GeneralSecurityException::class)
    private fun getCipher(mode: Int, key: Key) = Cipher.getInstance(RSA_ECB_PKCS1_PADDING).apply {
        init(mode, key)
    }

    companion object {
        private const val RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding"
        private const val BLOCK_SIZE = 214
        private const val OUTPUT_BLOCK_SIZE = 256
        private const val EMPTY_BYTE = Byte.MIN_VALUE
    }
}