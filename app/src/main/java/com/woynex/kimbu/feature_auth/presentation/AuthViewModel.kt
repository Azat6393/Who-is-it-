package com.woynex.kimbu.feature_auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.woynex.kimbu.core.utils.Constants.FIREBASE_USERS_COLLECTION
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_auth.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _isAuth = MutableStateFlow(true)
    val isAuth = _isAuth.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _signUpResponse = MutableStateFlow<Resource<User>>(Resource.Empty())
    val signUpResponse = _signUpResponse.asStateFlow()

    private val _signInResponse = MutableStateFlow<Resource<User>>(Resource.Empty())
    val signInResponse = _signInResponse.asStateFlow()


    init {
        _isAuth.value = Firebase.auth.currentUser != null
        viewModelScope.launch {
            delay(2000)
            _isLoading.value = false
        }
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        lastName: String,
        firstName: String,
        phoneNumber: String
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
                            phone_number = phoneNumber,
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

    private fun getUser(id: String) {
        val db = Firebase.firestore
        db.collection(FIREBASE_USERS_COLLECTION)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    _signInResponse.value = Resource.Success<User>(it)
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
                    createUser(
                        User(
                            id = user.uid,
                            first_name = user.displayName,
                            last_name = "",
                            phone_number = user.phoneNumber,
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
                        createUser(
                            User(
                                id = user.uid,
                                first_name = firstName,
                                last_name = lastName,
                                phone_number = user.phoneNumber,
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

    private fun createUser(
        user: User
    ) {
        val db = Firebase.firestore
        db.collection(FIREBASE_USERS_COLLECTION)
            .document(user.id!!)
            .set(user)
            .addOnSuccessListener {
                _signUpResponse.value = Resource.Success<User>(user)
            }
            .addOnFailureListener { e ->
                _signUpResponse.value = Resource.Error<User>(e.localizedMessage ?: "Error")
            }
    }
}