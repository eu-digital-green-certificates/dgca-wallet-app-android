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
 *  Created by osarapulov on 9/22/21 5:19 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission

import android.util.Base64
import com.fasterxml.jackson.annotation.JsonProperty
import dgca.verifier.app.decoder.base64ToX509Certificate
import dgca.wallet.app.android.data.remote.ticketing.TicketingApiService
import dgca.wallet.app.android.data.remote.ticketing.access.token.ValidateRequest
import dgca.wallet.app.android.data.remote.ticketing.identity.PublicKeyJwkRemote
import dgca.wallet.app.android.model.BookingPortalEncryptionData
import io.jsonwebtoken.Jwts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import java.time.Duration
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap


class ValidationUseCase(var ticketingApiService: TicketingApiService) {
    val dccCryptService = DccCryptService()
    val keyProvider = KeyProvider()
    val dccSign: DccSign = DccSign()

    suspend fun run(qrString: String, bookingPortalEncryptionData: BookingPortalEncryptionData) =
        withContext(Dispatchers.IO) {
            val token = "token goes here"
            val authTokenHeader = "Bearer ${token}"
            val sig = "sig"
            val encKey = "encKey"
            val validationRequest = ValidateRequest(kid = "", dcc = "", sig = sig, encKey = encKey)
            val iv = byteArrayOf(0, 0, 1, 5, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            val publicKeyJwkRemote: PublicKeyJwkRemote =
                bookingPortalEncryptionData.validationServiceIdentityResponse.getEncryptionPublicKey()!!
            val publicKey: PublicKey = try {
                publicKeyJwkRemote.x5c.base64ToX509Certificate()!!.publicKey
            } catch (exception: Exception) {
                throw IllegalStateException()
            }
            val encodedDcc: ByteArray = encodeDcc(qrString, validationRequest, iv, publicKey)
            validationRequest.kid = publicKeyJwkRemote.kid
            validationRequest.sig = dccSign.signDcc(encodedDcc, bookingPortalEncryptionData.keyPair.private)

            val accessTokenPayload: AccessTokenPayload = createAccessTocken()
            val accessToken: String = accessTokenBuilder.payload(accessTokenPayload).build(parsePrivateKey(EC_PRIVATE_KEY), "kid")

            println("jwt: $accessToken")

            val resultToken: String = validationService.validate(dccValidationRequest, accessTokenPayload)

            val jwtToken = Jwts.parser().setSigningKey(
                keyProvider.receiveCertificate(keyProvider.getKeyNames(KeyType.ValidationServiceSignKey)[0])!!
                    .publicKey
            ).parse(resultToken)
            val res = ticketingApiService.validate(
                bookingPortalEncryptionData.accessTokenResponse.validationUrl,
                authTokenHeader,
                validationRequest
            )
            if (res.isSuccessful || res.code() != HttpURLConnection.HTTP_OK) throw IllegalStateException()
        }

    private fun encodeDcc(dcc: String, dccValidationRequest: ValidateRequest, iv: ByteArray, publicKey: PublicKey): ByteArray {
        val encryptedData: EncryptedData = dccCryptService.encryptData(
            dcc.toByteArray(StandardCharsets.UTF_8),
            publicKey,
            "RSAOAEPWithSHA256AESGCM", iv
        )
        dccValidationRequest.dcc = Base64.encodeToString(encryptedData.dataEncrypted, Base64.NO_WRAP)
        dccValidationRequest.encKey = Base64.encodeToString(encryptedData.encKey, Base64.NO_WRAP)
        return encryptedData.dataEncrypted!!
    }
}

data class EncryptedData(
    var dataEncrypted: ByteArray? = null,
    var encKey: ByteArray? = null
)

interface CryptSchema {
    fun encryptData(data: ByteArray?, publicKey: PublicKey?, iv: ByteArray?): EncryptedData?
    fun decryptData(encryptedData: EncryptedData?, privateKey: PrivateKey?, iv: ByteArray?): ByteArray?
    fun getEncSchema(): String
}

class RsaOaepWithSha256AesGcm : CryptSchema {
    /**
     * encrypt Data.
     * @param data data
     * @param publicKey publicKey
     * @param iv iv
     * @return EncryptedData
     */
    override fun encryptData(data: ByteArray?, publicKey: PublicKey?, iv: ByteArray?): EncryptedData? {
        var iv = iv
        return try {
            if (iv == null) {
                iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            } else if (iv.size > 16 || iv.size < 16 || iv.size % 8 > 0) {
                throw InvalidKeySpecException()
            }
            val encryptedData = EncryptedData()
            val keyGen: KeyGenerator = KeyGenerator.getInstance("AES")
            keyGen.init(256) // for example
            val secretKey: SecretKey = keyGen.generateKey()
            val gcmParameterSpec = GCMParameterSpec(iv.size * 8, iv)
            val cipher: Cipher = Cipher.getInstance(DATA_CIPHER)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
            encryptedData.dataEncrypted = cipher.doFinal(data)

            // encrypt RSA key
            val keyCipher: Cipher = Cipher.getInstance(KEY_CIPHER)
            val oaepParameterSpec = OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
            )
            keyCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpec)
            val secretKeyBytes: ByteArray = secretKey.encoded
            encryptedData.encKey = keyCipher.doFinal(secretKeyBytes)
            encryptedData
        } catch (e: Exception) {
            throw IllegalStateException()
        }
    }

    /**
     * decrypt Data.
     * @param encryptedData encryptedData
     * @param privateKey privateKey
     * @param iv iv
     * @return dycrypted data
     */
    override fun decryptData(encryptedData: EncryptedData?, privateKey: PrivateKey?, iv: ByteArray?): ByteArray? {
        var iv = iv
        return try {
            if (iv == null) {
                iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            } else if (iv.size > 16 || iv.size < 16 || iv.size % 8 > 0) {
                throw InvalidKeySpecException()
            }
            // decrypt RSA key
            val keyCipher: Cipher = Cipher.getInstance(KEY_CIPHER)
            val oaepParameterSpec = OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
            )
            keyCipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParameterSpec)
            val encKey: ByteArray = keyCipher.doFinal(encryptedData!!.encKey)
            val gcmParameterSpec = GCMParameterSpec(iv.size * 8, iv)
            val cipher: Cipher = Cipher.getInstance(DATA_CIPHER)
            val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("AES")
            val secretKeySpec = SecretKeySpec(encKey, 0, encKey.size, "AES")
            val secretKey: SecretKey = secretKeyFactory.generateSecret(secretKeySpec)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
            cipher.doFinal(encryptedData.dataEncrypted)
        } catch (e: Exception) {
            throw IllegalStateException()
        }
    }

    override fun getEncSchema(): String = "RSAOAEPWithSHA256AESGCM"

    companion object {
        const val KEY_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        const val DATA_CIPHER = "AES/GCM/NoPadding"
    }
}

