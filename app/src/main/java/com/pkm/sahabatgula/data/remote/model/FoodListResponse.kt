package com.pkm.sahabatgula.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class FoodListResponse(

	@field:SerializedName("data")
	val data: List<FoodItem>,

	@field:SerializedName("meta")
	val meta: Meta,

	@field:SerializedName("status")
	val status: String
)

@Parcelize
data class FoodCategories(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int
): Parcelable


data class Meta(

	@field:SerializedName("total")
	val total: Int,

	@field:SerializedName("limit")
	val limit: Int,

	@field:SerializedName("page")
	val page: Int
)

@Parcelize
data class FoodItem(

	@field:SerializedName("serving_unit")
	val servingUnit: String,

	@field:SerializedName("category_id")
	val categoryId: Int,

	@field:SerializedName("weight_unit")
	val weightUnit: String,

	@field:SerializedName("weight_size")
	val weightSize: Int,

	@field:SerializedName("serving_size")
	val servingSize: Int,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("photo_url")
	val photoUrl: String,

	@field:SerializedName("calories")
	val calories: Double,

	@field:SerializedName("food_categories")
	val foodCategories: FoodCategories
): Parcelable


data class CategoryListResponse(
	val data: List<FoodCategories>
)
