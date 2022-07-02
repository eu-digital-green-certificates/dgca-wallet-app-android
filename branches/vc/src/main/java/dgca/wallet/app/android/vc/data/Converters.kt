/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-wallet-app-android
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
 *  Created by mykhailo.nester on 13/04/2022, 15:17
 */

package dgca.wallet.app.android.vc.data

import androidx.room.TypeConverter
import dgca.wallet.app.android.vc.data.remote.model.IssuerType
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

val UTC_ZONE_ID: ZoneId = ZoneId.ofOffset("", ZoneOffset.UTC).normalized()

class Converters {

    @TypeConverter
    fun fromType(value: IssuerType): String = value.name

    @TypeConverter
    fun toType(value: String): IssuerType = IssuerType.values().find { it.name == value } ?: IssuerType.UNKNOWN

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