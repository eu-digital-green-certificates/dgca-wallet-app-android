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

package dgca.wallet.app.android.data

import dgca.wallet.app.android.data.remote.ApiResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

abstract class BaseRepository : Repository {

    suspend fun <P> execute(doOnAsyncBlock: suspend () -> P): P? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                doOnAsyncBlock.invoke()
            } catch (throwable: Throwable) {
                Timber.w(throwable, "Throwable")
                null
            }
        }
    }

    suspend fun <P> doApiBackgroundWork(doOnAsyncBlock: suspend CoroutineScope.() -> Response<P>): ApiResult<P> =
        doCoroutineWork(doOnAsyncBlock, Dispatchers.IO)

    private suspend inline fun <P> doCoroutineWork(
        crossinline doOnAsyncBlock: suspend CoroutineScope.() -> Response<P>,
        context: CoroutineContext
    ): ApiResult<P> {
        var error: ApiResult<P>? = null
        val response = withContext(context) {
            return@withContext try {
                Timber.v("Do network coroutine work")
                doOnAsyncBlock.invoke(this)
            } catch (e: UnknownHostException) {
                Timber.w(e, "UnknownHostException")
                error = ApiResult.error(
                    error = e.toString(),
                    errorDescription = "Server is unreachable. Please, check network connection."
                )
                null
            } catch (e: SocketTimeoutException) {
                Timber.w(e, "SocketTimeoutException")
                error = ApiResult.error(
                    error = e.toString(),
                    errorDescription = "No internet connection"
                )
                null
            } catch (throwable: Throwable) {
                Timber.w(throwable, "Throwable")
                error = ApiResult.error(
                    error = throwable.toString(),
                    errorDescription = "Unknown exception. Please, try again."
                )
                null
            }
        }

        return if (error != null) {
            error!!
        } else {
            ApiResult(response)
        }
    }
}