package com.woynex.kimbu.feature_search.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity(tableName = "number_info")
@Serializable
data class NumberInfo(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String?,
    val number: String,
    val type: String,
    val countryCode: String,
    val profilePhoto: String,
    val date: Long
): java.io.Serializable