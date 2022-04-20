/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
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
 *  Created by osarapulov on 4/20/22, 11:10 AM
 */

package feature.ticketing.domain.data.accesstoken

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.junit.Assert
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
            javaClass.classLoader!!.getResourceAsStream(TICKETING_ACCESS_TOKEN_RESPONSE_FILE_NAME)
        val accessTokenResponseModelString: String =
            IOUtils.toString(accessTokenResponseModelIs, Charset.defaultCharset())
        val ticketingAccessTokenResponseModel: TicketingAccessTokenResponse =
            objectMapper.readValue(accessTokenResponseModelString, TicketingAccessTokenResponse::class.java)
        Assert.assertNotNull(ticketingAccessTokenResponseModel)
    }

    companion object {
        const val TICKETING_ACCESS_TOKEN_RESPONSE_FILE_NAME = "ticketing_access_token_response.json"
    }
}
