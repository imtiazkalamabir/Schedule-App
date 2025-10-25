package com.challenge.scheduleapp.domain.usecase

import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllAppScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(): Flow<List<AppSchedule>> {
        return scheduleRepository.getAllSchedules()
    }
}