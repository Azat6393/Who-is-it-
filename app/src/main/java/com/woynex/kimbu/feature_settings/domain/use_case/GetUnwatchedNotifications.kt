package com.woynex.kimbu.feature_settings.domain.use_case

import com.woynex.kimbu.core.domain.model.NotificationModel
import com.woynex.kimbu.feature_settings.domain.repository.SettingsRepository
import javax.inject.Inject

class GetUnwatchedNotifications @Inject constructor(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(): List<NotificationModel> {
        return repo.getUnwatchedNotifications()
    }
}