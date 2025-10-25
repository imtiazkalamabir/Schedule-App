package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import javax.inject.Inject

class DeleteAppScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: Long) {
        scheduleRepository.deleteSchedule(scheduleId)
    }
}