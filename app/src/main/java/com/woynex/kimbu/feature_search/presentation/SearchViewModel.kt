package com.woynex.kimbu.feature_search.presentation

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.woynex.kimbu.core.data.local.datastore.KimBuPreferencesKey
import com.woynex.kimbu.core.data.local.room.KimBuDatabase
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.core.utils.Constants.FIREBASE_FIRESTORE_USERS_COLLECTION
import com.woynex.kimbu.core.utils.Constants.FIREBASE_REALTIME_NUMBERS_COLLECTION
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.core.utils.deleteCountryCode
import com.woynex.kimbu.feature_auth.domain.model.User
import com.woynex.kimbu.feature_auth.domain.model.toNumberInfo
import com.woynex.kimbu.feature_search.domain.model.*
import com.woynex.kimbu.feature_search.domain.use_case.GetCallLogsUseCase
import com.woynex.kimbu.feature_search.domain.use_case.GetContactsUseCase
import com.woynex.kimbu.feature_search.domain.use_case.GetLastCallLogsUseCase
import com.woynex.kimbu.feature_search.domain.use_case.UpdateCallLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val updateCallLogsUseCase: UpdateCallLogsUseCase,
    private val getCallLogsUseCase: GetCallLogsUseCase,
    private val getLastCallLogsUseCase: GetLastCallLogsUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val dataStore: DataStore<Preferences>,
    private val database: KimBuDatabase
) : ViewModel() {

    private val _callLogs = MutableStateFlow<List<NumberInfo>>(emptyList())
    val callLogs = _callLogs.asStateFlow()

    private val _lastCallLogs = MutableStateFlow<List<NumberInfo>>(emptyList())
    val lastCallLogs = _lastCallLogs.asStateFlow()

    private val _phoneNumberResponse = MutableStateFlow<Resource<NumberInfo>>(Resource.Empty())
    val phoneNumberResponse = _phoneNumberResponse.asStateFlow()

    val currentUser = dataStore.data.map { preferences ->
        User(
            id = preferences[KimBuPreferencesKey.USER_ID_KEY],
            first_name = preferences[KimBuPreferencesKey.USER_FIRST_NAME_KEY],
            last_name = preferences[KimBuPreferencesKey.USER_LAST_NAME_KEY],
            phone_number = preferences[KimBuPreferencesKey.USER_PHONE_NUMBER_KEY],
            profile_photo = preferences[KimBuPreferencesKey.USER_PROFILE_PHOTO_KEY],
            email = preferences[KimBuPreferencesKey.USER_EMAIL_KEY],
            created_date = preferences[KimBuPreferencesKey.USER_CREATED_DATE_KEY],
            contacts_uploaded = preferences[KimBuPreferencesKey.CONTACTS_UPLOADED_KEY] ?: false,
            has_permission = preferences[KimBuPreferencesKey.HAS_PERMISSION_KEY] ?: false
        )
    }

    fun updateHasPermission(state: Boolean) = viewModelScope.launch {
        val db = Firebase.firestore
        db.collection(FIREBASE_FIRESTORE_USERS_COLLECTION)
            .document(currentUser.first().id!!)
            .update("has_permission", true)
            .addOnSuccessListener {
                viewModelScope.launch {
                    dataStore.edit { preferences ->
                        preferences[KimBuPreferencesKey.HAS_PERMISSION_KEY] = state
                    }
                }
            }
    }

    fun uploadContactsToDatabase() = viewModelScope.launch {
        currentUser.first().has_permission.let { hasPermission ->
            if (hasPermission) {
                currentUser.first().contacts_uploaded.let { isUploaded ->
                    if (!isUploaded) {
                        viewModelScope.launch {
                            val userId = currentUser.first().id
                            userId?.let { uuid ->
                                val database = Firebase.database.reference
                                getContactsUseCase().forEach { contact ->
                                    database.child(FIREBASE_REALTIME_NUMBERS_COLLECTION)
                                        .child(contact.number.deleteCountryCode())
                                        .push()
                                        .setValue(
                                            Tag(
                                                name = contact.name,
                                                uuid = uuid,
                                                number = contact.number
                                            )
                                        )
                                }
                                updateContactsUploaded(uuid)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateContactsUploaded(uuid: String) {
        val db = Firebase.firestore
        db.collection(FIREBASE_FIRESTORE_USERS_COLLECTION)
            .document(uuid)
            .update("contacts_uploaded", true)
            .addOnSuccessListener {
                viewModelScope.launch {
                    dataStore.edit { preferences ->
                        preferences[KimBuPreferencesKey.CONTACTS_UPLOADED_KEY] = true
                    }
                }
            }
    }

    fun searchPhoneNumber(number: String) = viewModelScope.launch {
        _phoneNumberResponse.value = Resource.Loading<NumberInfo>()
        val db = Firebase.firestore
        db.collection(FIREBASE_FIRESTORE_USERS_COLLECTION)
            .whereEqualTo("phone_number", number)
            .get()
            .addOnSuccessListener {
                if (it.documents.size > 0) {
                    val user = it.documents[0].toObject(User::class.java)
                    user?.let {
                        _phoneNumberResponse.value =
                            Resource.Success<NumberInfo>(user.toNumberInfo())
                        updateUserStatistics(user.id!!)
                    }
                } else {
                    _phoneNumberResponse.value =
                        Resource.Error<NumberInfo>("There isn't number")
                }
            }
            .addOnFailureListener { exception ->
                _phoneNumberResponse.value =
                    Resource.Error<NumberInfo>(exception.localizedMessage ?: "There isn't a number")
            }
    }

    private fun updateUserStatistics(searchedUserId: String) = viewModelScope.launch {
        val currentUserId = currentUser.first().id
        currentUserId?.let { id ->
            val db = Firebase.firestore
            db.collection(Constants.FIREBASE_FIRESTORE_STATISTICS_COLLECTION)
                .document(searchedUserId)
                .get()
                .addOnSuccessListener {
                    val list = it.toObject(Statistics::class.java)
                    list?.searched_id_list?.add(
                        SearchedUser(
                            currentUserId,
                            System.currentTimeMillis()
                        )
                    )
                    db.collection(Constants.FIREBASE_FIRESTORE_STATISTICS_COLLECTION)
                        .document(searchedUserId)
                        .set(
                            list ?: Statistics(
                                searched_id_list = arrayListOf(
                                    SearchedUser(
                                        currentUserId,
                                        System.currentTimeMillis()
                                    )
                                )
                            )
                        )
                }
                .addOnFailureListener {
                    it.localizedMessage?.let { it1 -> Log.d("updateUserStatistics", it1) }
                }
        }
    }

    fun clearPhoneNumberResponse() {
        _phoneNumberResponse.value = Resource.Empty()
    }

    fun updateCallLogs() = viewModelScope.launch {
        updateCallLogsUseCase()
    }

    fun getLastCallLogs() = viewModelScope.launch {
        if(lastCallLogs.value.isEmpty()){
            getLastCallLogsUseCase().onEach {
                _lastCallLogs.value = it
            }.launchIn(viewModelScope)
        }
    }

    fun getLogs() = viewModelScope.launch {
        if(callLogs.value.isEmpty()){
            database.callHistoryDao.getLogs().onEach {
                _callLogs.value = it
            }.launchIn(viewModelScope)
        }
    }
}