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

package dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.transmission

import dgca.wallet.app.android.data.remote.ticketing.validate.BookingPortalValidationResponse
import dgca.wallet.app.android.data.remote.ticketing.validate.BookingPortalValidationResponseResult
import dgca.wallet.app.android.data.remote.ticketing.validate.ResultsItem
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.validationresult.BookingPortalLimitedValidityResultItem
import dgca.wallet.app.android.wallet.scan_import.qr.bookingsystemmodel.validationresult.BookingPortalValidationResult

fun BookingPortalValidationResponse.toValidationResult(): BookingPortalValidationResult {
    return when (this.result) {
        BookingPortalValidationResponseResult.OK -> BookingPortalValidationResult.Valid
        BookingPortalValidationResponseResult.NOK -> BookingPortalValidationResult.Invalid
        BookingPortalValidationResponseResult.CHK -> BookingPortalValidationResult.LimitedValidity(this.results.toLimitedValidityResultItems())
    }
}

fun ResultsItem.toLimitedValidityResultItems(): BookingPortalLimitedValidityResultItem = BookingPortalLimitedValidityResultItem(
    result = result,
    identifier = identifier,
    details = details,
    type = type
)

fun List<ResultsItem>.toLimitedValidityResultItems(): List<BookingPortalLimitedValidityResultItem> = map {
    it.toLimitedValidityResultItems()
}