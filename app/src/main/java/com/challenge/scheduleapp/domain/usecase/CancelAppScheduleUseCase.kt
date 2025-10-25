package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import javax.inject.Inject

class CancelAppScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: Long, newStatus: String): Result<Unit> {
        return try {
            scheduleRepository.cancelSchedule(scheduleId, newStatus)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}