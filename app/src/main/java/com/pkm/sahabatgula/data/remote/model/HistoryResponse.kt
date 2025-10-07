package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    @field:SerializedName("status")
    val status: String,
    @field:SerializedName("data")
    val data: List<HistoryItem>
)

data class HistoryItem(
    @field:SerializedName("date")
    val date: String,
    @field:SerializedName("foods")
    val foods: List<FoodLog>,
    @field:SerializedName("activities")
    val activities: List<ActivityLog>
)

data class FoodLog(

    @field:SerializedName("id")
    val id: String,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("photo_url")
    val photoUrl: String?,
    @field:SerializedName("description")
    val description: String?,
    @field:SerializedName("calories")
    val calories: Double,
    @field:SerializedName("portion")
    val portion: Int,
    @field:SerializedName("time")
    val time: String,
    @Transient
    var isExpanded: Boolean = false
)

data class ActivityLog(
    @field:SerializedName("id")
    val id: String,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("photo_url")
    val photoUrl: String?,
    @field:SerializedName("description")
    val description: String?,
    @field:SerializedName("calories_burned")
    val caloriesBurned: Double,
    @field:SerializedName("duration")
    val duration: Int,
    @field:SerializedName("time")
    val time: String,
    @Transient
    var isExpanded: Boolean = false
)
