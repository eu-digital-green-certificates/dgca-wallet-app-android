/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by mykhailo.nester on 17/08/2021, 18:13
 */

package dgca.wallet.app.android.dcc.nfc

import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.android.app.dcc.R

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex(): String {
    val result = StringBuffer()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}


fun Fragment.showTurnOnNfcDialog() {
    val nfcDialog = AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.nfc_turn_on_title))
        .setMessage(getString(R.string.nfc_turn_on_message))
        .setPositiveButton(getString(R.string.nfc_turn_on_positive)) { dialog, _ ->
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            dialog.dismiss()
        }
        .setNegativeButton(getString(R.string.nfc_turn_on_negative)) { dialog, _ -> dialog.dismiss() }
        .create()
    nfcDialog.show()
}