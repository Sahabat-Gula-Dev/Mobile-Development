package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class DetailFoodResponse(

	@field:SerializedName("data")
	val data: Data,

	@field:SerializedName("status")
	val status: String
)

data class Food(

	@field:SerializedName("fiber")
	val fiber: Double,

	@field:SerializedName("potassium")
	val potassium: Double,

	@field:SerializedName("carbs")
	val carbs: Double,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("calories")
	val calories: Double,

	@field:SerializedName("food_categories")
	val foodCategories: DetailFoodCategories,

	@field:SerializedName("sodium")
	val sodium: Double,

	@field:SerializedName("serving_unit")
	val servingUnit: String,

	@field:SerializedName("category_id")
	val categoryId: Int,

	@field:SerializedName("weight_unit")
	val weightUnit: String? = null,

	@field:SerializedName("weight_size")
	val weightSize: Int,

	@field:SerializedName("serving_size")
	val servingSize: Int,

	@field:SerializedName("protein")
	val protein: Double,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("fat")
	val fat: Double,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("photo_url")
	val photoUrl: String,

	@field:SerializedName("sugar")
	val sugar: Double
)

data class DetailFoodCategories(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int
)

data class Data(

	@field:SerializedName("food")
	val food: Food
)
