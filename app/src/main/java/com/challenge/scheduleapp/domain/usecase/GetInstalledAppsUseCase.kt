package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.model.InstalledApp
import com.challenge.scheduleapp.domain.repository.AppRepository
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(): List<InstalledApp> {
        return appRepository.getInstalledApps()
    }
}