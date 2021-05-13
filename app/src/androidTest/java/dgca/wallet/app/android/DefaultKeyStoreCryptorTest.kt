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
 *  Created by osarapulov on 5/13/21 2:21 PM
 */

package dgca.wallet.app.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import dgca.wallet.app.android.security.DefaultKeyStoreCryptor
import dgca.wallet.app.android.security.KeyStoreCryptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultKeyStoreCryptorTest {
    private lateinit var cryptor: KeyStoreCryptor

    companion object {
        const val MAX_BLOCK_SIZE = 214
    }

    @Before
    fun createLogHistory() {
        cryptor = DefaultKeyStoreCryptor()
    }

    @Test
    fun test() {
        val text = String(ByteArray(MAX_BLOCK_SIZE)) + "Hello World!"

        val encrypted = cryptor.encrypt(text)

        assertNotNull(encrypted)

        val decrypted = cryptor.decrypt(encrypted)

        assertEquals(text, decrypted)
    }
}