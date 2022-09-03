package com.woynex.kimbu.feature_settings.domain.use_case

data class SettingsUseCases(
    val deleteNotification: DeleteNotification,
    val getAllNotifications: GetAllNotifications,
    val getUnwatchedNotifications: GetUnwatchedNotifications,
    val updateNotification: UpdateNotification
    )
