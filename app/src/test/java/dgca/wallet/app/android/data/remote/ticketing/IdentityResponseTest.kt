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
 *  Created by osarapulov on 9/16/21 3:09 PM
 */

package dgca.wallet.app.android.data.remote.ticketing

import com.fasterxml.jackson.databind.ObjectMapper
import dgca.wallet.app.android.data.remote.ticketing.identity.IdentityResponse
import org.apache.commons.io.IOUtils
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.InputStream
import java.nio.charset.Charset


@RunWith(MockitoJUnitRunner::class)
class IdentityResponseTest {
    private val objectMapper = ObjectMapper().apply { findAndRegisterModules() }

    @Test
    fun shouldConvertBookingSystemModel() {
        val identityResponseIs: InputStream =
            javaClass.classLoader!!.getResourceAsStream(IDENTITY_RESPONSE_FILE_NAME)
        val identityResponseString: String = IOUtils.toString(identityResponseIs, Charset.defaultCharset())
        val identityResponse: IdentityResponse =
            objectMapper.readValue(identityResponseString, IdentityResponse::class.java)
        assertNotNull(identityResponse)
    }

    companion object {
        const val IDENTITY_RESPONSE_FILE_NAME = "identity_response.json"
    }
}