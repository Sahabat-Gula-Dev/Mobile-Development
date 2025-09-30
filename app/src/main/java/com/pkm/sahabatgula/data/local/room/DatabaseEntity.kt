package com.pkm.sahabatgula.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val username: String?,
    val email: String,
    val gender: String?,
    val age: Int?,
    val height: Int?,
    val weight: Int?,
    val waist_circumference: Int?,
    val blood_pressure: Boolean?,
    val blood_sugar: Boolean?,
    val eat_vegetables: Boolean?,
    val diabetes_family: String?,
    val activity_level: String?,
    val risk_index: Int?,
    val bmi_score: Double?,
    val max_calories: Int?,
    val max_carbs: Double?,
    val max_protein: Double?,
    val max_fat: Double?,
    val max_sugar: Double?,
    val max_natrium: Double?,
    val max_fiber: Double?,
    val max_potassium: Double?
)

@Entity(tableName = "summary", primaryKeys = ["date", "type"])
data class SummaryEntity(
    val date: String,
    val type: String, // "DAILY", "WEEKLY", "MONTHLY"
    val calories: Double?,
    val carbs: Double?,
    val protein: Double?,
    val fat: Double?,
    val sugar: Double?,
    val sodium: Double?,
    val fiber: Double?,
    val potassium: Double?,
    val burned: Int?,
    val steps: Int?,
    val water: Int?
)