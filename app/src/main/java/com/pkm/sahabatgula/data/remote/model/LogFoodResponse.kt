package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class FoodItemRequest(
    @SerializedName("food_id")
    val foodId: String,

    @SerializedName("portion")
    val portion: Int?
)

data class LogFoodRequest(
    @SerializedName("foods")
    val foods: List<FoodItemRequest>
)

data class LogFoodResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: LogFoodData
)

data class LogFoodData(
    @SerializedName("logs")
    val logs: List<LogEntry>,

    @SerializedName("totals")
    val totals: Totals,

    @SerializedName("ratios")
    val ratios: Ratios
)

data class LogEntry(
    @SerializedName("id")
    val id: Int,

    @SerializedName("food_id")
    val foodId: String,

    @SerializedName("portion")
    val portion: Int,

    @SerializedName("logged_at")
    val loggedAt: String
)


data class Totals(
    @SerializedName("calories")
    val calories: Double,

    @SerializedName("carbs")
    val carbs: Double,

    @SerializedName("protein")
    val protein: Double,

    @SerializedName("fat")
    val fat: Double,

    @SerializedName("sugar")
    val sugar: Double,

    @SerializedName("sodium")
    val sodium: Double,

    @SerializedName("fiber")
    val fiber: Double,

    @SerializedName("potassium")
    val potassium: Double
)

data class Ratios(
    @SerializedName("calories_ratio")
    val caloriesRatio: Double,

    @SerializedName("sugar_ratio")
    val sugarRatio: Double
)