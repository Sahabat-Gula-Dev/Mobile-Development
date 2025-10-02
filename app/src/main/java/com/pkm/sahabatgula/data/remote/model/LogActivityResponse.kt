package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class LogActivityRequest(
	@field:SerializedName("activities")
	val activities: List<LogActivityItemRequest>
)

data class LogActivityItemRequest(
	@field:SerializedName("activity_id")
	val activityId: String,
)


data class LogActivityResponse(

	@field:SerializedName("data")
	val data: LogActivityData,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

data class LogActivityData(

	@field:SerializedName("total_burned")
	val totalBurned: Int,

	@field:SerializedName("logs")
	val logs: List<LogActivityItem>
)

data class LogActivityItem(

	@field:SerializedName("logged_at")
	val loggedAt: String,

	@field:SerializedName("activity_id")
	val activityId: String,

	@field:SerializedName("id")
	val id: Int
)