class DccCryptService {
    var cryptSchemaMap: MutableMap<String, CryptSchema>

    /**
     * init schemas.
     */
    init {
        cryptSchemaMap = HashMap()
        val cryptSchema: CryptSchema = RsaOaepWithSha256AesGcm()
        cryptSchemaMap[cryptSchema.getEncSchema()] = cryptSchema
    }

    /**
     * encrypt Data.
     * @param data data
     * @param publicKey publicKey
     * @param encSchema encSchema
     * @param iv iv
     * @return EncryptedData
     */
    fun encryptData(data: ByteArray, publicKey: PublicKey?, encSchema: String, iv: ByteArray?): EncryptedData {
        val cryptSchema: CryptSchema? = cryptSchemaMap[encSchema]
        return if (cryptSchema != null) {
            cryptSchema.encryptData(data, publicKey, iv)!!
        } else {
            throw IllegalStateException("encryption schema not supported $encSchema")
        }
    }

    /**
     * decrypt Data.
     * @param encryptedData encryptedData
     * @param privateKey privateKey
     * @param encSchema encSchema
     * @param iv iv
     * @return decrypted data
     */
    fun decryptData(encryptedData: EncryptedData?, privateKey: PrivateKey?, encSchema: String, iv: ByteArray?): ByteArray {
        val cryptSchema: CryptSchema? = cryptSchemaMap[encSchema]
        return if (cryptSchema != null) {
            cryptSchema.decryptData(encryptedData, privateKey, iv)!!
        } else {
            throw IllegalStateException("encryption schema not supported $encSchema")
        }
    }
}

class KeyProvider {
    var dgcConfigProperties: DgcConfigProperties = DgcConfigProperties()
    var certificates: MutableMap<String, Certificate> = HashMap()
    var privateKeys: MutableMap<String, PrivateKey> = HashMap()
    var kids: MutableMap<String, String> = HashMap()
    var algs: MutableMap<String, String> = HashMap()
    var kidToName: MutableMap<String, String> = HashMap()

