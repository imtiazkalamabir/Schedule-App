package com.challenge.scheduleapp.di

import android.content.Context
import androidx.room.Room
import com.challenge.scheduleapp.data.localdb.database.ScheduleAppDatabase
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
    fun provideScheduleAppDatabase(@ApplicationContext context: Context): ScheduleAppDatabase {
        return Room.databaseBuilder(
            context,
            ScheduleAppDatabase::class.java,
            "schedule_app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(database: ScheduleAppDatabase) = database.scheduleDao()

}