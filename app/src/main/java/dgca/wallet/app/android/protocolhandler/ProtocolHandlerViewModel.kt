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

package dgca.wallet.app.android.protocolhandler

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dgca.wallet.app.android.ProtocolHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProtocolHandlerViewModel @Inject constructor(
    private val protocolHandler: ProtocolHandler
) : ViewModel() {

    private val _protocolHandlerResult = MutableLiveData<ProtocolHandlerResult>()
    val protocolHandlerResult: LiveData<ProtocolHandlerResult> = _protocolHandlerResult

    fun init(data: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                protocolHandler.handle(data)?.let { ProtocolHandlerResult.Applicable(it) }
                    ?: ProtocolHandlerResult.NotApplicable
            }.let {
                _protocolHandlerResult.value = it
            }
        }
    }

    sealed class ProtocolHandlerResult {
        class Applicable(val intent: Intent) : ProtocolHandlerResult()
        object NotApplicable : ProtocolHandlerResult()
    }
}