    /**
     * create keys.
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws IOException IOException
     * @throws CertificateException CertificateException
     * @throws KeyStoreException KeyStoreException
     * @throws UnrecoverableEntryException UnrecoverableEntryException
     */
    fun createKeys() {
        val keyStorePassword: CharArray = dgcConfigProperties.keyStorePassword!!.toCharArray()
        Security.addProvider(BouncyCastleProvider())
        Security.setProperty("crypto.policy", "unlimited")
        val keyStore: KeyStore = KeyStore.getInstance("JKS")
        val keyFile = File(dgcConfigProperties.keyStoreFile)
        if (!keyFile.isFile) {
            throw java.lang.IllegalStateException(
                "keyfile not found on: " + keyFile
                        + " please adapt the configuration property: issuance.keyStoreFile"
            )
        }
        val certificateUtils = CertificateUtils()
        FileInputStream(dgcConfigProperties.keyStoreFile!!).use { `is` ->
            val privateKeyPassword: CharArray = dgcConfigProperties.privateKeyPassword!!.toCharArray()
            keyStore.load(`is`, privateKeyPassword)
            val keyPassword: KeyStore.PasswordProtection = KeyStore.PasswordProtection(keyStorePassword)
            for (alias in getKeyNames(KeyType.All)) {
                val entry: KeyStore.Entry = keyStore.getEntry(alias, keyPassword)
                if (entry is KeyStore.PrivateKeyEntry) {
                    val privateKeyEntry: KeyStore.PrivateKeyEntry = entry
                    val privateKey: PrivateKey = privateKeyEntry.privateKey
                    privateKeys[alias] = privateKey
                }
                val cert: X509Certificate = keyStore.getCertificate(alias) as X509Certificate
                certificates[alias] = cert
                val kid: String = certificateUtils.getCertKid(cert)!!
                kids[alias] = kid
                kidToName[kid] = alias
                if (cert.sigAlgOID.contains("1.2.840.113549.1.1.1")) {
                    algs[alias] = "RS256"
                }
                if (cert.sigAlgOID.contains("1.2.840.113549.1.1.10")) {
                    algs[alias] = "PS256"
                }
                if (cert.sigAlgOID.contains("1.2.840.10045.4.3.2")) {
                    algs[alias] = "ES256"
                }
            }
        }
    }

    fun receiveCertificate(keyName: String): Certificate? {
        return certificates[keyName]
    }

    fun receivePrivateKey(keyName: String): PrivateKey? {
        return privateKeys[keyName]
    }


    fun getKeyNames(type: KeyType): Array<String> {
        if (type == KeyType.ValidationServiceEncKey) {
            return dgcConfigProperties.encAliases!!
        }
        return if (type == KeyType.ValidationServiceSignKey) {
            dgcConfigProperties.signAliases!!
        } else dgcConfigProperties.encAliases!! + dgcConfigProperties.signAliases!!
    }

    fun getKid(keyName: String): String? {
        return kids[keyName]
    }

    fun getAlg(keyName: String): String? {
        return algs[keyName]
    }

    val activeSignKey: String
        get() = dgcConfigProperties.activeSignKey!!

    fun getKeyName(kid: String): String? {
        return kidToName[kid]
    }

    fun getKeyUse(keyName: String?): KeyUse {
        return if (setOf(dgcConfigProperties.encAliases).contains(keyName)) {
            KeyUse.enc
        } else KeyUse.sig
    }
}

enum class KeyType {
    All, ValidationServiceEncKey, ValidationServiceSignKey
}

enum class KeyUse {
    enc, sig
}

class DgcConfigProperties {
    var businessRulesDownload = GatewayDownload()
    var valueSetsDownload = GatewayDownload()
    var certificatesDownloader = GatewayDownload()

    class GatewayDownload {
        var timeInterval: Int? = null
        var lockLimit: Int? = null
    }

    var validationExpire: Duration = Duration.ofMinutes(60)
    var serviceUrl: String? = null
    var keyStoreFile: String? = null
    var keyStorePassword: String? = null
    var privateKeyPassword: String? = null

    var encAliases: Array<String>? = null

    var signAliases: Array<String>? = null
    var activeSignKey: String? = null
}

class CertificateUtils {
    var certificateFactory: CertificateFactory = CertificateFactory()

    /**
     * Calculates in DGC context used KID of [X509Certificate].
     * (KID consists of the first 8 bytes of SHA-256 Certificate HASH)
     *
     * @param x509Certificate the certificate the kid should be calculated for.
     * @return base64 encoded KID.
     */
    fun getCertKid(x509Certificate: X509Certificate): String? {
        return try {
            val hashBytes = calculateHashBytes(x509Certificate.encoded)
            val kidBytes: ByteArray = Arrays.copyOfRange(hashBytes, 0, KID_BYTE_COUNT.toInt())
            Base64.encodeToString(kidBytes, Base64.NO_WRAP)
        } catch (e: java.lang.Exception) {
            null
        }
    }

