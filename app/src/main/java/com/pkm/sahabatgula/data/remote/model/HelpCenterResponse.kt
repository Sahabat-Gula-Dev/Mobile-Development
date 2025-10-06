package com.pkm.sahabatgula.data.remote.model

import com.google.gson.annotations.SerializedName

data class HelpCenterResponse(

	@field:SerializedName("data")
	val data: List<FaqItem?>? = null,

	@field:SerializedName("meta")
	val meta: FaqMeta? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class FaqItem(

	@field:SerializedName("question")
	val question: String? = null,

	@field:SerializedName("answer")
	val answer: String? = null,

	@field:SerializedName("category_id")
	val categoryId: Int? = null,

	@field:SerializedName("faq_categories")
	val faqCategories: FaqCategories? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@Transient
	var isExpanded: Boolean = false
)

data class FaqMeta(

	@field:SerializedName("total")
	val total: Int? = null,

	@field:SerializedName("limit")
	val limit: Int? = null,

	@field:SerializedName("page")
	val page: Int? = null
)

data class FaqCategories(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class FaqCategoryListResponse(
	@SerializedName("status")
	val status: String,
	@SerializedName("data")
	val data: List<FaqCategories>
)
