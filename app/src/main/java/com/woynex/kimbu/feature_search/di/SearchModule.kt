package com.woynex.kimbu.feature_search.di

import android.content.Context
import com.woynex.kimbu.core.data.local.room.KimBuDatabase
import com.woynex.kimbu.feature_search.data.repository.BlockedNumberRepositoryImpl
import com.woynex.kimbu.feature_search.data.repository.SearchRepositoryImpl
import com.woynex.kimbu.feature_search.domain.repository.BlockedNumberRepository
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import com.woynex.kimbu.feature_search.domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

    @Provides
    @Singleton
    fun provideSearchRepository(
        database: KimBuDatabase,
        @ApplicationContext context: Context
    ): SearchRepository {
        return SearchRepositoryImpl(database, context)
    }

    @Provides
    @Singleton
    fun provideBlockedNumberRepository(
        @ApplicationContext context: Context
    ): BlockedNumberRepository {
        return BlockedNumberRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideGetCallLogsUseCase(
        repository: SearchRepository
    ): GetCallLogsUseCase {
        return GetCallLogsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateCallLogsUseCase(
        repository: SearchRepository
    ) = UpdateCallLogsUseCase(repository)

    @Provides
    @Singleton
    fun provideLastCallLogsUseCase(
        repository: SearchRepository
    ) = GetLastCallLogsUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateCallNumberUseCase(
        repository: SearchRepository
    ) = UpdateCallNumberUseCase(repository)

    @Provides
    @Singleton
    fun provideGetContactsUseCase(
        repository: SearchRepository
    ) = GetContactsUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateLogsNameUseCase(
        repository: SearchRepository
    ) = UpdateLogsNameUseCase(repository)

    @Provides
    @Singleton
    fun provideSearchContactByNameUseCase(
        repository: SearchRepository
    ) = SearchContactByNumberUseCase(repository)
}