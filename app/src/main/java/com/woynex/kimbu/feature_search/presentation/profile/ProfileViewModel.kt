package com.woynex.kimbu.feature_search.presentation.profile

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.woynex.kimbu.core.data.local.datastore.KimBuPreferencesKey
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.core.utils.Constants.FIREBASE_STORAGE_PROFILE_IMAGES_CHILD
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.core.utils.deleteCountryCode
import com.woynex.kimbu.feature_auth.domain.model.User
import com.woynex.kimbu.feature_auth.domain.model.toNumberInfo
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.model.Tag
import com.woynex.kimbu.feature_search.domain.use_case.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val blockNumberUseCase: BlockNumberUseCase,
    private val unblockNumberUseCase: UnblockNumberUseCase,
    private val checkForBlockedNumberUseCase: CheckForBlockedNumberUseCase,
    private val updateCallNumberUseCase: UpdateCallNumberUseCase,
    private val dataStore: DataStore<Preferences>,
    private val updateLogsNameUseCase: UpdateLogsNameUseCase,
    private val searchContactByNumberUseCase: SearchContactByNumberUseCase
) : ViewModel() {

    private val _isBlocked = MutableStateFlow<Boolean>(false)
    val isBlocked = _isBlocked.asStateFlow()

    private val _numberResponse = MutableStateFlow<Resource<NumberInfo>>(Resource.Empty())
    val numberResponse = _numberResponse.asStateFlow()

    private val _tagsResponse = MutableStateFlow<Resource<List<Tag>>>(Resource.Empty())
    val tagsResponse = _tagsResponse.asStateFlow()

    private val currentUser = dataStore.data.map { preferences ->
        User(
            id = preferences[KimBuPreferencesKey.USER_ID_KEY],
            first_name = preferences[KimBuPreferencesKey.USER_FIRST_NAME_KEY],
            last_name = preferences[KimBuPreferencesKey.USER_LAST_NAME_KEY],
            phone_number = preferences[KimBuPreferencesKey.USER_PHONE_NUMBER_KEY],
            profile_photo = preferences[KimBuPreferencesKey.USER_PROFILE_PHOTO_KEY],
            email = preferences[KimBuPreferencesKey.USER_EMAIL_KEY],
            created_date = preferences[KimBuPreferencesKey.USER_CREATED_DATE_KEY],
            contacts_uploaded = preferences[KimBuPreferencesKey.CONTACTS_UPLOADED_KEY] ?: false
        )
    }

    lateinit var user: User

    init {
        viewModelScope.launch {
            user = currentUser.first()
        }
    }

    fun getTags(number: String) {
        _tagsResponse.value = Resource.Loading<List<Tag>>()
        val database = Firebase.database.reference
        database.child(Constants.FIREBASE_REALTIME_NUMBERS_COLLECTION)
            .child(number.deleteCountryCode())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value != null) {
                        val tagList = mutableSetOf<Tag>()
                        snapshot.children.forEach { snapshot ->
                            val item = snapshot.getValue(Tag::class.java)
                            item?.let { tagList.add(it) }
                        }
                        _tagsResponse.value = Resource.Success<List<Tag>>(tagList.toList())
                    } else {
                        _tagsResponse.value = Resource.Success<List<Tag>>(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _tagsResponse.value = Resource.Error<List<Tag>>(error.message)
                    Log.d("Get Tags", error.message)
                }
            })
    }

    fun searchContactByNumber(number: String): String {
        return searchContactByNumberUseCase(number)
    }

    fun updateLogsName() = viewModelScope.launch {
        updateLogsNameUseCase()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun blockNumber(number: String) = viewModelScope.launch {
        blockNumberUseCase(number).also {
            checkForBlockedNumber(number)
        }
    }

    fun unblockNumber(number: String) = viewModelScope.launch {
        unblockNumberUseCase(number).also {
            checkForBlockedNumber(number)
        }
    }

    fun checkForBlockedNumber(number: String) = viewModelScope.launch {
        checkForBlockedNumberUseCase(number).onEach {
            _isBlocked.value = it
        }.launchIn(viewModelScope)
    }

    fun searchNumber(number: NumberInfo) = viewModelScope.launch {
        val db = Firebase.firestore
        db.collection(Constants.FIREBASE_FIRESTORE_USERS_COLLECTION)
            .whereEqualTo("phone_number", number.number)
            .get()
            .addOnSuccessListener {
                if (it.documents.size > 0) {
                    val user = it.documents[0].toObject(User::class.java)
                    user?.let {
                        _numberResponse.value =
                            Resource.Success<NumberInfo>(user.toNumberInfo())
                        viewModelScope.launch {
                            updateCallNumberUseCase(number)
                        }
                    }
                }
            }
    }

    fun saveNewProfilePhoto(uri: Uri) = viewModelScope.launch {
        val storageRef = Firebase.storage.getReference(FIREBASE_STORAGE_PROFILE_IMAGES_CHILD)
        val profileImagesRef = storageRef.child(user.id!!)
        val imageRef =
            profileImagesRef.child("${UUID.randomUUID()}.jpg")
        imageRef.putFile(uri)
            .addOnSuccessListener {
                it.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { url ->
                        val db = Firebase.firestore
                        db.collection(Constants.FIREBASE_FIRESTORE_USERS_COLLECTION)
                            .document(user.id!!)
                            .update("profile_photo", url.toString())
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    dataStore.edit { preferences ->
                                        preferences[KimBuPreferencesKey.USER_PROFILE_PHOTO_KEY] =
                                            url.toString()
                                        user.profile_photo = url.toString()
                                    }
                                }
                            }
                    }

            }.addOnFailureListener {
                println("Error: ${it.localizedMessage}")
            }
    }

    fun updateName(name: String) = viewModelScope.launch {
        val firstName = name.split(" ")[0]
        val lastName = name.split(" ")[1]
        val db = Firebase.firestore
        db.collection(Constants.FIREBASE_FIRESTORE_USERS_COLLECTION)
            .document(user.id!!)
            .update("first_name", firstName)
            .addOnSuccessListener {
                viewModelScope.launch {
                    dataStore.edit { preferences ->
                        preferences[KimBuPreferencesKey.USER_FIRST_NAME_KEY] = firstName
                        user.first_name = firstName
                    }
                }
            }
        db.collection(Constants.FIREBASE_FIRESTORE_USERS_COLLECTION)
            .document(user.id!!)
            .update("last_name", lastName)
            .addOnSuccessListener {
                viewModelScope.launch {
                    dataStore.edit { preferences ->
                        preferences[KimBuPreferencesKey.USER_LAST_NAME_KEY] = lastName
                        user.last_name = lastName
                    }
                }
            }
    }
}