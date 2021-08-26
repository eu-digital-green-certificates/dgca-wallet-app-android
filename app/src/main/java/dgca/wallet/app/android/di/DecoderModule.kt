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
 *  Created by Mykhailo Nester on 4/23/21 9:48 AM
 */

package dgca.wallet.app.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.decoder.CertificateDecoder
import dgca.verifier.app.decoder.DefaultCertificateDecoder
import dgca.verifier.app.decoder.base45.Base45Decoder
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.base45.DefaultBase45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.cbor.DefaultCborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.compression.DefaultCompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.cose.DefaultCoseService
import dgca.verifier.app.decoder.cose.VerificationCryptoService
import dgca.verifier.app.decoder.prefixvalidation.DefaultPrefixValidationService
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.DefaultSchemaValidator
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.decoder.services.X509
import dgca.wallet.app.android.certificate.DefaultGreenCertificateFetcher
import dgca.wallet.app.android.certificate.GreenCertificateFetcher
import javax.inject.Singleton

/**
 * Provide QR decoder functionality for injection.
 */
@ExperimentalUnsignedTypes
@InstallIn(SingletonComponent::class)
@Module
object DecoderModule {

    @Singleton
    @Provides
    fun provideBase45Decoder(): Base45Decoder = Base45Decoder()

    @Singleton
    @Provides
    fun provideCertificateDecoder(base45Decoder: Base45Decoder): CertificateDecoder = DefaultCertificateDecoder(base45Decoder)

    @Singleton
    @Provides
    fun providePrefixValidationService(): PrefixValidationService = DefaultPrefixValidationService()

    @Singleton
    @Provides
    fun provideBase45ServiceDecoder(): Base45Service = DefaultBase45Service()

    @Singleton
    @Provides
    fun provideCompressorService(): CompressorService = DefaultCompressorService()

    @Singleton
    @Provides
    fun provideCoseService(): CoseService = DefaultCoseService()

    @Singleton
    @Provides
    fun provideSchemaValidator(): SchemaValidator = DefaultSchemaValidator()

    @Singleton
    @Provides
    fun provideCborService(): CborService = DefaultCborService()

    @Singleton
    @Provides
    fun provideX509(): X509 = X509()

    @Singleton
    @Provides
    fun provideCryptoService(x509: X509): CryptoService = VerificationCryptoService(x509)

    @Singleton
    @Provides
    fun provide(
        prefixValidationService: PrefixValidationService,
        base45Service: Base45Service,
        compressorService: CompressorService,
        coseService: CoseService,
        schemaValidator: SchemaValidator,
        cborService: CborService,
    ): GreenCertificateFetcher = DefaultGreenCertificateFetcher(
        prefixValidationService,
        base45Service,
        compressorService,
        coseService,
        schemaValidator,
        cborService
    )
}