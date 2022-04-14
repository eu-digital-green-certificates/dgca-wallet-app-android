package com.android.app.base

import android.content.Intent
import android.content.res.Resources
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*-
 * ---license-start
 * eu-digital-green-certificates / dgc-certlogic-android
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 *
 * Created by osarapulov on 24.11.21 13:08
 */
interface ProcessorItemCard : BaseItem, Parcelable {
    fun itemId(): Int
    fun processorId(): String
    fun title(res: Resources): String
    fun typeTitle(res: Resources): String
    fun typeValue(res: Resources): String
    fun subTitle(res: Resources): String
    fun dateString(res: Resources): String
    fun actionIntent(): Intent
    val isRevoked: Boolean
}