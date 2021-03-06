/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-wallet-app-android
 *  ---
 *  Copyright (C) 2022 T-Systems International GmbH and all other contributors
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
 *  Created by mykhailo.nester on 26/04/2022, 23:54
 */

package dgca.wallet.app.android.vc.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

abstract class BaseUseCase<Type, Params> constructor(
    private val executionDispatcher: CoroutineDispatcher,
    private val errorHandler: ErrorHandler
) {

    private var backgroundDeferredJob: Deferred<Type> = CompletableDeferred()

    @Throws(Exception::class)
    protected abstract suspend fun invoke(params: Params): Type

    @Suppress("UNCHECKED_CAST")
    open fun execute(
        scope: CoroutineScope,
        params: Params = Any() as Params,
        onSuccess: (Type) -> Unit = {},
        onFailure: (ErrorType) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        scope.launch(executionDispatcher) {
            supervisorScope {
                backgroundDeferredJob = async { invoke(params) }
                launch(Dispatchers.Main) {
                    try {
                        onSuccess(backgroundDeferredJob.await())
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to execute ${this@BaseUseCase}")
                        onFailure(errorHandler.getError(e))
                    } finally {
                        onComplete()
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    open suspend fun execute(
        params: Params = Any() as Params,
        onSuccess: (Type) -> Unit = {},
        onFailure: (ErrorType) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        try {
            onSuccess(invoke(params))
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute ${this@BaseUseCase}")
            onFailure(errorHandler.getError(e))
        } finally {
            onComplete()
        }
    }

    @Suppress("UNCHECKED_CAST")
    open suspend fun execute(params: Params = Any() as Params): Type? =
        try {
            invoke(params)
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute ${this@BaseUseCase}")
            null
        }

    open fun cancel() {
        if (backgroundDeferredJob.isActive) {
            backgroundDeferredJob.cancel()
        }
    }
}

abstract class BaseFlowableUseCase<F, P>(private val dispatcher: CoroutineDispatcher) {

    protected abstract fun invoke(params: P): Flow<F>

    @Suppress("UNCHECKED_CAST")
    open fun execute(params: P = Any() as P): Flow<F> = invoke(params)
        .catch { Timber.e(it, "Failed to execute flow") }
        .flowOn(dispatcher)
}
