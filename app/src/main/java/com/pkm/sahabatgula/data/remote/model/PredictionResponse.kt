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
	val id: String,

	@field:SerializedName("photo_url")
	val photoUrl: String? = null,

	@field:SerializedName("calories")
	val calories: Double? = null,

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