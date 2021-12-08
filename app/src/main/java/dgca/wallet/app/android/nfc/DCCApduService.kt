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
 *  Created by mykhailo.nester on 17/08/2021, 19:30
 */

package dgca.wallet.app.android.nfc

import android.app.Service
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import dgca.wallet.app.android.wallet.view.certificate.ViewCertificateFragment
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.math.BigInteger

@Suppress("PrivatePropertyName")
class DCCApduService : HostApduService() {

    private val APDU_SELECT = byteArrayOf(
        0x00.toByte(), // CLA	- Class - Class of instruction
        0xA4.toByte(), // INS	- Instruction - Instruction code
        0x04.toByte(), // P1	- Parameter 1 - Instruction parameter 1
        0x00.toByte(), // P2	- Parameter 2 - Instruction parameter 2
        0x07.toByte(), // Lc field	- Number of bytes present in the data field of the command
        0xD2.toByte(),
        0x76.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x85.toByte(),
        0x01.toByte(),
        0x01.toByte(), // NDEF Tag Application name
        0x00.toByte()  // Le field	- Maximum number of bytes expected in the data field of the response to the command
    )

    private val CAPABILITY_CONTAINER_OK = byteArrayOf(
        0x00.toByte(), // CLA	- Class - Class of instruction
        0xa4.toByte(), // INS	- Instruction - Instruction code
        0x00.toByte(), // P1	- Parameter 1 - Instruction parameter 1
        0x0c.toByte(), // P2	- Parameter 2 - Instruction parameter 2
        0x02.toByte(), // Lc field	- Number of bytes present in the data field of the command
        0xe1.toByte(), 0x03.toByte() // file identifier of the CC file
    )

    private val READ_CAPABILITY_CONTAINER = byteArrayOf(
        0x00.toByte(), // CLA	- Class - Class of instruction
        0xb0.toByte(), // INS	- Instruction - Instruction code
        0x00.toByte(), // P1	- Parameter 1 - Instruction parameter 1
        0x00.toByte(), // P2	- Parameter 2 - Instruction parameter 2
        0x0f.toByte()  // Lc field	- Number of bytes present in the data field of the command
    )

    // In the scenario that we have done a CC read, the same byte[] match
    // for ReadBinary would trigger and we don't want that in succession
    private var READ_CAPABILITY_CONTAINER_CHECK = false

    private val READ_CAPABILITY_CONTAINER_RESPONSE = byteArrayOf(
        0x00.toByte(), 0x11.toByte(), // CCLEN length of the CC file
        0x20.toByte(), // Mapping Version 2.0
        0xFF.toByte(), 0xFF.toByte(), // MLe maximum
        0xFF.toByte(), 0xFF.toByte(), // MLc maximum
        0x04.toByte(), // T field of the NDEF File Control TLV
        0x06.toByte(), // L field of the NDEF File Control TLV
        0xE1.toByte(), 0x04.toByte(), // File Identifier of NDEF file
        0xFF.toByte(), 0xFE.toByte(), // Maximum NDEF file size of 65534 bytes
        0x00.toByte(), // Read access without any security
        0xFF.toByte(), // Write access without any security
        0x90.toByte(), 0x00.toByte() // A_OKAY
    )

    private val NDEF_SELECT_OK = byteArrayOf(
        0x00.toByte(), // CLA	- Class - Class of instruction
        0xa4.toByte(), // Instruction byte (INS) for Select command
        0x00.toByte(), // Parameter byte (P1), select by identifier
        0x0c.toByte(), // Parameter byte (P1), select by identifier
        0x02.toByte(), // Lc field	- Number of bytes present in the data field of the command
        0xE1.toByte(), 0x04.toByte() // file identifier of the NDEF file retrieved from the CC file
    )

    private val NDEF_READ_BINARY = byteArrayOf(
        0x00.toByte(), // Class byte (CLA)
        0xb0.toByte() // Instruction byte (INS) for ReadBinary command
    )

    private val NDEF_READ_BINARY_NLEN = byteArrayOf(
        0x00.toByte(), // Class byte (CLA)
        0xb0.toByte(), // Instruction byte (INS) for ReadBinary command
        0x00.toByte(), 0x00.toByte(), // Parameter byte (P1, P2), offset inside the CC file
        0x02.toByte()  // Le field
    )

    private val A_OKAY = byteArrayOf(
        0x90.toByte(), // SW1	Status byte 1 - Command processing status
        0x00.toByte()   // SW2	Status byte 2 - Command processing qualifier
    )

    private val A_ERROR = byteArrayOf(
        0x6A.toByte(), // SW1	Status byte 1 - Command processing status
        0x82.toByte()   // SW2	Status byte 2 - Command processing qualifier
    )

    private val ndefId = byteArrayOf(0xE1.toByte(), 0x04.toByte())

