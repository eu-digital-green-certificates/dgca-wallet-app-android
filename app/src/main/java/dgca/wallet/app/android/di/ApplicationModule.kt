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
 *  Created by osarapulov on 8/17/21 8:29 AM
 */

package dgca.wallet.app.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dgca.wallet.app.android.wallet.view.certificate.DefaultShareImageIntentProvider
import dgca.wallet.app.android.wallet.view.certificate.ShareImageIntentProvider
import dgca.wallet.app.android.wallet.scan_import.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApplicationModule {

    @Singleton
    @Provides
    fun provideShareImageIntentProvider(@ApplicationContext context: Context): ShareImageIntentProvider =
        DefaultShareImageIntentProvider(context)

    @Singleton
    @Provides
    fun provideBitmapFetcher(@ApplicationContext context: Context): BitmapFetcher = DefaultBitmapFetcher(context)

    @Singleton
    @Provides
    fun provideQrCodeFetcher(): QrCodeFetcher = DefaultQrCodeFetcher()

    @Singleton
    @Provides
    fun provideFileSaver(@ApplicationContext context: Context): FileSaver = DefaultFileSaver(context)

    @Singleton
    @Provides
    fun provideUriProvider(@ApplicationContext context: Context): UriProvider = DefaultUriProvider(context)
}