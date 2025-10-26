package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import com.challenge.scheduleapp.domain.model.InvalidTimeException
import com.challenge.scheduleapp.domain.model.TimeConflictException
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import javax.inject.Inject

class UpdateAppScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val appScheduleManager: AppScheduleManager
) {
    suspend operator fun invoke(scheduleId: Long, newScheduledTime: Long): Result<Unit> {

        if (scheduleRepository.hasTimeConflict(newScheduledTime, scheduleId)) {
            return Result.failure(TimeConflictException("Another schedule already has this time"))
        }

        if (newScheduledTime <= System.currentTimeMillis()) {
            return Result.failure(InvalidTimeException("The scheduled time must be in the future"))
        }

        return try {
            val existingSchedule = scheduleRepository.getScheduleById(scheduleId)
            if (existingSchedule != null) {
                val updatedSchedule = existingSchedule.copy(scheduledTime = newScheduledTime)
                scheduleRepository.updateSchedule(updatedSchedule)

                appScheduleManager.updateAppSchedule(scheduleId, existingSchedule.packageName, newScheduledTime)

                Result.success(Unit)
            } else {
                Result.failure(Exception("Schedule not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    }
}