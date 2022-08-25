package com.woynex.kimbu.feature_search.data.local.room

import androidx.room.*
import com.woynex.kimbu.feature_search.domain.model.NumberInfo

@Dao
interface CallHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<NumberInfo>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCall(call: NumberInfo)

    @Query("SELECT * FROM number_info")
    suspend fun getAllCall(): List<NumberInfo>

    @Query("SELECT * FROM number_info ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedList(limit: Int, offset: Int): List<NumberInfo>

    @Query("SELECT * FROM number_info ORDER BY id DESC LIMIT 3")
    suspend fun getLastCallLogs(): List<NumberInfo>

}