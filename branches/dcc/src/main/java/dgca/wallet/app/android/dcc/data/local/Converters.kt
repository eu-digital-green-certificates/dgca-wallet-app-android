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
 *  Created by osarapulov on 5/11/21 12:33 AM
 */

package dgca.wallet.app.android.dcc.data.local

import androidx.room.TypeConverter
import dgca.verifier.app.engine.UTC_ZONE_ID
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class Converters {

    @TypeConverter
    fun timestampToZonedDateTime(value: Long?): ZonedDateTime =
        if (value != null) {
            val instant: Instant = Instant.EPOCH.plus(value, ChronoUnit.MICROS)
            ZonedDateTime.ofInstant(instant, UTC_ZONE_ID)
        } else {
            ZonedDateTime.now(UTC_ZONE_ID)
        }

    @TypeConverter
    fun zonedDateTimeToTimestamp(zonedDateTime: ZonedDateTime?): Long =
        ChronoUnit.MICROS.between(
            Instant.EPOCH,
            (zonedDateTime?.withZoneSameInstant(UTC_ZONE_ID)
                ?: ZonedDateTime.now(UTC_ZONE_ID)).toInstant()
        )
}
