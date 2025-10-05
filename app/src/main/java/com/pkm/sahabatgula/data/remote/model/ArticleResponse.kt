package com.pkm.sahabatgula.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArticleResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("data")
    val data: List<Article>?,
    @SerializedName("meta")
    val meta: ExploreMeta?
): Parcelable


@Parcelize
data class Article(
    @SerializedName("id")
    val id: String,
    @SerializedName("author_id")
    val authorId: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("cover_url")
    val coverUrl: String?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("article_categories")
    val articleCategories: ArticleCategory?,
    @field:SerializedName("category_id")
    val categoryId: Int,
): Parcelable

@Parcelize
data class ArticleCategory(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?
): Parcelable


data class ArticleCategoryListResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<ArticleCategory>
)