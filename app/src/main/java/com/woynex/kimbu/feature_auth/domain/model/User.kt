package com.woynex.kimbu.feature_auth.domain.model

import com.woynex.kimbu.feature_search.domain.model.NumberInfo

data class User(
    val id: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val phone_number: String? = null,
    val profile_photo: String? = null,
    val email: String? = null,
    val created_date: Long? = null,
    val contacts_uploaded: Boolean = false
)

fun User.toNumberInfo(): NumberInfo {
    return NumberInfo(
        id = 0,
        name = "${this.first_name} ${this.last_name}",
        number = this.phone_number ?: "",
        type = "",
        countryCode = "",
        date = this.created_date ?: 0
    )
}
