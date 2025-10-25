package com.challenge.scheduleapp.data.repository

import com.challenge.scheduleapp.data.localdb.dao.ScheduleDao
import com.challenge.scheduleapp.data.localdb.entity.toDomain
import com.challenge.scheduleapp.data.localdb.entity.toEntity
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override suspend fun getAllSchedules(): Flow<List<AppSchedule>> {
        return scheduleDao.getAllSchedules().map { entities ->
            entities
                .filter { it.status.isNotEmpty() }
                .map { it.toDomain() }
                .sortedByDescending { it.scheduledTime }
        }
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