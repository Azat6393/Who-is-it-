package com.woynex.kimbu.feature_settings.domain.repository

import com.woynex.kimbu.core.domain.model.NotificationModel

interface SettingsRepository {

    suspend fun insertNotification(notificationModel: NotificationModel)

    suspend fun deleteNotification(notificationModel: NotificationModel)

    suspend fun updateNotification(notification: NotificationModel)

    suspend fun getAllNotifications(): List<NotificationModel>

    suspend fun getUnwatchedNotifications(): List<NotificationModel>

}