package com.woynex.kimbu.feature_auth.domain.model

data class User(
    val id: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val phone_number: String? = null,
    val profile_photo: String? = null,
    val email: String? = null,
    val created_date: Long? = null,
)
