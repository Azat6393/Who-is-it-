package com.woynex.kimbu.feature_auth.domain.model

import com.woynex.kimbu.feature_search.domain.model.NumberInfo

data class User(
    val id: String? = null,
    var first_name: String? = null,
    var last_name: String? = null,
    val phone_number: String? = null,
    var profile_photo: String? = null,
    val email: String? = null,
    val created_date: Long? = null,
    val contacts_uploaded: Boolean = false,
    val has_permission: Boolean = false
)

fun User.toNumberInfo(): NumberInfo {
    return NumberInfo(
        id = 0,
        name = "${this.first_name} ${this.last_name}",
        number = this.phone_number ?: "",
        type = "",
        countryCode = "",
        profilePhoto = this.profile_photo ?: "",
        date = this.created_date ?: 0,
        has_permission = this.has_permission
    )
}
