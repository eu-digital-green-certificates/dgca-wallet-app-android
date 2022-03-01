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

package dgca.wallet.app.android.wallet.scan_import.qr.ticketing.transmission

import dgca.wallet.app.android.data.remote.ticketing.validate.BookingPortalValidationResponse
import dgca.wallet.app.android.data.remote.ticketing.validate.BookingPortalValidationResponseResult
import dgca.wallet.app.android.data.remote.ticketing.validate.BookingPortalValidationResponseResultItem
import dgca.wallet.app.android.wallet.scan_import.qr.ticketing.validationresult.BookingPortalLimitedValidityResult
import dgca.wallet.app.android.wallet.scan_import.qr.ticketing.validationresult.BookingPortalRuleResultItem
import dgca.wallet.app.android.wallet.scan_import.qr.ticketing.validationresult.BookingPortalValidationResult

fun BookingPortalValidationResponse.toValidationResult(): BookingPortalValidationResult {
    return when (this.result) {
        BookingPortalValidationResponseResult.OK -> BookingPortalValidationResult.Valid
        BookingPortalValidationResponseResult.NOK ->
            BookingPortalValidationResult.Invalid(this.resultValidations.toRuleResultItems())
        BookingPortalValidationResponseResult.CHK ->
            BookingPortalValidationResult.LimitedValidity(this.resultValidations.toRuleResultItems())
    }
}

fun List<BookingPortalValidationResponseResultItem>.toRuleResultItems(): List<BookingPortalRuleResultItem> =
    map {
        it.toRuleValidityResultItems()
    }

fun BookingPortalValidationResponseResultItem.toRuleValidityResultItems(): BookingPortalRuleResultItem =
    BookingPortalRuleResultItem(
        result = result.toBookingPortalValidationResponseResult(),
        identifier = identifier,
        details = details,
        type = type
    )

fun BookingPortalValidationResponseResult.toBookingPortalValidationResponseResult(): BookingPortalLimitedValidityResult =
    when (this) {
        BookingPortalValidationResponseResult.OK -> BookingPortalLimitedValidityResult.OK
        BookingPortalValidationResponseResult.NOK -> BookingPortalLimitedValidityResult.NOK
        BookingPortalValidationResponseResult.CHK -> BookingPortalLimitedValidityResult.CHK
    }