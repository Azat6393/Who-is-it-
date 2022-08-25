package com.woynex.kimbu.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.woynex.kimbu.feature_search.data.local.room.CallHistoryDao
import com.woynex.kimbu.feature_search.domain.model.NumberInfo

@Database(
    entities = [NumberInfo::class],
    version = 1
)
abstract class KimBuDatabase : RoomDatabase() {
    abstract val callHistoryDao: CallHistoryDao
}