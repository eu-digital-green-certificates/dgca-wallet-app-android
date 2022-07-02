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

package dgca.wallet.app.android.dcc.model

import dgca.verifier.app.decoder.model.*

fun GreenCertificate.toCertificateModel(): CertificateModel =
    CertificateModel(
        person.toPersonModel(),
        dateOfBirth,
        vaccinations?.map { it.toVaccinationModel() },
        tests?.map { it.toTestModel() },
        recoveryStatements?.map { it.toRecoveryModel() }
    )

fun RecoveryStatement.toRecoveryModel(): RecoveryModel =
    RecoveryModel(
        disease.toDiseaseCode().toDiseaseType(),
        dateOfFirstPositiveTest,
        countryOfVaccination,
        certificateIssuer,
        certificateValidFrom,
        certificateValidUntil,
        certificateIdentifier
    )

fun Test.toTestModel(): TestModel =
    TestModel(
        disease.toDiseaseCode().toDiseaseType(),
        typeOfTest.toTypeOfTestCode().toTypeOfTest(),
        testName,
        testNameAndManufacturer,
        dateTimeOfCollection,
        dateTimeOfTestResult,
        testResult,
        testingCentre,
        countryOfVaccination,
        certificateIssuer,
        certificateIdentifier,
        getTestResultType().toTestResult()
    )

fun Test.TestResult.toTestResult(): TestResult =
    when (this) {
        Test.TestResult.DETECTED -> TestResult.DETECTED
        Test.TestResult.NOT_DETECTED -> TestResult.NOT_DETECTED
    }

fun DiseaseCode.toDiseaseType(): DiseaseType =
    when (this) {
        DiseaseCode.COVID_19 -> DiseaseType.COVID_19
        else -> DiseaseType.UNDEFINED
    }

fun TypeOfTestCode.toTypeOfTest(): TypeOfTest =
    when (this) {
        TypeOfTestCode.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION -> TypeOfTest.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION
        TypeOfTestCode.RAPID_IMMUNOASSAY -> TypeOfTest.RAPID_IMMUNOASSAY
        else -> TypeOfTest.UNDEFINED
    }

fun VaccinePropylaxisCode.toVaccineProphylaxisType(): VaccinePropylaxisType =
    when (this) {
        VaccinePropylaxisCode.SARS_COV_2_ANTIGEN_VACCINE -> VaccinePropylaxisType.SARS_COV_2_ANTIGEN_VACCINE
        VaccinePropylaxisCode.SARS_COV_2_MRNA_VACCINE -> VaccinePropylaxisType.SARS_COV_2_MRNA_VACCINE
        VaccinePropylaxisCode.COVID_19_VACCINES -> VaccinePropylaxisType.COVID_19_VACCINES
        else -> VaccinePropylaxisType.UNDEFINED
    }

fun ManufacturerCode.toManufacturerType(): ManufacturerType =
    when (this) {
        ManufacturerCode.ASTRA_ZENECA_AB -> ManufacturerType.ASTRA_ZENECA_AB
        ManufacturerCode.BIONTECH_MANUFACTURING_GMBH -> ManufacturerType.BIONTECH_MANUFACTURING_GMBH
        ManufacturerCode.JANSSEN_CILAG_INTERNATIONAL -> ManufacturerType.JANSSEN_CILAG_INTERNATIONAL
        ManufacturerCode.MODERNA_BIOTECH_SPAINS_L -> ManufacturerType.MODERNA_BIOTECH_SPAINS_L
        ManufacturerCode.CUREVAC_AG -> ManufacturerType.CUREVAC_AG
        ManufacturerCode.CAN_SIGNO_BIOLOGICS -> ManufacturerType.CAN_SIGNO_BIOLOGICS
        ManufacturerCode.CHINA_SINOPHARM_INTERNATIONAL_CORP_BEIJING_LOCATION -> ManufacturerType.CHINA_SINOPHARM_INTERNATIONAL_CORP_BEIJING_LOCATION
        ManufacturerCode.SINOPHARM_WEIGIDA_EUROPE_PHARMACEUTICALS_R_O_PRAGUE_LOCATION -> ManufacturerType.SINOPHARM_WEIGIDA_EUROPE_PHARMACEUTICALS_R_O_PRAGUE_LOCATION
        ManufacturerCode.SINOPHARM_ZHIJUN_SHENZHEN_PHARMACEUTICAL_CO_LTD_SHENZHEN_LOCATION -> ManufacturerType.SINOPHARM_ZHIJUN_SHENZHEN_PHARMACEUTICAL_CO_LTD_SHENZHEN_LOCATION
        ManufacturerCode.NOVAVAX_CZAS -> ManufacturerType.NOVAVAX_CZAS
        ManufacturerCode.GAMALEYA_RESEARCH_INSTITUTE -> ManufacturerType.GAMALEYA_RESEARCH_INSTITUTE
        ManufacturerCode.VECTOR_INSTITUTE -> ManufacturerType.VECTOR_INSTITUTE
        ManufacturerCode.SINOVAC_BIOTECH -> ManufacturerType.SINOVAC_BIOTECH
        ManufacturerCode.BHARAT_BIOTECH -> ManufacturerType.BHARAT_BIOTECH
        ManufacturerCode.SERUM_INSTITUTE_OF_INDIA_PRIVATE_LIMITED -> ManufacturerType.SERUM_INSTITUTE_OF_INDIA_PRIVATE_LIMITED
        else -> ManufacturerType.UNDEFINED
    }

