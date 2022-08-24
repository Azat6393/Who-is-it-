package com.woynex.kimbu.feature_search.data.use_case

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_search.data.model.NumberInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetCallLogsUseCase @Inject constructor(
    private val context: Context
) {
    @SuppressLint("Range")
    operator fun invoke(): Flow<Resource<List<NumberInfo>>> = flow {
        try {
            val uriCallLogs = Uri.parse("content://call_log/calls")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val cursorCallLogs =
                    context.contentResolver.query(
                        uriCallLogs, null, null, null
                    )
                val callLogList = arrayListOf<NumberInfo>()
                cursorCallLogs?.let {
                    cursorCallLogs.moveToLast()

                    var size = 0

                    do {
                        val stringId =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls._ID))
                        val stringNumber =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER))
                        val stringName =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.CACHED_NAME))
                        val stringDate =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.DATE))
                        val typeString =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.TYPE))
                        val countryCodeString =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.COUNTRY_ISO))

                        callLogList.add(
                            NumberInfo(
                                id = stringId.toInt(),
                                name = if (stringName.isNullOrBlank()) stringNumber
                                else stringName,
                                number = stringNumber,
                                type = typeString,
                                countryCode = countryCodeString,
                                date = stringDate.toLong()
                            )
                        )
                        size++
                    } while (cursorCallLogs.moveToPrevious() && size <= 120)
                    emit(Resource.Success<List<NumberInfo>>(callLogList))
                    cursorCallLogs.close()
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error<List<NumberInfo>>(e.localizedMessage ?: "Something went wrong"))
        }
    }
}