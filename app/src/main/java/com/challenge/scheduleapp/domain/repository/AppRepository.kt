package com.challenge.scheduleapp.domain.repository

import com.challenge.scheduleapp.domain.model.InstalledApp

interface AppRepository {
    suspend fun getInstalledApps(): List<InstalledApp>
}