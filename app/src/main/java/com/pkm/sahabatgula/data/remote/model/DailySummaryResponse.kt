package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class DailySummaryResponse(

	@field:SerializedName("data")
	val data: SummaryData? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class SummaryData_(
	// Pindahkan properti dari class 'Summary' yang lama ke sini
	@SerializedName("date")
	val date: String,
	@SerializedName("nutrients")
	val nutrients: Nutrients,

	// Properti ini sudah benar
	@SerializedName("activities")
	val activities: Activities,
	@SerializedName("steps")
	val steps: Int,
	@SerializedName("water")
	val water: Int
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