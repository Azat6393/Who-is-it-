package com.woynex.kimbu.feature_search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.use_case.GetCallLogsUseCase
import com.woynex.kimbu.feature_search.domain.use_case.GetLastCallLogsUseCase
import com.woynex.kimbu.feature_search.domain.use_case.UpdateCallLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val updateCallLogsUseCase: UpdateCallLogsUseCase,
    private val getCallLogsUseCase: GetCallLogsUseCase,
    private val getLastCallLogsUseCase: GetLastCallLogsUseCase
) : ViewModel() {

    private val _callLogs = MutableStateFlow<PagingData<NumberInfo>?>(null)
    val callLogs = _callLogs.asStateFlow()

    private val _lastCallLogs = MutableStateFlow<List<NumberInfo>>(emptyList())
    val lastCallLogs = _lastCallLogs.asStateFlow()

    fun getCallLog() = viewModelScope.launch {
        getCallLogsUseCase()
            .flow.cachedIn(viewModelScope)
            .onEach {
                _callLogs.value = it
            }.launchIn(viewModelScope)
    }

    fun updateCallLogs() = viewModelScope.launch {
        updateCallLogsUseCase()
    }

    fun getLastCallLogs() {
        getLastCallLogsUseCase().onEach {
            _lastCallLogs.value = it
        }.launchIn(viewModelScope)
    }
}