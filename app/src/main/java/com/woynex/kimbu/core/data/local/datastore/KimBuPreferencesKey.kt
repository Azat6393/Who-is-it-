package com.woynex.kimbu.core.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object KimBuPreferencesKey {
    val USER_ID_KEY = stringPreferencesKey("is")
    val USER_FIRST_NAME_KEY = stringPreferencesKey("first_name")
    val USER_LAST_NAME_KEY = stringPreferencesKey("last_name")
    val USER_PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")
    val USER_PROFILE_PHOTO_KEY = stringPreferencesKey("profile_photo")
    val USER_EMAIL_KEY = stringPreferencesKey("email")
    val USER_CREATED_DATE_KEY = longPreferencesKey("created_date")
    val CONTACTS_UPLOADED_KEY = booleanPreferencesKey("contacts_uploaded")
    val HAS_PERMISSION_KEY = booleanPreferencesKey("has_permission")
}