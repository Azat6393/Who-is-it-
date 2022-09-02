package com.woynex.kimbu.feature_search.domain.model

data class Statistics(
    val searched_id_list: ArrayList<SearchedUser>? = null,
)

data class SearchedUser(
    val id: String? = null,
    val date: Long? = null
)
