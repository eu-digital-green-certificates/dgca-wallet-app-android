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
 *  Created by osarapulov on 5/11/21 1:04 PM
 */

package dgca.wallet.app.android.data

data class CertificateModel(
    val person: PersonModel,
    val dateOfBirth: String,
    val vaccinations: List<VaccinationModel>?,
    val tests: List<TestModel>?,
    val recoveryStatements: List<RecoveryModel>?
) {
    fun getFullName(): String {
        val givenName: String? = person.givenName?.trim()
        val familyName: String? = person.familyName?.trim()
        val stringBuilder = StringBuilder()
        if (givenName?.isNotEmpty() == true) {
            stringBuilder.append(givenName)
        }
        if (familyName?.isNotEmpty() == true) {
            stringBuilder.append(" ").append(familyName)
        }
        if (stringBuilder.isEmpty()) {
            val standardisedGivenName = person.standardisedGivenName
            if (standardisedGivenName?.isNotEmpty() == true) {
                stringBuilder.append(standardisedGivenName)
            }
            val standardisedFamilyName = person.standardisedFamilyName
            if (standardisedFamilyName.isNotEmpty()) {
                stringBuilder.append(" ").append(standardisedFamilyName)
            }
        }
        return stringBuilder.trim().toString()
    }
}

data class PersonModel(
    val standardisedFamilyName: String,
    val familyName: String?,
    val standardisedGivenName: String?,
    val givenName: String?
)

data class VaccinationModel(
    override val disease: String,
    val vaccine: String,
    val medicinalProduct: String,
    val manufacturer: String,
    val doseNumber: Int,
    val totalSeriesOfDoses: Int,
    val dateOfVaccination: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateIdentifier: String
) : CertificateData

data class TestModel(
    override val disease: String,
    val typeOfTest: String,
    val testName: String?,
    val testNameAndManufacturer: String?,
    val dateTimeOfCollection: String,
    val dateTimeOfTestResult: String?,
    val testResult: String,
    val testingCentre: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateIdentifier: String,
    val resultType: TestResult
) : CertificateData

enum class TestResult(val value: String) {
    DETECTED("DETECTED"),
    NOT_DETECTED("NOT DETECTED")
}

data class RecoveryModel(
    override val disease: String,
    val dateOfFirstPositiveTest: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateValidFrom: String,
    val certificateValidUntil: String,
    val certificateIdentifier: String
) : CertificateData

interface CertificateData {
    val disease: String
}

fun CertificateModel.getCertificateListData(): List<CertificateData> {
    val list = mutableListOf<CertificateData>()
    list.addAll(vaccinations ?: emptyList())
    list.addAll(tests ?: emptyList())
    list.addAll(recoveryStatements ?: emptyList())

    return list
}