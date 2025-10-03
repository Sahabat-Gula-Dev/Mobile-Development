package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class CarouselResponse(
    @field:SerializedName("data")
    val data: List<CarouselItem>,

    @field:SerializedName("meta")
    val meta: CarouselMeta,

    @field:SerializedName("status")
    val status: String
)

data class CarouselItem(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("target_url")
    val targetUrl: String?,

    @field:SerializedName("created_at")
    val createdAt: String
)

data class CarouselMeta(
    @field:SerializedName("total")
    val total: Int,

    @field:SerializedName("limit")
    val limit: Int,

    @field:SerializedName("page")
    val page: Int
)