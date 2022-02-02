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
 *  Created by osarapulov on 5/11/21 1:04 PM
 */

package dgca.wallet.app.android.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import dgca.wallet.app.android.toZonedDateTime
import java.time.ZonedDateTime

@Parcelize
data class CertificateModel(
    val person: PersonModel,
    val dateOfBirth: String,
    val vaccinations: List<VaccinationModel>?,
    val tests: List<TestModel>?,
    val recoveryStatements: List<RecoveryModel>?
) : Parcelable {

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

    fun getValidFrom(): ZonedDateTime? = when {
        vaccinations?.isNotEmpty() == true -> {
            vaccinations.first().dateOfVaccination
        }
        recoveryStatements?.isNotEmpty() == true -> {
            recoveryStatements.first().certificateValidFrom
        }
        tests?.isNotEmpty() == true -> {
            tests.first().dateTimeOfCollection
        }
        else -> {
            null
        }
    }?.toZonedDateTime()

    fun getValidTo(): ZonedDateTime? = when {
        recoveryStatements?.isNotEmpty() == true -> {
            recoveryStatements.first().certificateValidUntil.toZonedDateTime()
        }
        else -> {
            null
        }
    }
}

@Parcelize
data class PersonModel(
    val standardisedFamilyName: String,
    val familyName: String?,
    val standardisedGivenName: String?,
    val givenName: String?
) : Parcelable

@Parcelize
data class VaccinationModel(
    override val disease: DiseaseType,
    val vaccine: VaccinePropylaxisType,
    val medicinalProduct: String,
    val manufacturer: ManufacturerType,
    val doseNumber: Int,
    val totalSeriesOfDoses: Int,
    val dateOfVaccination: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateIdentifier: String
) : CertificateData, Parcelable

@Parcelize
data class TestModel(
    override val disease: DiseaseType,
    val typeOfTest: TypeOfTest,
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
) : CertificateData, Parcelable

enum class TestResult(val value: String) {
    DETECTED("DETECTED"),
    NOT_DETECTED("NOT DETECTED")
}

enum class DiseaseType(val value: String) {
    COVID_19("COVID-19"),
    UNDEFINED("UNDEFINED")
}

enum class TypeOfTest(val value: String) {
    NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION("Nucleic acid amplification with probe detection"),
    RAPID_IMMUNOASSAY("Rapid immunoassay"),
    UNDEFINED("")
}

enum class VaccinePropylaxisType(val value: String) {
    SARS_COV_2_ANTIGEN_VACCINE("SARS-CoV-2 antigen vaccine"),
    SARS_COV_2_MRNA_VACCINE("SARS-CoV-2 mRNA vaccine"),
    COVID_19_VACCINES("covid-19 vaccines"),
    UNDEFINED("")
}

enum class ManufacturerType(val value: String) {
    ASTRA_ZENECA_AB("AstraZenecaAB"),
    BIONTECH_MANUFACTURING_GMBH("BiontechManufacturingGmbH"),
    JANSSEN_CILAG_INTERNATIONAL("Janssen-CilagInternational"),
    MODERNA_BIOTECH_SPAINS_L("ModernaBiotechSpainS.L."),
    CUREVAC_AG("CurevacAG"),
    CAN_SIGNO_BIOLOGICS("CanSinoBiologics"),
    CHINA_SINOPHARM_INTERNATIONAL_CORP_BEIJING_LOCATION("ChinaSinopharmInternationalCorp.-Beijinglocation"),
    SINOPHARM_WEIGIDA_EUROPE_PHARMACEUTICALS_R_O_PRAGUE_LOCATION("SinopharmWeiqidaEuropePharmaceuticals.r.o.-Praguelocation"),
    SINOPHARM_ZHIJUN_SHENZHEN_PHARMACEUTICAL_CO_LTD_SHENZHEN_LOCATION("SinopharmZhijun(Shenzhen)PharmaceuticalCo.Ltd.-Shenzhenlocation"),
    NOVAVAX_CZAS("NovavaxCZAS"),
    GAMALEYA_RESEARCH_INSTITUTE("GamaleyaResearchInstitute"),
    VECTOR_INSTITUTE("VectorInstitute"),
    SINOVAC_BIOTECH("SinovacBiotech"),
    BHARAT_BIOTECH("BharatBiotech"),
    SERUM_INSTITUTE_OF_INDIA_PRIVATE_LIMITED("SerumInstituteOfIndiaPrivateLimited"),
    UNDEFINED("UNDEFINED")
}

@Parcelize
data class RecoveryModel(
    override val disease: DiseaseType,
    val dateOfFirstPositiveTest: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateValidFrom: String,
    val certificateValidUntil: String,
    val certificateIdentifier: String
) : CertificateData, Parcelable

interface CertificateData {
    val disease: DiseaseType
}

fun CertificateModel.getCertificateListData(): List<CertificateData> {
    val list = mutableListOf<CertificateData>()
    list.addAll(vaccinations ?: emptyList())
    list.addAll(tests ?: emptyList())
    list.addAll(recoveryStatements ?: emptyList())

    return list
}