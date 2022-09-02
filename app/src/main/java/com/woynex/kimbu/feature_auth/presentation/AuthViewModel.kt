package com.woynex.kimbu.feature_auth.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.woynex.kimbu.core.data.local.datastore.KimBuPreferencesKey
import com.woynex.kimbu.core.utils.Constants.FIREBASE_USERS_COLLECTION
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_auth.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val currentUser = dataStore.data.map { preferences ->
        User(
            id = preferences[KimBuPreferencesKey.USER_ID_KEY] ?: "",
            first_name = preferences[KimBuPreferencesKey.USER_FIRST_NAME_KEY] ?: "",
            last_name = preferences[KimBuPreferencesKey.USER_LAST_NAME_KEY] ?: "",
            phone_number = preferences[KimBuPreferencesKey.USER_PHONE_NUMBER_KEY] ?: "",
            profile_photo = preferences[KimBuPreferencesKey.USER_PROFILE_PHOTO_KEY] ?: "",
            email = preferences[KimBuPreferencesKey.USER_EMAIL_KEY] ?: "",
            created_date = preferences[KimBuPreferencesKey.USER_CREATED_DATE] ?: 0
        )
    }

    private val _isAuth = MutableStateFlow(false)
    val isAuth = _isAuth.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _signUpResponse = MutableStateFlow<Resource<User>>(Resource.Empty())
    val signUpResponse = _signUpResponse.asStateFlow()

    private val _signInResponse = MutableStateFlow<Resource<User>>(Resource.Empty())
    val signInResponse = _signInResponse.asStateFlow()

    private val _phoneNumberResponse = MutableStateFlow<Resource<String>>(Resource.Empty())
    val phoneNumberResponse = _phoneNumberResponse.asStateFlow()

    init {
        _isAuth.value = Firebase.auth.currentUser != null
        viewModelScope.launch {
            delay(2000)
            _isLoading.value = false
        }
    }

    private fun updateCurrentUser(user: User) = viewModelScope.launch {
        dataStore.edit { preferences ->
            preferences[KimBuPreferencesKey.USER_ID_KEY] = user.id ?: ""
            preferences[KimBuPreferencesKey.USER_FIRST_NAME_KEY] = user.first_name ?: ""
            preferences[KimBuPreferencesKey.USER_LAST_NAME_KEY] = user.last_name ?: ""
            preferences[KimBuPreferencesKey.USER_PHONE_NUMBER_KEY] = user.phone_number ?: ""
            preferences[KimBuPreferencesKey.USER_PROFILE_PHOTO_KEY] = user.profile_photo ?: ""
            preferences[KimBuPreferencesKey.USER_EMAIL_KEY] = user.email ?: ""
            preferences[KimBuPreferencesKey.USER_CREATED_DATE] = user.created_date ?: 0
        }
    }

    private fun updatePhoneNumberFromDataStore(number: String) = viewModelScope.launch {
        dataStore.edit { preferences ->
            preferences[KimBuPreferencesKey.USER_PHONE_NUMBER_KEY] = number
        }
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        lastName: String,
        firstName: String,
    ) = viewModelScope.launch {
        _signUpResponse.value = Resource.Loading<User>()
        val auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.let { user ->
                    createUser(
                        User(
                            id = user.uid,
                            first_name = firstName,
                            last_name = lastName,
                            phone_number = "",
                            profile_photo = "",
                            email = email,
                            created_date = System.currentTimeMillis()
                        )
                    )
                }
            }
            .addOnFailureListener {
                _signUpResponse.value = Resource.Error<User>(it.localizedMessage ?: "Error")
            }
    }

    fun signInWithEmail(
        email: String, password: String,
    ) = viewModelScope.launch {
        _signInResponse.value = Resource.Loading<User>()
        val auth = Firebase.auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.let { user ->
                    getUser(user.uid)
                }
            }
            .addOnFailureListener {
                _signInResponse.value = Resource.Error<User>(it.localizedMessage ?: "Error")
            }
    }

    fun logInWithFacebook(idToken: AccessToken) = viewModelScope.launch {
        _signInResponse.value = Resource.Loading<User>()
        val auth = Firebase.auth
        val firebaseCredential = FacebookAuthProvider.getCredential(idToken.token)
        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener {
                it.user?.let { user ->
                    getUserIfExists(
                        User(
                            id = user.uid,
                            first_name = user.displayName.toString().split(" ")[0] ?: "",
                            last_name = user.displayName.toString().split(" ")[1] ?: "",
                            phone_number = user.phoneNumber ?: "",
                            profile_photo = user.photoUrl.toString() ?: "",
                            email = user.email,
                            created_date = System.currentTimeMillis()
                        )
                    )
                }
            }
            .addOnFailureListener {
                _signUpResponse.value =
                    Resource.Error<User>(it.localizedMessage ?: "Error")
            }
    }

    fun logInWithGoogle(idToken: String, firstName: String, lastName: String) =
        viewModelScope.launch {
            _signInResponse.value = Resource.Loading<User>()
            val auth = Firebase.auth
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential)
                .addOnSuccessListener {
                    it.user?.let { user ->
                        getUserIfExists(
                            User(
                                id = user.uid,
                                first_name = firstName,
                                last_name = lastName,
                                phone_number = user.phoneNumber ?: "",
                                profile_photo = user.photoUrl.toString() ?: "",
                                email = user.email,
                                created_date = System.currentTimeMillis()
                            )
                        )
                    }
                }
                .addOnFailureListener {
                    _signUpResponse.value =
                        Resource.Error<User>(it.localizedMessage ?: "Error")
                }
        }

    private fun getUserIfExists(user: User) {
        val db = Firebase.firestore
        db.collection(FIREBASE_USERS_COLLECTION)
            .document(user.id!!)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val document = it.result
                    if (document.exists()) {
                        val response = document.toObject(User::class.java)
                        response?.let { result ->
                            updateCurrentUser(result)
                            _signUpResponse.value = Resource.Success<User>(result)
                        }
                    } else {
                        createUser(user)
                    }
                } else {
                    _signUpResponse.value =
                        Resource.Error<User>(it.exception?.localizedMessage ?: "Error")
                }
            }
    }

    private fun getUser(id: String) {
        val db = Firebase.firestore
        db.collection(FIREBASE_USERS_COLLECTION)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    updateCurrentUser(it)
                    _signInResponse.value = Resource.Success<User>(it)
                }
            }
            .addOnFailureListener {
                _signInResponse.value = Resource.Error<User>(it.localizedMessage ?: "Error")
            }
    }

    private fun createUser(
        user: User
    ) {
        val db = Firebase.firestore
        db.collection(FIREBASE_USERS_COLLECTION)
            .document(user.id!!)
            .set(user)
            .addOnSuccessListener {
                updateCurrentUser(user)
                _signUpResponse.value = Resource.Success<User>(user)
            }
            .addOnFailureListener { e ->
                _signUpResponse.value = Resource.Error<User>(e.localizedMessage ?: "Error")
            }
    }

    fun updatePhoneNumber(
        number: String,
        userId: String
    ) {
        _phoneNumberResponse.value = Resource.Loading<String>()
        val db = Firebase.firestore
        db.collection(FIREBASE_USERS_COLLECTION)
            .document(userId)
            .update("phone_number", number)
            .addOnSuccessListener {
                updatePhoneNumberFromDataStore(number)
                _phoneNumberResponse.value = Resource.Success<String>("Successful")
            }
            .addOnFailureListener { e ->
                _phoneNumberResponse.value = Resource.Error<String>(e.localizedMessage ?: "Error")
            }
    }
}