package com.woynex.kimbu.feature_settings.presentation.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.woynex.kimbu.core.data.local.datastore.KimBuPreferencesKey
import com.woynex.kimbu.feature_auth.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val currentUser = dataStore.data.map { preferences ->
        User(
            id = preferences[KimBuPreferencesKey.USER_ID_KEY],
            first_name = preferences[KimBuPreferencesKey.USER_FIRST_NAME_KEY],
            last_name = preferences[KimBuPreferencesKey.USER_LAST_NAME_KEY],
            phone_number = preferences[KimBuPreferencesKey.USER_PHONE_NUMBER_KEY],
            profile_photo = preferences[KimBuPreferencesKey.USER_PROFILE_PHOTO_KEY],
            email = preferences[KimBuPreferencesKey.USER_EMAIL_KEY],
            created_date = preferences[KimBuPreferencesKey.USER_CREATED_DATE]
        )
    }

    fun signOut() = viewModelScope.launch {
        Firebase.auth.signOut()
        dataStore.edit { preferences ->
            preferences[KimBuPreferencesKey.USER_ID_KEY] = ""
            preferences[KimBuPreferencesKey.USER_FIRST_NAME_KEY] = ""
            preferences[KimBuPreferencesKey.USER_LAST_NAME_KEY] = ""
            preferences[KimBuPreferencesKey.USER_PHONE_NUMBER_KEY] = ""
            preferences[KimBuPreferencesKey.USER_PROFILE_PHOTO_KEY] = ""
            preferences[KimBuPreferencesKey.USER_EMAIL_KEY] = ""
            preferences[KimBuPreferencesKey.USER_CREATED_DATE] = 0
        }
    }
}