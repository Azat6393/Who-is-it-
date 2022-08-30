package com.woynex.kimbu.feature_search.presentation.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woynex.kimbu.feature_search.domain.use_case.BlockNumberUseCase
import com.woynex.kimbu.feature_search.domain.use_case.CheckForBlockedNumberUseCase
import com.woynex.kimbu.feature_search.domain.use_case.UnblockNumberUseCase
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
    private val checkForBlockedNumberUseCase: CheckForBlockedNumberUseCase
) : ViewModel() {

    private val _isBlocked = MutableStateFlow<Boolean>(false)
    val isBlocked = _isBlocked.asStateFlow()

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
}