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
 *  Created by osarapulov on 10/2/21 5:02 PM
 */

package dgca.wallet.app.android.data.remote.ticketing

import com.fasterxml.jackson.databind.ObjectMapper
import dgca.wallet.app.android.data.remote.ticketing.validate.BookingPortalValidationResponse
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission.toValidationResult
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.validationresult.BookingPortalValidationResult
import junit.framework.Assert.*
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.InputStream
import java.nio.charset.Charset

@RunWith(MockitoJUnitRunner::class)
class BookingPortalValidationResponseTest {
    private val objectMapper = ObjectMapper().apply { findAndRegisterModules() }

    @Test
    fun shouldConvertBookingPortalValidationResponseModel() {
        val bookingPortalValidationResponseValid: BookingPortalValidationResponse =
            readFrom(BOOKING_PORTAL_VALIDATION_RESULT_VALID_FILE_NAME)
        val bookingPortalValidationResponseInvalid: BookingPortalValidationResponse =
            readFrom(BOOKING_PORTAL_VALIDATION_RESULT_INVALID_FILE_NAME)
        val bookingPortalValidationResponseLimitedValidity: BookingPortalValidationResponse =
            readFrom(BOOKING_PORTAL_VALIDATION_RESULT_LIMITED_VALIDITY_FILE_NAME)
        assertNotNull(bookingPortalValidationResponseValid)
        assertTrue(bookingPortalValidationResponseValid.toValidationResult() is BookingPortalValidationResult.Valid)
        assertNotNull(bookingPortalValidationResponseInvalid)
        assertTrue(bookingPortalValidationResponseInvalid.toValidationResult() is BookingPortalValidationResult.Invalid)
        assertNotNull(bookingPortalValidationResponseLimitedValidity)
        assertTrue(bookingPortalValidationResponseLimitedValidity.toValidationResult() is BookingPortalValidationResult.LimitedValidity)
    }

    private inline fun <reified T> readFrom(fileName: String): T {
        val `is`: InputStream = javaClass.classLoader!!.getResourceAsStream(fileName)
        val string: String = IOUtils.toString(`is`, Charset.defaultCharset())
        return objectMapper.readValue(string, T::class.java)
    }

    companion object {
        const val BOOKING_PORTAL_VALIDATION_RESULT_VALID_FILE_NAME = "booking_portal_validation_result_valid.json"
        const val BOOKING_PORTAL_VALIDATION_RESULT_INVALID_FILE_NAME = "booking_portal_validation_result_invalid.json"
        const val BOOKING_PORTAL_VALIDATION_RESULT_LIMITED_VALIDITY_FILE_NAME =
            "booking_portal_validation_result_limited_validity.json"
    }
}