/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 3/17/22, 1:55 PM
 */

package dgca.wallet.app.android.vc.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dgca.wallet.app.android.vc.data.local.model.VcEntity

@Dao
interface VcItemDao {

    @Query("SELECT * FROM vc_item")
    suspend fun getVcItems(): List<VcEntity>

    @Query("DELETE FROM vc_item WHERE id = :itemCard")
    suspend fun deleteItem(itemCard: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVcItem(vcEntity: VcEntity): Long

    @Query("SELECT * FROM vc_item WHERE id = :certId")
    suspend fun getById(certId: Int): VcEntity?
}
