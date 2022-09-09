package com.woynex.kimbu.feature_search.domain.repository

import androidx.paging.Pager
import com.woynex.kimbu.feature_search.domain.model.Contact
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    suspend fun getCallLogs(): List<NumberInfo>

    suspend fun getCallsFromDao(): List<NumberInfo>

    suspend fun insertCall(calls: List<NumberInfo>)

    suspend fun getPagedCalls(): Pager<Int, NumberInfo>

    suspend fun getLastCallLogs(): Flow<List<NumberInfo>>

    suspend fun updateCallNumber(callNumber: NumberInfo)

    suspend fun getAllContacts(): List<Contact>

    suspend fun updateLogsName()

    fun searchContactByNumber(number: String): String
}