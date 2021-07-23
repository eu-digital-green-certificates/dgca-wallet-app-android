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

package dgca.wallet.app.android.data.local

import dgca.verifier.app.decoder.model.*
import dgca.wallet.app.android.data.*

fun GreenCertificate.toCertificateModel(): CertificateModel {
    return CertificateModel(
        person.toPersonModel(),
        dateOfBirth,
        vaccinations?.map { it.toVaccinationModel() },
        tests?.map { it.toTestModel() },
        recoveryStatements?.map { it.toRecoveryModel() }
    )
}

fun RecoveryStatement.toRecoveryModel(): RecoveryModel {
    return RecoveryModel(
        disease.toDiseaseCode().toDiseaseType(),
        dateOfFirstPositiveTest,
        countryOfVaccination,
        certificateIssuer,
        certificateValidFrom,
        certificateValidUntil,
        certificateIdentifier
    )
}

fun Test.toTestModel(): TestModel {
    return TestModel(
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
}

fun Test.TestResult.toTestResult(): TestResult {
    return when (this) {
        Test.TestResult.DETECTED -> TestResult.DETECTED
        Test.TestResult.NOT_DETECTED -> TestResult.NOT_DETECTED
    }
}

fun DiseaseCode.toDiseaseType(): DiseaseType = when (this) {
    DiseaseCode.COVID_19 -> DiseaseType.COVID_19
    else -> DiseaseType.UNDEFINED
}

fun TypeOfTestCode.toTypeOfTest(): TypeOfTest = when (this) {
    TypeOfTestCode.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION -> TypeOfTest.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION
    TypeOfTestCode.RAPID_IMMUNOASSAY -> TypeOfTest.RAPID_IMMUNOASSAY
    else -> TypeOfTest.UNDEFINED
}

fun VaccinePropylaxisCode.toVaccineProphylaxisType(): VaccinePropylaxisType = when (this) {
    VaccinePropylaxisCode.SARS_CoV_2_antigen_vaccine -> VaccinePropylaxisType.SARS_CoV_2_antigen_vaccine
    VaccinePropylaxisCode.SARS_CoV_2_mRNA_vaccine -> VaccinePropylaxisType.SARS_CoV_2_mRNA_vaccine
    VaccinePropylaxisCode.covid_19_vaccines -> VaccinePropylaxisType.covid_19_vaccines
    else -> VaccinePropylaxisType.UNDEFINED
}

fun ManufacturerCode.toManufacturerType(): ManufacturerType = when (this) {
    ManufacturerCode.AstraZenecaAB -> ManufacturerType.AstraZenecaAB
    ManufacturerCode.BiontechManufacturingGmbH -> ManufacturerType.BiontechManufacturingGmbH
    ManufacturerCode.Janssen_CilagInternational -> ManufacturerType.Janssen_CilagInternational
    ManufacturerCode.ModernaBiotechSpainS_L -> ManufacturerType.ModernaBiotechSpainS_L
    ManufacturerCode.CurevacAG -> ManufacturerType.CurevacAG
    ManufacturerCode.CanSinoBiologics -> ManufacturerType.CanSinoBiologics
    ManufacturerCode.ChinaSinopharmInternationalCorp_Beijinglocation -> ManufacturerType.ChinaSinopharmInternationalCorp_Beijinglocation
    ManufacturerCode.SinopharmWeiqidaEuropePharmaceuticals_r_o_Praguelocation -> ManufacturerType.SinopharmWeiqidaEuropePharmaceuticals_r_o_Praguelocation
    ManufacturerCode.SinopharmZhijun_Shenzhen_PharmaceuticalCo_Ltd_Shenzhenlocation -> ManufacturerType.SinopharmZhijun_Shenzhen_PharmaceuticalCo_Ltd_Shenzhenlocation
    ManufacturerCode.NovavaxCZAS -> ManufacturerType.NovavaxCZAS
    ManufacturerCode.GamaleyaResearchInstitute -> ManufacturerType.GamaleyaResearchInstitute
    ManufacturerCode.VectorInstitute -> ManufacturerType.VectorInstitute
    ManufacturerCode.SinovacBiotech -> ManufacturerType.SinovacBiotech
    ManufacturerCode.BharatBiotech -> ManufacturerType.BharatBiotech
    ManufacturerCode.SerumInstituteOfIndiaPrivateLimited -> ManufacturerType.SerumInstituteOfIndiaPrivateLimited
    else -> ManufacturerType.UNDEFINED
}

fun Vaccination.toVaccinationModel(): VaccinationModel {
    return VaccinationModel(
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
}

fun Person.toPersonModel(): PersonModel {
    return PersonModel(
        standardisedFamilyName,
        familyName,
        standardisedGivenName,
        givenName
    )
}

fun String.toDiseaseCode(): DiseaseCode = when (this) {
    DiseaseCode.COVID_19.value -> DiseaseCode.COVID_19
    else -> DiseaseCode.UNDEFINED
}

fun String.toTypeOfTestCode(): TypeOfTestCode = when (this) {
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
    SARS_CoV_2_antigen_vaccine("1119305005"),
    SARS_CoV_2_mRNA_vaccine("1119349007"),
    covid_19_vaccines("J07BX03"),
    UNDEFINED("")
}

enum class ManufacturerCode(val value: String) {
    AstraZenecaAB("ORG-100001699"),
    BiontechManufacturingGmbH("ORG-100030215"),
    Janssen_CilagInternational("ORG-100001417"),
    ModernaBiotechSpainS_L("ORG-100031184"),
    CurevacAG("ORG-100006270"),
    CanSinoBiologics("ORG-100013793"),
    ChinaSinopharmInternationalCorp_Beijinglocation("ORG-100020693"),
    SinopharmWeiqidaEuropePharmaceuticals_r_o_Praguelocation("ORG-100010771"),
    SinopharmZhijun_Shenzhen_PharmaceuticalCo_Ltd_Shenzhenlocation("ORG-100024420"),
    NovavaxCZAS("ORG-100032020"),
    GamaleyaResearchInstitute("Gamaleya-Research-Institute"),
    VectorInstitute("Vector-Institute"),
    SinovacBiotech("Sinovac-Biotech"),
    BharatBiotech("Bharat-Biotech"),
    SerumInstituteOfIndiaPrivateLimited("ORG-100001981"),
    UNDEFINED("")
}

enum class TypeOfTestCode(val value: String) {
    NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION("LP6464-4"),
    RAPID_IMMUNOASSAY("LP217198-3"),
    UNDEFINED("")
}