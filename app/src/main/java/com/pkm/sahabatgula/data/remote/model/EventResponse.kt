package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("data")
    val data: List<Event>?,
    @SerializedName("meta")
    val meta: ExploreMeta?
)

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
    @SerializedName("event_categories")
    val eventCategories: EventCategory?
)

data class EventCategory(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?
)

data class ExploreMeta(
    @SerializedName("page")
    val page: Int?,
    @SerializedName("limit")
    val limit: Int?,
    @SerializedName("total")
    val total: Int?
)