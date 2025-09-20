package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class SetupProfileResponse(

	@field:SerializedName("data")
	val setupProfileData: SetupProfileData,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

data class SetupProfileData(

	@field:SerializedName("riskIndex")
	val riskIndex: Int,

	@field:SerializedName("carbs")
	val carbs: Double?,

	@field:SerializedName("protein")
	val protein: Double?,

	@field:SerializedName("fat")
	val fat: Double?,

	@field:SerializedName("maxCalories")
	val maxCalories: Int,

	@field:SerializedName("sugar")
	val sugar: Double?,

	@field:SerializedName("bmi")
	val bmi: Double?
)
