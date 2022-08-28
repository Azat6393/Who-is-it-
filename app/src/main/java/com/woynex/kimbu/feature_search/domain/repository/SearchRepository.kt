package com.woynex.kimbu.feature_search.domain.repository

import androidx.paging.Pager
import com.woynex.kimbu.feature_search.domain.model.NumberInfo

interface SearchRepository {

    suspend fun getCallLogs(): List<NumberInfo>

    suspend fun getCallsFromDao(): List<NumberInfo>

    suspend fun insertCall(calls: List<NumberInfo>)

    suspend fun getPagedCalls(): Pager<Int, NumberInfo>

    suspend fun getLastCallLogs(): List<NumberInfo>

    suspend fun searchPhoneNumber(phoneNumber: String): NumberInfo
}