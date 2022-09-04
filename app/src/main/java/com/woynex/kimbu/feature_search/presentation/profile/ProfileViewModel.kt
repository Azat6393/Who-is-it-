package com.woynex.kimbu.feature_search.presentation.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_auth.domain.model.User
import com.woynex.kimbu.feature_auth.domain.model.toNumberInfo
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.use_case.BlockNumberUseCase
import com.woynex.kimbu.feature_search.domain.use_case.CheckForBlockedNumberUseCase
import com.woynex.kimbu.feature_search.domain.use_case.UnblockNumberUseCase
import com.woynex.kimbu.feature_search.domain.use_case.UpdateCallNumberUseCase
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
    private val updateCallNumberUseCase: UpdateCallNumberUseCase
) : ViewModel() {

    private val _isBlocked = MutableStateFlow<Boolean>(false)
    val isBlocked = _isBlocked.asStateFlow()

    private val _numberResponse = MutableStateFlow<Resource<NumberInfo>>(Resource.Empty())
    val numberResponse = _numberResponse.asStateFlow()

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