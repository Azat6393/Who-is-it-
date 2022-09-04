package com.woynex.kimbu.feature_settings.data.repository

import com.woynex.kimbu.core.data.local.room.NotificationDao
import com.woynex.kimbu.core.domain.model.NotificationModel
import com.woynex.kimbu.feature_settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : SettingsRepository {

    override suspend fun insertNotification(notificationModel: NotificationModel) {
        return dao.insertNotification(notificationModel)
    }

    override suspend fun deleteNotification(notificationModel: NotificationModel) {
        return dao.deleteNotification(notificationModel)
    }

    override suspend fun updateNotification(notification: NotificationModel) {
        return dao.updateNotification(notification)
    }

    override suspend fun getAllNotifications(): Flow<List<NotificationModel>> {
        return dao.getAllNotifications()
    }

    override suspend fun getUnwatchedNotifications(): Flow<List<NotificationModel>> {
        return dao.getUnwatchedNotifications()
    }
}