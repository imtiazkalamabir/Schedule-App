package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.model.InvalidTimeException
import com.challenge.scheduleapp.domain.model.ScheduleStatus
import com.challenge.scheduleapp.domain.model.TimeConflictException
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import javax.inject.Inject

class AddAppScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val appScheduleManager: AppScheduleManager
) {
    suspend operator fun invoke(
        packageName: String,
        appName: String,
        scheduledTime: Long
    ): Result<Long> {
        return try {
            if (scheduleRepository.hasTimeConflict(scheduledTime)) {
                Result.failure(TimeConflictException("Time conflicting with another schedule"))
            } else if (scheduledTime <= System.currentTimeMillis()) {
                Result.failure(InvalidTimeException("The scheduled time must be in the future"))
            } else {
                val scheduleId = scheduleRepository.insertSchedule(
                    AppSchedule(
                        packageName = packageName,
                        appName = appName,
                        scheduledTime = scheduledTime,
                        status = ScheduleStatus.PENDING,
                        createdAt = System.currentTimeMillis()
                    )
                )

                appScheduleManager.scheduleApp(scheduleId, packageName, scheduledTime)

                Result.success(scheduleId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}