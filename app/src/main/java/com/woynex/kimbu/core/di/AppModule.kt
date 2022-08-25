package com.woynex.kimbu.core.di

import android.app.Application
import androidx.room.Room
import com.woynex.kimbu.core.data.local.KimBuDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKimBuDatabase(
        app: Application
    ): KimBuDatabase {
        return Room.databaseBuilder(
            app,
            KimBuDatabase::class.java,
            "kim_bu_database"
        ).build()
    }
}