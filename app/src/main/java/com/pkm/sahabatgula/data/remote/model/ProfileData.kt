package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

enum class Gender(val value: String) {
    MALE("Laki-laki"),
    FEMALE("Perempuan")
}

enum class DiabetesFamily(val value: String) {
    FIRSTFAM("Tingkat Satu"),
    SECONDFAM("Tingkat Dua"),
    NONE("Tidak Ada")
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
    @SerializedName("waist_circumference")
    val waistCircumference: Int? = null,
    @SerializedName("blood_pressure")
    val bloodPressure: Boolean? = null,
    @SerializedName("blood_sugar")
    val bloodSugar: Boolean? = null,
    @SerializedName("eat_vegetables")
    val eatVegetables: Boolean? = null,
    @SerializedName("diabetes_family")
    val diabetesFamily: String? = null,
    @SerializedName("activity_level")
    val activityLevel: String? = null
)