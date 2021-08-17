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
 *  Created by mykhailo.nester on 17/08/2021, 18:54
 */

package dgca.wallet.app.android.nfc

import android.nfc.NdefRecord
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.util.Arrays
import kotlin.experimental.and

class TextRecord(
    val languageCode: String,
    val text: String
) : ParsedNdefRecord {

    override fun str(): String {
        return text
    }

    companion object {

        fun parse(record: NdefRecord): ParsedNdefRecord? {
            return if (record.tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.type, NdefRecord.RTD_TEXT)) {
                try {
                    val payload = record.payload
                    /*
                          * payload[0] contains the "Status Byte Encodings" field, per the
                          * NFC Forum "Text Record Type Definition" section 3.2.1.
                          *
                          * bit7 is the Text Encoding Field.
                          *
                          * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
                          * The text is encoded in UTF16
                          *
                          * Bit_6 is reserved for future use and must be set to zero.
                          *
                          * Bits 5 to 0 are the length of the IANA language code.
                          */
                    val textEncoding = if (payload[0] and 128.toByte() == 0.toByte()) {
                        Charsets.UTF_8
                    } else {
                        Charsets.UTF_16
                    }

                    val languageCodeLength: Int = (payload[0] and 63.toByte()).toInt()
                    val languageCode = String(payload, 1, languageCodeLength, StandardCharsets.US_ASCII)
                    val text = String(
                        payload, languageCodeLength + 1,
                        payload.size - languageCodeLength - 1, textEncoding
                    )
                    return TextRecord(languageCode, text)
                } catch (e: UnsupportedEncodingException) {
                    // should never happen unless we get a malformed tag.
                    throw IllegalArgumentException(e)
                }
            } else {
                null
            }
        }

        fun isText(record: NdefRecord): Boolean {
            return try {
                parse(record)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }
    }
}