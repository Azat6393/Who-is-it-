package com.woynex.kimbu.feature_search.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.woynex.kimbu.core.data.local.KimBuDatabase
import com.woynex.kimbu.feature_search.data.local.room.CallHistoryPagingSource
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val database: KimBuDatabase,
    private val context: Context
) : SearchRepository {

    override suspend fun getLastCallLogs(): List<NumberInfo> {
        return database.callHistoryDao.getLastCallLogs()
    }

    override suspend fun getCallsFromDao(): List<NumberInfo> {
        return database.callHistoryDao.getAllCall()
    }

    override suspend fun insertCall(calls: List<NumberInfo>) {
        database.callHistoryDao.insertCalls(calls)
    }

    override suspend fun getPagedCalls(): Pager<Int, NumberInfo> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            )
        ) {
            CallHistoryPagingSource(database.callHistoryDao)
        }
    }

    @SuppressLint("Range")
    override suspend fun getCallLogs(): List<NumberInfo> {

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

                cursorCallLogs.let {
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
                                name = if (stringName.isNullOrBlank()) ""
                                else stringName,
                                number = stringNumber,
                                type = typeString,
                                countryCode = if (countryCodeString.isNullOrBlank()) ""
                                else countryCodeString,
                                date = stringDate.toLong()
                            )
                        )
                        size++
                    } while (cursorCallLogs.moveToPrevious() && size <= 1000)
                    cursorCallLogs.close()
                }
                return callLogList
            }
        }
        return emptyList()
    }
}