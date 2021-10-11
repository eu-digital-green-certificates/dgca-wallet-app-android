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
 *  Created by osarapulov on 9/22/21 2:32 PM
 */

package dgca.wallet.app.android.model

import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.ticketing.accesstoken.TicketingValidationServiceIdentityResponse
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.InputStream
import java.nio.charset.Charset

@RunWith(MockitoJUnitRunner::class)
class ValidationTicketingServiceParcelableIdentityTest {
    private val objectMapper = ObjectMapper().apply { findAndRegisterModules() }

    @Test
    fun shouldConvertValidationServiceIdentityResponseModel() {
        val validationServiceIdentityResponseIs: InputStream =
            javaClass.classLoader!!.getResourceAsStream(VALIDATION_SERVICE_IDENTITY_RESPONSE_FILE_NAME)
        val validationServiceIdentityResponseString: String =
            IOUtils.toString(validationServiceIdentityResponseIs, Charset.defaultCharset())
        val ticketingValidationServiceIdentityResponse: TicketingValidationServiceIdentityResponse =
            objectMapper.readValue(validationServiceIdentityResponseString, TicketingValidationServiceIdentityResponse::class.java)
        Assert.assertNotNull(ticketingValidationServiceIdentityResponse)
    }

    companion object {
        const val VALIDATION_SERVICE_IDENTITY_RESPONSE_FILE_NAME = "validation_service_identity_response.json"
    }
}