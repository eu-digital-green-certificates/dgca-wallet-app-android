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
 *  Created by osarapulov on 9/10/21 11:42 AM
 */

package feature.ticketing.presentation.model

import android.os.Parcelable
import feature.ticketing.domain.data.checkin.TicketingCheckInRemote
import kotlinx.parcelize.Parcelize

@Parcelize
data class TicketingCheckInParcelable(
    val protocol: String,
    val protocolVersion: String,
    val serviceIdentity: String,
    val privacyUrl: String,
    val token: String,
    val consent: String,
    val subject: String,
    val serviceProvider: String
) : Parcelable

fun TicketingCheckInRemote.fromRemote(): TicketingCheckInParcelable = TicketingCheckInParcelable(
    protocol = protocol,
    protocolVersion = protocolVersion,
    serviceIdentity = serviceIdentity,
    privacyUrl = privacyUrl,
    token = token,
    consent = consent,
    subject = subject,
    serviceProvider = serviceProvider
)

fun TicketingCheckInParcelable.toRemote(): TicketingCheckInRemote = TicketingCheckInRemote(
    protocol = protocol,
    protocolVersion = protocolVersion,
    serviceIdentity = serviceIdentity,
    privacyUrl = privacyUrl,
    token = token,
    consent = consent,
    subject = subject,
    serviceProvider = serviceProvider
)