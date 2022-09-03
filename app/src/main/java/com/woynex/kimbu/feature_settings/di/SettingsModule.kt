package com.woynex.kimbu.feature_settings.di

import com.woynex.kimbu.core.data.local.room.NotificationDao
import com.woynex.kimbu.feature_settings.data.repository.SettingsRepositoryImpl
import com.woynex.kimbu.feature_settings.domain.repository.SettingsRepository
import com.woynex.kimbu.feature_settings.domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {


    @Provides
    @Singleton
    fun provideSettingsRepository(dao: NotificationDao): SettingsRepository {
        return SettingsRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSettingsUseCases(repo: SettingsRepository): SettingsUseCases {
        return SettingsUseCases(
            deleteNotification = DeleteNotification(repo),
            getAllNotifications = GetAllNotifications(repo),
            getUnwatchedNotifications = GetUnwatchedNotifications(repo),
            updateNotification = UpdateNotification(repo)
        )
    }
}