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
 *  Created by mykhailo.nester on 21/04/2022, 19:46
 */

package dgca.wallet.app.android.vc.ui.certview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.wallet.app.android.vc.data.VcRepository
import dgca.wallet.app.android.vc.model.DataItem
import dgca.wallet.app.android.vc.model.PayloadData
import dgca.wallet.app.android.vc.setupPathProviders
import dgca.wallet.app.android.vc.tryFetchObject
import dgca.wallet.app.android.vc.ui.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewCertificateViewModel @Inject constructor(
    private val vcRepository: VcRepository
) : ViewModel() {

    private val _event = MutableLiveData<Event<ViewEvent>>()
    val event: LiveData<Event<ViewEvent>> = _event

    init {
        setupPathProviders()
    }

    fun init(certId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = vcRepository.getVcItemById(certId)
            if (result == null) {
                _event.postValue(Event(ViewEvent.OnItemNotFound))
                return@launch
            }

            val contextJson = result.contextJson
            val payload = result.payload

            parsePayload(contextJson, payload)
        }
    }

    fun deleteItem(certificateId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = vcRepository.deleteItem(certificateId)
            _event.postValue(Event(ViewEvent.OnDeleteItem(result)))
        }
    }

    private fun parsePayload(contextJson: String, payload: String) {
        val payloadData = Gson().fromJson(contextJson, PayloadData::class.java)

        val headers = mutableListOf<DataItem>()
        payloadData.header.forEach { (path, payloadItem) ->
            payload.tryFetchObject(path, payloadItem.title, headers)
        }

        val items = mutableListOf<DataItem>()
        payloadData.body.forEach { (path, payloadItem) ->
            payload.tryFetchObject(path, payloadItem.title, items)
        }

        _event.postValue(Event(ViewEvent.OnItemAvailable(headers, items, payload)))
    }

    sealed class ViewEvent {
        object OnItemNotFound : ViewEvent()
        data class OnItemAvailable(val headers: MutableList<DataItem>, val payloadItems: List<DataItem>, val json: String) :
            ViewEvent()

        data class OnDeleteItem(val isDeleted: Boolean) : ViewEvent()
    }
}
