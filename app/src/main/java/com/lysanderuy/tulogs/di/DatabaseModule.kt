package com.lysanderuy.tulogs.di

import android.content.Context
import androidx.room.Room
import com.lysanderuy.tulogs.data.local.AlarmDao
import com.lysanderuy.tulogs.data.local.SleepLogDao
import com.lysanderuy.tulogs.data.local.SleepTagDao
import com.lysanderuy.tulogs.data.local.TuLogsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TuLogsDatabase {
        return Room.databaseBuilder(
            context,
            TuLogsDatabase::class.java,
            "tulogs.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAlarmDao(database: TuLogsDatabase): AlarmDao = database.alarmDao()

    @Provides
    fun provideSleepTagDao(database: TuLogsDatabase): SleepTagDao = database.sleepTagDao()

    @Provides
    fun provideSleepLogDao(database: TuLogsDatabase): SleepLogDao = database.sleepLogDao()
}