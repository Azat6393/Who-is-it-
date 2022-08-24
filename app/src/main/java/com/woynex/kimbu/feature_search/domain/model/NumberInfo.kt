package com.woynex.kimbu.feature_search.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class NumberInfo(
    val id: Int,
    val name: String?,
    val number: String,
    val type: String,
    val countryCode: String,
    val date: Long
) : Parcelable