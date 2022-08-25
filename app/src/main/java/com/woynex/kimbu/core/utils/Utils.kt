package com.woynex.kimbu.core.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.woynex.kimbu.feature_search.domain.model.CountryInfo
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
fun Long.millisToDate(format: String): String {
    val simpleDateFormat = SimpleDateFormat(format)
    return simpleDateFormat.format(this)
}


fun <T> convertToSet(list: List<T>): Set<T> {
    return HashSet(list)
}

fun getJsonFromAssets(context: Context, fileName: String): String? {
    var jsonString = ""
    try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        jsonString = String(buffer)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return jsonString
}

fun String.fromJsonToCountyList(): List<CountryInfo> {
    val gson = Gson()
    return gson.fromJson(this, Array<CountryInfo>::class.java).asList()
}