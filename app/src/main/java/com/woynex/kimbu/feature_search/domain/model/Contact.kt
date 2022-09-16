package com.woynex.kimbu.feature_search.domain.model

import com.woynex.kimbu.core.utils.deleteCountryCode

data class Contact(
    val id: String,
    val number: String,
    val name: String
)

fun Contact.toTag(uuid: String): Tag {
    return Tag(
        name = name,
        uuid = uuid,
        number = number.deleteCountryCode()
    )
}
