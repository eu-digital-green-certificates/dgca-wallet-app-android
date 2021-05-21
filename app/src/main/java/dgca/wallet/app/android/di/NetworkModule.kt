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
 *  Created by mykhailo.nester on 5/7/21 5:23 PM
 */

package dgca.wallet.app.android.di

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.decoder.BuildConfig
import dgca.wallet.app.android.data.ConfigRepository
import dgca.wallet.app.android.data.remote.ApiService
import dgca.wallet.app.android.network.HeaderInterceptor
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton

private const val CONNECT_TIMEOUT = 30L

const val BASE_URL = "https://dgca-issuance-web.cfapps.eu10.hana.ondemand.com/"
const val SHA256_PREFIX = "sha256/"

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Singleton
    @Provides
    internal fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
        return Cache(context.cacheDir, cacheSize)
    }

    @Singleton
    @Provides
    internal fun provideRetrofit(okHttpClient: Provider<OkHttpClient>): Retrofit {
        return createRetrofit(okHttpClient)
    }

    @Singleton
    @Provides
    internal fun provideHeaderInterceptor(): Interceptor = HeaderInterceptor()

    @Provides
    internal fun provideCertificatePinner(configRepository: ConfigRepository): CertificatePinner {
        val config = configRepository.local().getConfig()
        val pinnerBuilder = CertificatePinner.Builder()
        config.versions?.values.let { versions ->
            versions?.forEach { version ->
                version.contextEndpoint?.pubKeys?.forEach { keyHash ->
                    pinnerBuilder.add(URL(version.contextEndpoint.url).host, "$SHA256_PREFIX$keyHash")
                }
                version.endpoints?.values?.forEach { endpoint ->
                    endpoint.pubKeys?.forEach { keyHash ->
                        pinnerBuilder.add(URL(endpoint.url).host, "$SHA256_PREFIX$keyHash")
                    }
                }
            }
        }
        return pinnerBuilder.build()
    }

    @Provides
    internal fun provideOkhttpClient(
        cache: Cache, interceptor: Interceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        val httpClient = getHttpClient(cache).apply {
            addInterceptor(interceptor)
            certificatePinner(certificatePinner)
        }
        addLogging(httpClient)

        return httpClient.build()
    }

    @Singleton
    @Provides
    internal fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    private fun getHttpClient(cache: Cache): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
    }

    private fun addLogging(httpClient: OkHttpClient.Builder) {
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(logging)
        }
    }

    private fun createRetrofit(okHttpClient: Provider<OkHttpClient>): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .baseUrl(BASE_URL)
            .callFactory {
                okHttpClient.get().newCall(it)
            }
            .build()
    }
}

@PublishedApi
internal inline fun Retrofit.Builder.callFactory(
    crossinline body: (Request) -> Call
) = callFactory(object : Call.Factory {
    override fun newCall(request: Request): Call = body(request)
})
