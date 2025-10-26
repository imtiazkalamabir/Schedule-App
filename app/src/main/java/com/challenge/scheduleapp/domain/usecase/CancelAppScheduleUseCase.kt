package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import javax.inject.Inject

class CancelAppScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val appScheduleManager: AppScheduleManager
) {
    suspend operator fun invoke(scheduleId: Long, newStatus: String): Result<Unit> {
        return try {
            scheduleRepository.cancelSchedule(scheduleId, newStatus)

            appScheduleManager.cancelAppSchedule(scheduleId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}