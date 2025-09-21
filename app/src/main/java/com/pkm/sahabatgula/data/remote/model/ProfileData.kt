package com.pkm.sahabatgula.data.remote.model

enum class Gender(val value: String) {
    MALE("Laki-laki"),
    FEMALE("Perempuan")
}

enum class DiabetesFamily(val value: String) {
    IMMEDIATE("Tingkat Satu"),
    EXTENDED("Tingkat Dua"),
    NONE("Tidak ada")
}

enum class ActivityLevel(val value: String) {
    INACTIVE("Tidak Aktif"),
    LIGHTLY_ACTIVE("Ringan"),
    MODERATELY_ACTIVE("Sedang"),
    VERY_ACTIVE("Berat"),
    EXTREMELY_ACTIVE("Sangat Berat")
}

data class ProfileData(
    val gender: String? = null,
    val age: Int? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val waistCircumference: Int? = null,
    val bloodPressure: Boolean? = null,
    val bloodSugar: Boolean? = null,
    val eatVegetables: Boolean? = null,
    val diabetesFamily: String? = null,
    val activityLevel: String? = null
)