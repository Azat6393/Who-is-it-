package com.woynex.kimbu.feature_search.domain.model

data class Contact(
    val id: String,
    val number: String,
    val name: String
)

fun Contact.toTag(uuid: String): Tag {
    return Tag(
        name = name,
        uuid = uuid
    )
}

fun List<Contact>.toTagMap(uuid: String): Map<String, Tag> {
    val list = mutableMapOf<String, Tag>()
    this.forEach { contact ->
        list[contact.number] = contact.toTag(uuid)
    }
    return list
}