fun Vaccination.toVaccinationModel(): VaccinationModel =
    VaccinationModel(
        disease.toDiseaseCode().toDiseaseType(),
        vaccine.toVaccineProphylaxisCode().toVaccineProphylaxisType(),
        medicinalProduct,
        manufacturer.toManufacturerCode().toManufacturerType(),
        doseNumber,
        totalSeriesOfDoses,
        dateOfVaccination,
        countryOfVaccination,
        certificateIssuer,
        certificateIdentifier
    )

fun Person.toPersonModel(): PersonModel =
    PersonModel(
        standardisedFamilyName,
        familyName,
        standardisedGivenName,
        givenName
    )

fun String.toDiseaseCode(): DiseaseCode =
    when (this) {
        DiseaseCode.COVID_19.value -> DiseaseCode.COVID_19
        else -> DiseaseCode.UNDEFINED
    }

fun String.toTypeOfTestCode(): TypeOfTestCode =
    when (this) {
        TypeOfTestCode.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION.value -> TypeOfTestCode.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION
        TypeOfTestCode.RAPID_IMMUNOASSAY.value -> TypeOfTestCode.RAPID_IMMUNOASSAY
        else -> TypeOfTestCode.UNDEFINED
    }

fun String.toVaccineProphylaxisCode(): VaccinePropylaxisCode {
    VaccinePropylaxisCode.values().forEach {
        if (it.value == this) {
            return it
        }
    }
    return VaccinePropylaxisCode.UNDEFINED
}

fun String.toManufacturerCode(): ManufacturerCode {
    ManufacturerCode.values().forEach {
        if (it.value == this) {
            return it
        }
    }
    return ManufacturerCode.UNDEFINED
}

enum class DiseaseCode(val value: String) {
    COVID_19("840539006"),
    UNDEFINED("")
}

enum class VaccinePropylaxisCode(val value: String) {
    SARS_COV_2_ANTIGEN_VACCINE("1119305005"),
    SARS_COV_2_MRNA_VACCINE("1119349007"),
    COVID_19_VACCINES("J07BX03"),
    UNDEFINED("")
}

enum class ManufacturerCode(val value: String) {
    ASTRA_ZENECA_AB("ORG-100001699"),
    BIONTECH_MANUFACTURING_GMBH("ORG-100030215"),
    JANSSEN_CILAG_INTERNATIONAL("ORG-100001417"),
    MODERNA_BIOTECH_SPAINS_L("ORG-100031184"),
    CUREVAC_AG("ORG-100006270"),
    CAN_SIGNO_BIOLOGICS("ORG-100013793"),
    CHINA_SINOPHARM_INTERNATIONAL_CORP_BEIJING_LOCATION("ORG-100020693"),
    SINOPHARM_WEIGIDA_EUROPE_PHARMACEUTICALS_R_O_PRAGUE_LOCATION("ORG-100010771"),
    SINOPHARM_ZHIJUN_SHENZHEN_PHARMACEUTICAL_CO_LTD_SHENZHEN_LOCATION("ORG-100024420"),
    NOVAVAX_CZAS("ORG-100032020"),
    GAMALEYA_RESEARCH_INSTITUTE("Gamaleya-Research-Institute"),
    VECTOR_INSTITUTE("Vector-Institute"),
    SINOVAC_BIOTECH("Sinovac-Biotech"),
    BHARAT_BIOTECH("Bharat-Biotech"),
    SERUM_INSTITUTE_OF_INDIA_PRIVATE_LIMITED("ORG-100001981"),
    UNDEFINED("")
}

enum class TypeOfTestCode(val value: String) {
    NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION("LP6464-4"),
    RAPID_IMMUNOASSAY("LP217198-3"),
    UNDEFINED("")
}
