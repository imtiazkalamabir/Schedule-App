package com.challenge.scheduleapp.di

import com.challenge.scheduleapp.data.manager.AppScheduleManagerImpl
import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Binds
    @Singleton
    abstract fun bindAppScheduleManager(
        appScheduleManagerImpl: AppScheduleManagerImpl
    ): AppScheduleManager
}