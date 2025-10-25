package com.challenge.scheduleapp.data.repository

import com.challenge.scheduleapp.data.localdb.dao.ScheduleDao
import com.challenge.scheduleapp.data.localdb.entity.toEntity
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {
    override suspend fun insertSchedule(schedule: AppSchedule): Long {
        return scheduleDao.insertSchedule(schedule.toEntity())
    }

    override suspend fun hasTimeConflict(
        scheduledTime: Long,
        excludeId: Long?
    ): Boolean {
        return false
    }

    override suspend fun getAllSchedules(): List<AppSchedule> {
        return emptyList()
    }

    override suspend fun getScheduleById(scheduleId: Long): AppSchedule? {
        return null
    }

    override suspend fun updateSchedule(schedule: AppSchedule) {

    }

    override suspend fun cancelSchedule(scheduleId: Long) {

    }

    override suspend fun deleteSchedule(scheduleId: Long) {

    }


}