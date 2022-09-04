package com.woynex.kimbu.feature_settings.domain.use_case

import com.woynex.kimbu.core.domain.model.NotificationModel
import com.woynex.kimbu.feature_settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllNotifications @Inject constructor(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(): Flow<List<NotificationModel>> = repo.getAllNotifications()

}