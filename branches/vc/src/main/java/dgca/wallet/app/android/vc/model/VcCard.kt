/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
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
 *  Created by mykhailo.nester on 20/04/2022, 18:21
 */

package dgca.wallet.app.android.vc.model

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.android.app.base.ProcessorItemCard
import com.android.app.vc.R
import dgca.wallet.app.android.vc.VcProcessor
import kotlinx.parcelize.Parcelize
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class VcCard(
    val kid: String,
    val id: Int,
    val contextJson: String,
    val payload: String,
    val timeOfScanning: ZonedDateTime,
    override val isRevoked: Boolean,
) : ProcessorItemCard {

    override fun itemId(): Int = id

    override fun processorId(): String = VcProcessor.VC_PROCESSOR_ID

    override fun title(res: Resources): String = res.getString(R.string.vc_card_title)

    override fun typeTitle(res: Resources): String = "Certificate"

    override fun typeValue(res: Resources): String = ""

    override fun subTitle(res: Resources): String = ""

    override fun dateString(res: Resources): String =
        DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm").format(timeOfScanning.withZoneSameInstant(ZoneId.systemDefault()))

    override fun actionIntent(): Intent = Intent(VC_VIEW_ACTION, Uri.parse(VC_VIEW_URI)).putExtra(VC_CERTIFICATE_ID_PARAM_KEY, id)

    companion object {
        const val VC_CERTIFICATE_ID_PARAM_KEY = "dgca.wallet.app.android.vc.id_key"
        private const val VC_VIEW_ACTION = "com.android.app.vc.View"
        private const val VC_VIEW_URI = "view-certificate://vc"
    }
}
