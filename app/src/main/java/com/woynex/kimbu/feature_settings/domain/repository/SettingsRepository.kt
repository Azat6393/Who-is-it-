package com.woynex.kimbu.feature_settings.domain.repository

import com.woynex.kimbu.core.domain.model.NotificationModel
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun insertNotification(notificationModel: NotificationModel)

    suspend fun deleteNotification(notificationModel: NotificationModel)

    suspend fun updateNotification(notification: NotificationModel)

    suspend fun getAllNotifications(): Flow<List<NotificationModel>>

    suspend fun getUnwatchedNotifications(): Flow<List<NotificationModel>>

}