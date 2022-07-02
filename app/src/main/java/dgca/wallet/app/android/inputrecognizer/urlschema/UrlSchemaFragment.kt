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
 *  Created by osarapulov on 8/25/21 4:40 PM
 */

package dgca.wallet.app.android.inputrecognizer.urlschema

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import dgca.wallet.app.android.inputrecognizer.InputRecognizerDataHandlerFragment
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.databinding.FragmentUrlSchemaBinding

/**
 * To launch Url Schema fragment use command:
 * adb shell am start -n dgca.wallet.app.android/dgca.wallet.app.android.ui.MainActivity -a dgca.wallet.app.android.INTENT -e "DATA_PARAM" DCC
 */
@AndroidEntryPoint
class UrlSchemaFragment : InputRecognizerDataHandlerFragment<FragmentUrlSchemaBinding>() {

    private val args: UrlSchemaFragmentArgs by navArgs()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentUrlSchemaBinding =
        FragmentUrlSchemaBinding.inflate(inflater, container, false)

    override fun getData() = args.data
}
