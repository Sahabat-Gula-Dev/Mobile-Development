package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class ActivityResponse(

	@field:SerializedName("data")
	val data: List<ActivitiesDataItem>,

	@field:SerializedName("meta")
	val meta: ActivityMeta,

	@field:SerializedName("status")
	val status: String
)

data class ActivityCategories(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int
)

data class ActivitiesDataItem(

	@field:SerializedName("duration")
	val duration: Int,

	@field:SerializedName("activity_categories")
	val activityCategories: ActivityCategories,

	@field:SerializedName("category_id")
	val categoryId: Int,

	@field:SerializedName("calories_burned")
	val caloriesBurned: Int,

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

	@field:SerializedName("duration_unit")
	val durationUnit: String,

	@Transient
	var isExpanded: Boolean = false,

	@Transient
	var isSelected: Boolean = false
)

data class ActivityMeta(

	@field:SerializedName("total")
	val total: Int,

	@field:SerializedName("limit")
	val limit: Int,

	@field:SerializedName("page")
	val page: Int
)

data class ActivityCategoryListResponse(
	val data: List<ActivityCategories>
)




