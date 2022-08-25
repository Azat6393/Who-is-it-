package com.woynex.kimbu.feature_search.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CountryInfo(
    val flag: String,
    val name: String,
    val number: String
)