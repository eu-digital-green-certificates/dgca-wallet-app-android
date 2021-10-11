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
 *  Created by osarapulov on 9/20/21 10:59 AM
 */

package dgca.wallet.app.android.model

import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.ticketing.data.accesstoken.TicketingAccessTokenResponse
import org.apache.commons.io.IOUtils
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.InputStream
import java.nio.charset.Charset

@RunWith(MockitoJUnitRunner::class)
class TicketingAccessTokenResponseTest {
    private val objectMapper = ObjectMapper().apply { findAndRegisterModules() }

    @Test
    fun shouldConvertAccessTokenResponseModel() {
        val accessTokenResponseModelIs: InputStream =
            javaClass.classLoader!!.getResourceAsStream(ACCESS_TOKEN_RESPONSE_MODEL_FILE_NAME)
        val accessTokenResponseModelString: String = IOUtils.toString(accessTokenResponseModelIs, Charset.defaultCharset())
        val ticketingAccessTokenResponseModel: TicketingAccessTokenResponse =
            objectMapper.readValue(accessTokenResponseModelString, TicketingAccessTokenResponse::class.java)
        assertNotNull(ticketingAccessTokenResponseModel)
    }

    companion object {
        const val ACCESS_TOKEN_RESPONSE_MODEL_FILE_NAME = "access_token_response_model.json"
    }
}