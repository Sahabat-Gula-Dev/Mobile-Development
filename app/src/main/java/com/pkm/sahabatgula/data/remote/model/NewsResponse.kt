package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("feed")
    val feed: Feed,
    @SerializedName("items")
    val items: List<NewsItem>
)
data class Feed(
    @SerializedName("url")
    val url: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("link")
    val link: String
)

data class Enclosure(
    @SerializedName("link")
    val link: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("length")
    val length: Int?
)

data class NewsItem(
    @SerializedName("title")
    val title: String,
    @SerializedName("pubDate")
    val pubDate: String,
    @SerializedName("link")
    val link: String,
    @SerializedName("guid")
    val guid: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("content")
    val content: String?,
    @SerializedName("enclosure")
    val enclosure: Enclosure?
)