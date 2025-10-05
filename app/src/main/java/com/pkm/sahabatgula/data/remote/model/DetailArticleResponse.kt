package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class DetailArticleResponse(

	@field:SerializedName("data")
	val data: ArticleData,

	@field:SerializedName("status")
	val status: String
)

data class ArticleCategories(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int
)

data class ArticleItem(

	@field:SerializedName("cover_url")
	val coverUrl: String,

	@field:SerializedName("category_id")
	val categoryId: Int,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("author_id")
	val authorId: String,

	@field:SerializedName("title")
	val title: String,

	@field:SerializedName("content")
	val content: String,

	@field:SerializedName("article_categories")
	val articleCategories: ArticleCategories
)

data class ArticleData(

	@field:SerializedName("article")
	val article: ArticleItem
)
