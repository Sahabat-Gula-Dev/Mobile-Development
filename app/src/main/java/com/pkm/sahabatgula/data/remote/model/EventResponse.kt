package com.pkm.sahabatgula.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class EventResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("data")
    val data: List<Event>?,
    @SerializedName("meta")
    val meta: ExploreMeta?
)

@Parcelize
data class Event(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("event_date")
    val eventDate: String?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("cover_url")
    val coverUrl: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("location_detail")
    val locationDetail: String?,
    @SerializedName("event_start")
    val eventStart: String?,
    @SerializedName("event_end")
    val eventEnd: String?,
    @SerializedName("event_categories")
    val eventCategories: EventCategory?
): Parcelable

@Parcelize

data class EventCategory(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?
): Parcelable


@Parcelize
data class ExploreMeta(
    @SerializedName("page")
    val page: Int?,
    @SerializedName("limit")
    val limit: Int?,
    @SerializedName("total")
    val total: Int?
): Parcelable

data class EventCategoryListResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<EventCategory>
)