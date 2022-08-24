package com.woynex.kimbu.feature_search

import android.content.Context
import com.woynex.kimbu.feature_search.domain.use_case.GetCallLogsUseCase
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
    fun provideGetCallLogsUseCase(
        @ApplicationContext context: Context
    ): GetCallLogsUseCase {
        return GetCallLogsUseCase(context)
    }

}