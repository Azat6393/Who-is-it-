package com.woynex.kimbu.feature_search.di

import android.content.Context
import com.woynex.kimbu.core.data.local.KimBuDatabase
import com.woynex.kimbu.feature_search.data.repository.SearchRepositoryImpl
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import com.woynex.kimbu.feature_search.domain.use_case.GetCallLogsUseCase
import com.woynex.kimbu.feature_search.domain.use_case.UpdateCallLogsUseCase
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
}