package com.woynex.kimbu.feature_search.presentation.profile

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_auth.domain.model.User
import com.woynex.kimbu.feature_auth.domain.model.toNumberInfo
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.model.Tag
import com.woynex.kimbu.feature_search.domain.use_case.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val blockNumberUseCase: BlockNumberUseCase,
    private val unblockNumberUseCase: UnblockNumberUseCase,
    private val checkForBlockedNumberUseCase: CheckForBlockedNumberUseCase,
    private val updateCallNumberUseCase: UpdateCallNumberUseCase,
    private val updateLogsNameUseCase: UpdateLogsNameUseCase,
) : ViewModel() {

    private val _isBlocked = MutableStateFlow<Boolean>(false)
    val isBlocked = _isBlocked.asStateFlow()

    private val _numberResponse = MutableStateFlow<Resource<NumberInfo>>(Resource.Empty())
    val numberResponse = _numberResponse.asStateFlow()

    private val _tagsResponse = MutableStateFlow<Resource<List<Tag>>>(Resource.Empty())
    val tagsResponse = _tagsResponse.asStateFlow()

    var _newName = ""

    fun getTags(number: String) {
        val database = Firebase.database.reference
        database.child(Constants.FIREBASE_NUMBERS_COLLECTION)
            .child(number)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value != null) {
                        val tagList = mutableListOf<Tag>()
                        snapshot.children.forEach { snapshot ->
                            val item = snapshot.getValue(Tag::class.java)
                            item?.let { tagList.add(it) }
                        }
                        _tagsResponse.value = Resource.Success<List<Tag>>(tagList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Get Tags", error.message)
                }
            })
    }

    fun updateLogsName(number: String) = viewModelScope.launch {
        _newName = updateLogsNameUseCase(number)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun blockNumber(number: String) = viewModelScope.launch {
        blockNumberUseCase(number)
    }

    fun unblockNumber(number: String) = viewModelScope.launch {
        unblockNumberUseCase(number)
    }

    fun checkForBlockedNumber(number: String) = viewModelScope.launch {
        checkForBlockedNumberUseCase(number).onEach {
            _isBlocked.value = it
        }.launchIn(viewModelScope)
    }

    fun searchNumber(number: NumberInfo) = viewModelScope.launch {
        val db = Firebase.firestore
        db.collection(Constants.FIREBASE_USERS_COLLECTION)
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
}