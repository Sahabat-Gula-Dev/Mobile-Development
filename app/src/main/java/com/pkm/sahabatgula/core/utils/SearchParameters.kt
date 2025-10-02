package com.pkm.sahabatgula.core.utils

data class SearchParameters(
    val query: String?,
    val categoryId: Int?,
    val selectedIds: Set<String>,
    val expandedId: String?
)