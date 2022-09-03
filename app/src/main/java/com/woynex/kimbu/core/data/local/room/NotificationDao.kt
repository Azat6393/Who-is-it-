package com.woynex.kimbu.core.data.local.room

import androidx.room.*
import com.woynex.kimbu.core.domain.model.NotificationModel

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notificationModel: NotificationModel)

    @Delete
    suspend fun deleteNotification(notificationModel: NotificationModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNotification(notification: NotificationModel)

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotifications(): List<NotificationModel>

    @Query("SELECT * FROM notifications WHERE is_viewed = 0")
    suspend fun getUnwatchedNotifications(): List<NotificationModel>

}