    private var ndefUri = NdefMessage(createTextRecord("", ndefId))
    private var ndefUriBytes = ndefUri.toByteArray()
    private var ndefUriLength = fillByteArrayToFixedDimension(
        BigInteger.valueOf(ndefUriBytes.size.toLong()).toByteArray(), 2
    )

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.hasExtra(NFC_NDEF_KEY)) {
            ndefUri = NdefMessage(createTextRecord(intent.getStringExtra(NFC_NDEF_KEY) ?: "", ndefId))
            ndefUriBytes = ndefUri.toByteArray()
            ndefUriLength = fillByteArrayToFixedDimension(
                BigInteger.valueOf(ndefUriBytes.size.toLong()).toByteArray(), 2
            )
        }

        Timber.i("onStartCommand() | NDEF$ndefUri")

        return Service.START_STICKY
    }

    override fun onDeactivated(reason: Int) {
        Timber.d("Deactivated: $reason")
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        try {
            // The following flow is based on Appendix E "Example of Mapping Version 2.0 Command Flow"
            // in the NFC Forum specification
            Timber.i("processCommandApdu() | incoming commandApdu: ${commandApdu.toHex()}")

            // First command: NDEF Tag Application select (Section 5.5.2 in NFC Forum spec)
            if (APDU_SELECT.contentEquals(commandApdu)) {
                Timber.i("APDU_SELECT triggered. Our Response: ${A_OKAY.toHex()}")
                return A_OKAY
            }

            // Second command: Capability Container select (Section 5.5.3 in NFC Forum spec)
            if (CAPABILITY_CONTAINER_OK.contentEquals(commandApdu)) {
                Timber.i("CAPABILITY_CONTAINER_OK triggered. Our Response: ${A_OKAY.toHex()}")
                return A_OKAY
            }

            // Third command: ReadBinary data from CC file (Section 5.5.4 in NFC Forum spec)
            if (READ_CAPABILITY_CONTAINER.contentEquals(commandApdu) && !READ_CAPABILITY_CONTAINER_CHECK) {
                Timber.i("READ_CAPABILITY_CONTAINER triggered. Our Response: ${READ_CAPABILITY_CONTAINER_RESPONSE.toHex()}")
                READ_CAPABILITY_CONTAINER_CHECK = true
                return READ_CAPABILITY_CONTAINER_RESPONSE
            }

            // Fourth command: NDEF Select command (Section 5.5.5 in NFC Forum spec)
            if (NDEF_SELECT_OK.contentEquals(commandApdu)) {
                Timber.i("NDEF_SELECT_OK triggered. Our Response: ${A_OKAY.toHex()}")
                return A_OKAY
            }

            if (NDEF_READ_BINARY_NLEN.contentEquals(commandApdu)) {
                // Build our response
                val response = ByteArray(ndefUriLength.size + A_OKAY.size)
                System.arraycopy(ndefUriLength, 0, response, 0, ndefUriLength.size)
                System.arraycopy(A_OKAY, 0, response, ndefUriLength.size, A_OKAY.size)

                Timber.i("NDEF_READ_BINARY_NLEN triggered. Our Response: ${response.toHex()}")

                READ_CAPABILITY_CONTAINER_CHECK = false
                return response
            }

            if (commandApdu.sliceArray(0..1).contentEquals(NDEF_READ_BINARY)) {
                val offset = commandApdu.sliceArray(2..3).toHex().toInt(16)
                val length = commandApdu.sliceArray(4..4).toHex().toInt(16)

                val fullResponse = ByteArray(ndefUriLength.size + ndefUriBytes.size)
                System.arraycopy(ndefUriLength, 0, fullResponse, 0, ndefUriLength.size)
                System.arraycopy(
                    ndefUriBytes,
                    0,
                    fullResponse,
                    ndefUriLength.size,
                    ndefUriBytes.size
                )

                Timber.i("NDEF_READ_BINARY triggered. Full data: ${fullResponse.toHex()}")
                Timber.i("READ_BINARY - OFFSET: $offset - LEN: $length")

                val slicedResponse = fullResponse.sliceArray(offset until fullResponse.size)

                // Build our response
                val realLength = if (slicedResponse.size <= length) {
                    slicedResponse.size
                } else {
                    length
                }
                val response = ByteArray(realLength + A_OKAY.size)

                System.arraycopy(slicedResponse, 0, response, 0, realLength)
                System.arraycopy(A_OKAY, 0, response, realLength, A_OKAY.size)

                val responseHex = response.toHex()
                val okHex = A_OKAY.toHex()
                if (responseHex.contains(okHex)) {
                    val recordWithOkMessage = ndefUri.records[0].payload.toHex() + okHex
                    if (recordWithOkMessage.contains(responseHex)) {
                        onDccShared()
                        Timber.i("NDEF_READ_BINARY triggered. Full DCC sent")
                    }
                }
                Timber.i("NDEF_READ_BINARY triggered. Our Response: $responseHex")

                READ_CAPABILITY_CONTAINER_CHECK = false

                return response
            }
        } catch (ex: Exception) {
            return A_ERROR
        }

        // We're doing something outside our scope
        Timber.i("processCommandApdu() | out of scope")
        return A_ERROR
    }

    private fun onDccShared() {
        val intent = Intent(ViewCertificateFragment.NFC_BROADCAST).apply {
            putExtra(ViewCertificateFragment.NFC_EXTRA_DCC_SENT, true)
        }
        sendBroadcast(intent)
    }

    private fun createTextRecord(text: String, id: ByteArray): NdefRecord {
        var textBytes = ByteArray(0)
        try {
            textBytes = text.toByteArray(Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            Timber.w(e)
        }

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, id, textBytes)
    }

    private fun fillByteArrayToFixedDimension(array: ByteArray, fixedSize: Int): ByteArray {
        if (array.size == fixedSize) {
            return array
        }

        val start = byteArrayOf(0x00.toByte())
        val filledArray = ByteArray(start.size + array.size)
        System.arraycopy(start, 0, filledArray, 0, start.size)
        System.arraycopy(array, 0, filledArray, start.size, array.size)
        return fillByteArrayToFixedDimension(filledArray, fixedSize)
    }

    companion object {
        const val NFC_NDEF_KEY = "ndefMessage"
    }
}