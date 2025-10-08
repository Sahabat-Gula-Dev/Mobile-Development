package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class MyProfileResponse(

	@field:SerializedName("data")
	val data: MyProfileData,

	@field:SerializedName("status")
	val status: String
)

data class MyProfileData(

	@field:SerializedName("profile")
	val myProfile: MyProfile
)
data class MyProfile(

	@field:SerializedName("id")
	val userId: String,

	@field:SerializedName("username")
	val username: String,

	@field:SerializedName("email")
	val email: String,

	@field:SerializedName("bmi_score")
	val bmiScore: Double,

	@field:SerializedName("height")
	val height: Int?,

	@field:SerializedName("weight")
	val weight: Int?,

	@field:SerializedName("max_sugar")
	val maxSugar: Double,

	@field:SerializedName("max_carbs")
	val maxCarbs: Double,

	@field:SerializedName("max_protein")
	val maxProtein: Double,

	@field:SerializedName("max_natrium")
	val maxNatrium: Double,

	@field:SerializedName("max_calories")
	val maxCalories: Double,

	@field:SerializedName("max_fat")
	val maxFat: Double,

	@field:SerializedName("risk_index")
	val riskIndex: Int,

	@field:SerializedName("max_fiber")
	val maxFiber: Double,

	@field:SerializedName("max_potassium")
	val maxPotassium: Double
)

