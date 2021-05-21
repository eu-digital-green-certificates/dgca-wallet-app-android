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
 *  Created by mykhailo.nester on 5/11/21 9:19 PM
 */

package dgca.wallet.app.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dgca.wallet.app.android.data.ConfigRepository
import dgca.wallet.app.android.data.ConfigRepositoryImpl
import dgca.wallet.app.android.data.WalletRepository
import dgca.wallet.app.android.data.WalletRepositoryImpl
import dgca.wallet.app.android.data.local.LocalConfigDataSource
import dgca.wallet.app.android.data.local.MutableConfigDataSource
import dgca.wallet.app.android.data.remote.DefaultRemoteConfigDataSource
import dgca.wallet.app.android.data.remote.RemoteConfigDataSource
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindWalletRepository(repository: WalletRepositoryImpl): WalletRepository


    @Singleton
    @Binds
    abstract fun bindLocalConfigDataSource(configDataSource: LocalConfigDataSource): MutableConfigDataSource

    @Singleton
    @Binds
    abstract fun bindRemoteConfigDataSource(configDataSourceDefault: DefaultRemoteConfigDataSource): RemoteConfigDataSource

    @Singleton
    @Binds
    abstract fun bindConfigRepository(configRepository: ConfigRepositoryImpl): ConfigRepository
}