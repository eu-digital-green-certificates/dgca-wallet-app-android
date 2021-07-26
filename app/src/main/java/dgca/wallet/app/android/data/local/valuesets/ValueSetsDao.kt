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
 *  Created by osarapulov on 7/26/21 12:09 PM
 */

package dgca.wallet.app.android.data.local.valuesets

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class ValueSetsDao {
    @Query("SELECT * from valuesets")
    abstract fun getAll(): List<ValueSetLocal>

    @Insert
    abstract fun insert(vararg valueSetsLocal: ValueSetLocal)

    @Query("DELETE FROM valuesets")
    abstract fun deleteAll()
}