package dgca.wallet.app.android.dcc.di

import android.content.Context
import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dgca.wallet.app.android.dcc.DccProcessor
import dgca.wallet.app.android.dcc.data.wallet.WalletRepository
import dgca.wallet.app.android.dcc.ui.wallet.certificates.view.DefaultShareImageIntentProvider
import dgca.wallet.app.android.dcc.ui.wallet.certificates.view.ShareImageIntentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
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
import dgca.wallet.app.android.dcc.data.local.Converters
import dgca.wallet.app.android.dcc.utils.base64.Base64Coder
import dgca.wallet.app.android.dcc.utils.base64.DefaultBase64Coder
import dgca.wallet.app.android.dcc.utils.jwt.DefaultJwtTokenGenerator
import dgca.wallet.app.android.util.jwt.JwtTokenGenerator
import feature.ticketing.domain.checkin.TicketingCheckInModelFetcher
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DccModule {

    @Singleton
    @Provides
    fun provideShareImageIntentProvider(@ApplicationContext context: Context): ShareImageIntentProvider =
        DefaultShareImageIntentProvider(context)

    @Provides
    @IntoSet
    @ProcessorMarker
    fun provideDccProcessor(
        @ApplicationContext context: Context,
        validationService: PrefixValidationService,
        base45Service: Base45Service,
        compressorService: CompressorService,
        coseService: CoseService,
        walletRepository: WalletRepository,
        ticketingCheckInModelFetcher: TicketingCheckInModelFetcher
    ): Processor = DccProcessor(
        context,
        validationService,
        base45Service,
        compressorService,
        coseService,
        walletRepository,
        ticketingCheckInModelFetcher
    )

    @Singleton
    @Provides
    fun providePrefixValidationService(): PrefixValidationService = DefaultPrefixValidationService()

    @ExperimentalUnsignedTypes
    @Singleton
    @Provides
    fun provideBase45Service(): Base45Service = DefaultBase45Service()

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

    @ExperimentalUnsignedTypes
    @Singleton
    @Provides
    fun provideBase45Decoder(): Base45Decoder = Base45Decoder()

    @ExperimentalUnsignedTypes
    @Singleton
    @Provides
    fun provideCertificateDecoder(base45Decoder: Base45Decoder): CertificateDecoder = DefaultCertificateDecoder(base45Decoder)

    @Singleton
    @Provides
    fun provideObjectMapper(): ObjectMapper {
        return jacksonObjectMapper().apply {
            findAndRegisterModules()
        }
    }

    @Singleton
    @Provides
    fun provideBase64Code(): Base64Coder {
        return DefaultBase64Coder()
    }

    @Singleton
    @Provides
    fun provideJwtTokenGenerator(
        objectMapper: ObjectMapper,
        base64Coder: Base64Coder
    ): JwtTokenGenerator {
        return DefaultJwtTokenGenerator(objectMapper, base64Coder)
    }

    @Singleton
    @Provides
    fun provideConverter(): Converters = Converters()
}
