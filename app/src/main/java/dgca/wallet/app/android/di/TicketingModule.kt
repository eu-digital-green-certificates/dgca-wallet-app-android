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
 *  Created by osarapulov on 9/16/21 2:40 PM
 */

package dgca.wallet.app.android.di

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.ticketing.DefaultJwtTokenParser
import dgca.verifier.app.ticketing.JwtTokenParser
import dgca.verifier.app.ticketing.checkin.TicketingCheckInModelFetcher
import dgca.verifier.app.ticketing.identity.GetTicketingIdentityDocumentUseCase
import dgca.verifier.app.ticketing.identity.TicketingIdentityDocumentFetcher
import dgca.verifier.app.ticketing.identity.accesstoken.GetTicketingAccessTokenUseCase
import dgca.verifier.app.ticketing.identity.accesstoken.TicketingAccessTokenFetcher
import dgca.verifier.app.ticketing.identity.validityserviceidentity.GetTicketingValidationServiceIdentityUseCase
import dgca.verifier.app.ticketing.identity.validityserviceidentity.TicketingValidationServiceIdentityFetcher
import dgca.verifier.app.ticketing.validation.TicketingValidationResultFetcher
import dgca.verifier.app.ticketing.validation.TicketingValidationUseCase
import dgca.verifier.app.ticketing.validation.encoding.DefaultTicketingDgcCryptor
import dgca.verifier.app.ticketing.validation.encoding.TicketingDgcCryptor
import dgca.verifier.app.ticketing.validation.encoding.TicketingDgcSigner
import dgca.verifier.app.ticketing.validation.encoding.TicketingValidationRequestProvider
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.data.remote.ticketing.TicketingApiService
import dgca.wallet.app.android.data.remote.ticketing.accesstoken.DefaultTicketingAccessTokenFetcher
import dgca.wallet.app.android.data.remote.ticketing.accesstoken.DefaultTicketingValidationServiceIdentityFetcher
import dgca.wallet.app.android.data.remote.ticketing.identity.DefaultTicketingIdentityDocumentFetcher
import dgca.wallet.app.android.data.remote.ticketing.validate.DefaultTicketingValidationResultFetcher
import dgca.wallet.app.android.wallet.scan_import.GreenCertificateFetcher
import dgca.wallet.app.android.wallet.scan_import.qr.ticketing.certselector.GetFilteredCertificatesUseCase
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TicketingModule {
    @Singleton
    @Provides
    internal fun provideTicketingCheckInModelFetcher(objectMapper: ObjectMapper): TicketingCheckInModelFetcher =
        TicketingCheckInModelFetcher(objectMapper)

    @Singleton
    @Provides
    internal fun provideTicketingApiService(retrofit: Retrofit): TicketingApiService {
        return retrofit.create(TicketingApiService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideIdentityDocumentFetcher(ticketingApiService: TicketingApiService): TicketingIdentityDocumentFetcher =
        DefaultTicketingIdentityDocumentFetcher(ticketingApiService)

    @Singleton
    @Provides
    internal fun provideTicketingAccessTokenFetcher(ticketingApiService: TicketingApiService): TicketingAccessTokenFetcher =
        DefaultTicketingAccessTokenFetcher(ticketingApiService)

    @Singleton
    @Provides
    internal fun provideTicketingValidationServiceIdentityFetcher(ticketingApiService: TicketingApiService): TicketingValidationServiceIdentityFetcher =
        DefaultTicketingValidationServiceIdentityFetcher(ticketingApiService)


    @Singleton
    @Provides
    internal fun provideTicketingValidationResultFetcher(ticketingApiService: TicketingApiService): TicketingValidationResultFetcher =
        DefaultTicketingValidationResultFetcher(ticketingApiService)

    @Singleton
    @Provides
    internal fun provideGetIdentityDocumentUseCase(ticketingIdentityDocumentFetcher: TicketingIdentityDocumentFetcher): GetTicketingIdentityDocumentUseCase =
        GetTicketingIdentityDocumentUseCase(ticketingIdentityDocumentFetcher)

    @Singleton
    @Provides
    internal fun provideJwtTokenParser(walletRepository: WalletRepository): JwtTokenParser = DefaultJwtTokenParser()

    @Singleton
    @Provides
    internal fun provideGetAccessTokenUseCase(
        ticketingAccessTokenFetcher: TicketingAccessTokenFetcher,
        objectMapper: ObjectMapper,
        jwtTokenParser: JwtTokenParser
    ): GetTicketingAccessTokenUseCase = GetTicketingAccessTokenUseCase(ticketingAccessTokenFetcher, objectMapper, jwtTokenParser)

    @Singleton
    @Provides
    internal fun provideGetValidationServiceIdentityUseCase(validationServiceIdentityFetcher: TicketingValidationServiceIdentityFetcher): GetTicketingValidationServiceIdentityUseCase =
        GetTicketingValidationServiceIdentityUseCase(validationServiceIdentityFetcher)


    @Singleton
    @Provides
    internal fun provideGetFilteredCertificatesUseCase(
        walletRepository: WalletRepository,
        greenCertificateFetcher: GreenCertificateFetcher
    ): GetFilteredCertificatesUseCase = GetFilteredCertificatesUseCase(walletRepository, greenCertificateFetcher)

    @Singleton
    @Provides
    internal fun provideTicketingDgcCryptor(): TicketingDgcCryptor = DefaultTicketingDgcCryptor()

    @Singleton
    @Provides
    internal fun provideTicketingDgcSigner(): TicketingDgcSigner = TicketingDgcSigner()

    @Singleton
    @Provides
    internal fun provideTicketingValidationRequestProvider(
        ticketingDgcCryptor: TicketingDgcCryptor, ticketingDgcSigner: TicketingDgcSigner
    ): TicketingValidationRequestProvider = TicketingValidationRequestProvider(ticketingDgcCryptor, ticketingDgcSigner)

    @Singleton
    @Provides
    internal fun provideTicketingValidationUseCase(
        ticketingValidationRequestProvider: TicketingValidationRequestProvider,
        validationResultFetcher: TicketingValidationResultFetcher,
        jwtTokenParser: JwtTokenParser,
        objectMapper: ObjectMapper
    ): TicketingValidationUseCase = TicketingValidationUseCase(
        ticketingValidationRequestProvider, validationResultFetcher, jwtTokenParser, objectMapper
    )
}