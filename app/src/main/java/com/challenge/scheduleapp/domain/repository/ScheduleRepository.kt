package com.challenge.scheduleapp.domain.repository

import com.challenge.scheduleapp.domain.model.AppSchedule

interface ScheduleRepository {

    suspend fun insertSchedule(schedule: AppSchedule): Long

    suspend fun hasTimeConflict(scheduledTime: Long, excludeId: Long? = null): Boolean

    suspend fun getAllSchedules(): List<AppSchedule>

    suspend fun getScheduleById(scheduleId: Long): AppSchedule?

    suspend fun updateSchedule(schedule: AppSchedule)

    suspend fun cancelSchedule(scheduleId: Long)

    suspend fun deleteSchedule(scheduleId: Long)
}