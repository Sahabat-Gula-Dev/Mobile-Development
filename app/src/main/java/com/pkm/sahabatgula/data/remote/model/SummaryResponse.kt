package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class SummaryResponse(

	@field:SerializedName("data")
	val data: SummaryData,

	@field:SerializedName("status")
	val status: String
)

data class SummaryNutrients(

	@field:SerializedName("sodium")
	val sodium: Double? = null,

	@field:SerializedName("fiber")
	val fiber: Double? = null,

	@field:SerializedName("potassium")
	val potassium: Double? = null,

	@field:SerializedName("carbs")
	val carbs: Double? = null,

	@field:SerializedName("protein")
	val protein: Double? = null,

	@field:SerializedName("fat")
	val fat: Double? = null,

	@field:SerializedName("calories")
	val calories: Int,

	@field:SerializedName("sugar")
	val sugar: Double? = null
)

data class SummaryDaily(

	@field:SerializedName("date")
	val date: String,

	@field:SerializedName("activities")
	val activities: Activities,

	@field:SerializedName("steps")
	val steps: Int,

	@field:SerializedName("water")
	val water: Int,

	@field:SerializedName("nutrients")
	val nutrients: Nutrients
)

data class SummaryMonthlyItem(

	@field:SerializedName("date")
	val date: String,

	@field:SerializedName("activities")
	val activities: Activities,

	@field:SerializedName("steps")
	val steps: Int,

	@field:SerializedName("water")
	val water: Int,

	@field:SerializedName("nutrients")
	val nutrients: Nutrients
)

data class SummaryActivities(

	@field:SerializedName("burned")
	val burned: Int
)

data class SummaryData(

	@field:SerializedName("daily")
	val daily: SummaryDaily,

	@field:SerializedName("monthly")
	val monthly: List<SummaryMonthlyItem>,

	@field:SerializedName("weekly")
	val weekly: List<SummaryWeeklyItem>
)

data class SummaryWeeklyItem(

	@field:SerializedName("date")
	val date: String,

	@field:SerializedName("activities")
	val activities: Activities,

	@field:SerializedName("steps")
	val steps: Int,

	@field:SerializedName("water")
	val water: Int,

	@field:SerializedName("nutrients")
	val nutrients: Nutrients
)
