package com.woynex.kimbu.feature_search.presentation.feed

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woynex.kimbu.core.Resource
import com.woynex.kimbu.feature_search.data.model.NumberInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _callLogs = MutableStateFlow<Resource<List<NumberInfo>>>(Resource.Empty())
    val callLogs = _callLogs.asStateFlow()

    @SuppressLint("Range")
    fun getCallLog() = viewModelScope.launch {
        val uriCallLogs = Uri.parse("content://call_log/calls")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val cursorCallLogs =
                context.contentResolver.query(
                    uriCallLogs, null, null, null
                )
            val callLogList = arrayListOf<NumberInfo>()
            cursorCallLogs?.let {
                cursorCallLogs.moveToLast()

                for (i in 1..3) {
                    val stringId =
                        cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls._ID))
                    val stringNumber =
                        cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER))
                    val stringName =
                        cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val stringDate =
                        cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.DATE))
                    callLogList.add(
                        NumberInfo(
                            id = stringId.toInt(),
                            name = if (stringNumber.isBlank() || stringNumber == null) stringNumber
                            else stringName,
                            number = stringNumber,
                            date = stringDate.toLong()
                        )
                    )
                    cursorCallLogs.moveToPrevious()
                }
                _callLogs.value = Resource.Success<List<NumberInfo>>(callLogList)
                cursorCallLogs.close()
            }
        }
    }

}