package com.woynex.kimbu.feature_search.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "number_info")
@Parcelize
data class NumberInfo(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String?,
    val number: String,
    val type: String,
    val countryCode: String,
    val date: Long
) : Parcelable