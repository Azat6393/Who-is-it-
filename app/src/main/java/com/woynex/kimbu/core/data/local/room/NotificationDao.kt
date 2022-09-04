package com.woynex.kimbu.core.data.local.room

import androidx.room.*
import com.woynex.kimbu.core.domain.model.NotificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notificationModel: NotificationModel)

    @Delete
    suspend fun deleteNotification(notificationModel: NotificationModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNotification(notification: NotificationModel)

    @Query("SELECT * FROM notifications")
    fun getAllNotifications(): Flow<List<NotificationModel>>

    @Query("SELECT * FROM notifications WHERE is_viewed = 0")
    fun getUnwatchedNotifications(): Flow<List<NotificationModel>>

}