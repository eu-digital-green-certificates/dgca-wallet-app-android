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
 *  Created by osarapulov on 9/10/21 11:48 AM
 */

package dgca.wallet.app.android.model

import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.ticketing.data.checkin.TicketingCheckInRemote
import org.apache.commons.io.IOUtils
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.InputStream
import java.nio.charset.Charset

@RunWith(MockitoJUnitRunner::class)
class TicketingCheckInParcelableTest {
    private val objectMapper = ObjectMapper().apply { findAndRegisterModules() }

    @Test
    fun shouldConvertBookingSystemModel() {
        val bookingSystemModelIs: InputStream =
            javaClass.classLoader!!.getResourceAsStream(BOOKING_SYSTEM_MODEL_FILE_NAME)
        val bookingSystemModelString: String = IOUtils.toString(bookingSystemModelIs, Charset.defaultCharset())
        val ticketingCheckInRemoteModel: TicketingCheckInRemote =
            objectMapper.readValue(bookingSystemModelString, TicketingCheckInRemote::class.java)
        assertNotNull(ticketingCheckInRemoteModel)
    }

    companion object {
        const val BOOKING_SYSTEM_MODEL_FILE_NAME = "ticketing_check_in_remote_model.json"
    }
}