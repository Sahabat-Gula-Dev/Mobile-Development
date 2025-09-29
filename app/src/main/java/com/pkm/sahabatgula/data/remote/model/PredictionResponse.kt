package com.pkm.sahabatgula.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class PredictionResponse(

	@field:SerializedName("data")
	val data: PredictionData? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("message")
	val message: String? = null
)

@Parcelize
data class FoodCategories(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
): Parcelable

@Parcelize
data class FoodsItem(

	@field:SerializedName("serving_unit")
	val servingUnit: String? = null,

	@field:SerializedName("category_id")
	val categoryId: Int? = null,

	@field:SerializedName("weight_unit")
	val weightUnit: String? = null,

	@field:SerializedName("weight_size")
	val weightSize: Int? = null,

	@field:SerializedName("serving_size")
	val servingSize: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("photo_url")
	val photoUrl: String? = null,

	@field:SerializedName("calories")
	val calories: Int? = null,

	@field:SerializedName("food_categories")
	val foodCategories: FoodCategories? = null
): Parcelable

data class PredictionData(

	@field:SerializedName("foods")
	val foods: List<FoodsItem?>? = null,

	@field:SerializedName("predicted_name")
	val predictedName: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("meta")
	val meta: Meta? = null
)

data class Meta(

	@field:SerializedName("total")
	val total: Int? = null,

	@field:SerializedName("limit")
	val limit: Int? = null,

	@field:SerializedName("page")
	val page: Int? = null
)
