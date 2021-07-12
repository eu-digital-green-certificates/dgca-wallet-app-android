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
 *  Created by mykhailo.nester on 5/14/21 2:03 PM
 */

package dgca.wallet.app.android.data.remote

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.ResponseBody
import retrofit2.Response

class ApiResult<T>(response: Response<T>? = null) {

    var success: T? = response?.body()
    var error: CommonError? = response?.errorBody().parseError(response)
    var rawResponse: Response<T>? = response

    companion object {

        fun <T> error(
            error: String = "Unknown",
            errorDescription: String = "Unknown exception."
        ): ApiResult<T> {
            val result = ApiResult<T>()
            result.error =
                CommonError(problem = error, details = errorDescription)
            return result
        }
    }

    private fun ResponseBody?.parseError(response: Response<T>?): CommonError? {
        var error: CommonError? =
            try {
                var responseError: CommonError?
                this?.charStream().use { reader ->
                    val json = reader?.readText()
                    val result: CommonError? = ObjectMapper().readValue(json, CommonError::class.java)

                    responseError = if (result != null && result.details.isEmpty()) {
                        null
                    } else {
                        result
                    }
                }
                responseError
            } catch (e: Exception) {
                null
            }

        if (this != null && error == null) {
            error = CommonError(problem = "unknown", details = response?.message() ?: "")
        }

        return error
    }
}

data class CommonError(
    val problem: String,
    val details: String,
    val code: String = "",
    val sendValue: String = ""
)