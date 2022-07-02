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
 *  Created by osarapulov on 10/2/21 5:30 PM
 */

package feature.ticketing.presentation.transmission

import feature.ticketing.data.remote.validate.BookingPortalValidationResponse
import feature.ticketing.data.remote.validate.BookingPortalValidationResponseResult
import feature.ticketing.data.remote.validate.BookingPortalValidationResponseResultItem
import feature.ticketing.presentation.validationresult.BookingPortalLimitedValidityResult
import feature.ticketing.presentation.validationresult.BookingPortalLimitedValidityResultItem
import feature.ticketing.presentation.validationresult.BookingPortalValidationResult

fun BookingPortalValidationResponse.toValidationResult(): BookingPortalValidationResult {
    return when (this.result) {
        BookingPortalValidationResponseResult.OK -> BookingPortalValidationResult.Valid
        BookingPortalValidationResponseResult.NOK -> BookingPortalValidationResult.Invalid
        BookingPortalValidationResponseResult.CHK -> BookingPortalValidationResult.LimitedValidity(this.resultValidations.toLimitedValidityResultItems())
    }
}

fun BookingPortalValidationResponseResultItem.toLimitedValidityResultItems(): BookingPortalLimitedValidityResultItem =
    BookingPortalLimitedValidityResultItem(
        result = result.toBookingPortalValidationResponseResult(),
        identifier = identifier,
        details = details,
        type = type
    )

fun List<BookingPortalValidationResponseResultItem>.toLimitedValidityResultItems(): List<BookingPortalLimitedValidityResultItem> =
    map {
        it.toLimitedValidityResultItems()
    }

fun BookingPortalValidationResponseResult.toBookingPortalValidationResponseResult(): BookingPortalLimitedValidityResult =
    when (this) {
        BookingPortalValidationResponseResult.OK -> BookingPortalLimitedValidityResult.OK
        BookingPortalValidationResponseResult.NOK -> BookingPortalLimitedValidityResult.NOK
        BookingPortalValidationResponseResult.CHK -> BookingPortalLimitedValidityResult.CHK
    }