    /**
     * Calculates in DGC context used KID of [X509CertificateHolder].
     * (KID consists of the first 8 bytes of SHA-256 Certificate HASH)
     *
     * @param x509CertificateHolder the certificate the kid should be calculated for.
     * @return base64 encoded KID.
     */
    fun getCertKid(x509CertificateHolder: X509CertificateHolder): String? {
        return try {
            val hashBytes: ByteArray = calculateHashBytes(x509CertificateHolder.encoded)
            val kidBytes: ByteArray = Arrays.copyOfRange(hashBytes, 0, KID_BYTE_COUNT.toInt())
            Base64.encodeToString(kidBytes, Base64.NO_WRAP)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Calculates the SHA-256 thumbprint of [X509Certificate].
     *
     * @param x509Certificate the certificate the thumbprint should be calculated for.
     * @return 32-byte SHA-256 hash as hex encoded string
     */
    fun getCertThumbprint(x509Certificate: X509Certificate): String? {
        return try {
            calculateHash(x509Certificate.encoded)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculates the SHA-256 thumbprint of [X509CertificateHolder].
     *
     * @param x509CertificateHolder the certificate the thumbprint should be calculated for.
     * @return 32-byte SHA-256 hash as hex encoded string
     */
    fun getCertThumbprint(x509CertificateHolder: X509CertificateHolder): String? {
        return try {
            calculateHash(x509CertificateHolder.encoded)
        } catch (e: IOException) {
            null
        } catch (e: NoSuchAlgorithmException) {
            null
        }
    }

    fun convertCertificate(inputCertificate: X509Certificate): X509CertificateHolder {
        return X509CertificateHolder(inputCertificate.encoded)
    }

    fun convertCertificate(inputCertificate: X509CertificateHolder): X509Certificate {
        return try {
            certificateFactory.engineGenerateCertificate(
                ByteArrayInputStream(inputCertificate.encoded)
            ) as X509Certificate
        } catch (e: IOException) {
            throw IllegalStateException(e.message, e.cause)
        }
    }

    /**
     * Calculates SHA-256 hash of a given Byte-Array.
     *
     * @param data data to hash.
     * @return HEX-String with the hash of the data.
     */
    @Throws(NoSuchAlgorithmException::class)
    fun calculateHash(data: ByteArray?): String {
        val certHashBytes = MessageDigest.getInstance("SHA-256").digest(data)
        return Hex.toHexString(certHashBytes)
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun calculateHashBytes(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }

    companion object {
        private const val KID_BYTE_COUNT: Byte = 8
    }
}

class DccSign {
    /**
     * sign dcc.
     * @param data data
     * @param privateKey privateKey
     * @return signature as base64
     */
    fun signDcc(data: ByteArray?, privateKey: PrivateKey?): String {
        return try {
            val signature: Signature = Signature.getInstance(SIG_ALG)
            signature.initSign(privateKey)
            signature.update(data)
            Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
        } catch (e: Exception) {
            throw IllegalStateException("can not sign dcc", e)
        }
    }

    /**
     * verify Signature.
     * @param data data
     * @param sig sig
     * @param publicKey publicKey
     * @return true if ok
     */
    fun verifySignature(data: ByteArray?, sig: ByteArray?, publicKey: PublicKey?): Boolean {
        return try {
            val signature: Signature = Signature.getInstance(SIG_ALG)
            signature.initVerify(publicKey)
            signature.update(data)
            signature.verify(sig)
        } catch (e: Exception) {
            throw IllegalStateException("can not sign dcc", e)
        }
    }

    companion object {
        const val SIG_ALG = "SHA256withECDSA"
    }
}

class AccessTokenPayload {
    var jti: String? = null
    var iss: String? = null
    var iat: Long = 0
    var sub: String? = null
    var aud: String? = null
    var exp: Long = 0

    @JsonProperty("t")
    var type = 0

    @JsonProperty("v")
    var version: String? = null

    @JsonProperty("vc")
    var conditions: AccessTokenConditions? = null
}

class AccessTokenConditions {
    /**
     * hash of the dcc.
     * Not applicable for Type 1,2
     */
    var hash: String? = null

    /**
     * selected language.
     */
    var lang: String? = null

    /**
     * ICOA 930 transliterated surname (Familienname).
     */
    var fnt: String? = null

    /**
     * ICOA 930 transliterated given name.
     */
    var gnt: String? = null

    /**
     * Date of birth.
     */
    var dob: String? = null

    /**
     * Contry of Arrival.
     */
    var coa: String? = null

    /**
     * Country of Departure.
     */
    var cod: String? = null

    /**
     * Region of Arrival ISO 3166-2 without Country.
     */
    var roa: String? = null

    /**
     * Region of Departure ISO 3166-2 without Country.
     */
    var rod: String? = null

    /**
     * Acceptable Type of DCC.
     */
    var type: Array<String>? = null

    /**
     * Optional category which shall be reflected in the validation by additional rules/logic.
     * if null, Standard Business Rule Check will apply.
     */
    var category: Array<String>? = null

    /**
     * Date where te DCC must be validateable.
     */
    var validationClock: String? = null

    /**
     * DCC must be valid from this date (ISO8601 with offset).
     */
    var validFrom: String? = null

    /**
     * DCC must be valid minimum to this date (ISO8601 with offset).
     */
    var validTo: String? = null
}
