package com.woynex.kimbu.feature_search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_search.data.model.NumberInfo
import com.woynex.kimbu.feature_search.data.use_case.GetCallLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getCallLogsUseCase: GetCallLogsUseCase
) : ViewModel() {

    private val _callLogs = MutableStateFlow<Resource<List<NumberInfo>>>(Resource.Empty())
    val callLogs = _callLogs.asStateFlow()

    fun getCallLog() {
        getCallLogsUseCase().onEach {
            _callLogs.value = it
        }.launchIn(viewModelScope)
    }
}