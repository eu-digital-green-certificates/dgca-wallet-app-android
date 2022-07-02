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
 *  Created by osarapulov on 9/20/21 1:48 PM
 */

package feature.ticketing.presentation.serviceselector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import feature.ticketing.presentation.TicketingServiceParcelable
import javax.inject.Inject

data class ValidationServicesContainer(
    val selectedTicketingServiceParcelable: TicketingServiceParcelable? = null,
    val selectableValidationServiceModelList: List<SelectableValidationServiceModel>
)

@HiltViewModel
class ValidationServiceViewModel @Inject constructor() : ViewModel() {
    private val _validationServicesContainer = MutableLiveData<ValidationServicesContainer>()
    val validationServicesContainer: LiveData<ValidationServicesContainer> = _validationServicesContainer

    private lateinit var validationTicketingServiceParcelables: List<TicketingServiceParcelable>
    private var selected: SelectableValidationServiceModel? = null

    fun init(validationTicketingServiceParcelables: List<TicketingServiceParcelable>) {
        if (!this::validationTicketingServiceParcelables.isInitialized) {
            this.validationTicketingServiceParcelables = validationTicketingServiceParcelables
            val selectableValidationServiceModelList = mutableListOf<SelectableValidationServiceModel>()
            this.validationTicketingServiceParcelables.forEachIndexed { index, service ->
                selectableValidationServiceModelList.add(
                    SelectableValidationServiceModel(
                        index.toString(),
                        service.name,
                        service.serviceEndpoint
                    )
                )
            }
            _validationServicesContainer.value = ValidationServicesContainer(null, selectableValidationServiceModelList)
        }
    }

    fun onValidationServiceSelected(position: Int) {
        val validationServicesContainer: ValidationServicesContainer = validationServicesContainer.value!!
        val list: MutableList<SelectableValidationServiceModel> =
            validationServicesContainer.selectableValidationServiceModelList.toMutableList()
        val selectedTicketingServiceParcelable: TicketingServiceParcelable = validationTicketingServiceParcelables[position]
        selected?.let { model ->
            val index = list.indexOfFirst { model == it }
            if (index != -1) {
                list[index] = model.copy(selected = false)
            }
        }

        val copy = list[position].copy(selected = true)
        selected = copy
        list[position] = copy

        _validationServicesContainer.value = ValidationServicesContainer(selectedTicketingServiceParcelable, list.toList())
    }
}