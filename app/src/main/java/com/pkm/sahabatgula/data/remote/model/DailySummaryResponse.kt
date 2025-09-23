package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class DailySummaryResponse(

	@field:SerializedName("data")
	val data: DailySummaryData? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Summary(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("activities")
	val activities: Activities? = null,

	@field:SerializedName("steps")
	val steps: Int? = null,

	@field:SerializedName("water")
	val water: Int? = null,

	@field:SerializedName("nutrients")
	val nutrients: Nutrients? = null
)

data class Nutrients(

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
	val calories: Double? = null,

	@field:SerializedName("sugar")
	val sugar: Double? = null
)

data class Activities(

	@field:SerializedName("burned")
	val burned: Int? = null
)

data class DailySummaryData(

	@field:SerializedName("summary")
	val summary: Summary? = null
)
