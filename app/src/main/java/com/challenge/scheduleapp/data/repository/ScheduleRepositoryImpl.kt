package com.challenge.scheduleapp.data.repository

import com.challenge.scheduleapp.data.localdb.dao.ScheduleDao
import com.challenge.scheduleapp.data.localdb.entity.toDomain
import com.challenge.scheduleapp.data.localdb.entity.toEntity
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.model.ScheduleStatus
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

    override suspend fun hasTimeConflict(scheduledTime: Long, excludeId: Long?): Boolean {
        // Checking for scheduling time conflicts within 1 min range
        val timeFrame = 60 * 1000 // 1 min in ms
        val startTime = scheduledTime - timeFrame
        val endTime = scheduledTime + timeFrame

        val pendingScheduleCount = scheduleDao.getPendingSchedulesCountInBetween(
            startTime = startTime,
            endTime = endTime,
            excludeId = excludeId ?: -1
        )
        return pendingScheduleCount > 0
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
        return scheduleDao.getScheduleById(scheduleId)?.toDomain()
    }

    override suspend fun updateSchedule(schedule: AppSchedule) {
        scheduleDao.updateSchedule(schedule.toEntity())
    }

    override suspend fun cancelSchedule(scheduleId: Long, newStatus: String) {
        scheduleDao.updateStatusById(scheduleId, newStatus)
    }

    override suspend fun deleteSchedule(scheduleId: Long) {
        scheduleDao.deleteSchedule(scheduleId)
    }

    override suspend fun markAsExecuted(scheduleId: Long) {
        val schedule = scheduleDao.getScheduleById(scheduleId) ?: return
        val updatedSchedule = schedule.copy(status = ScheduleStatus.EXECUTED.name)
        scheduleDao.updateSchedule(updatedSchedule)
    }

    override suspend fun markAsFailed(scheduleId: Long) {
        val schedule = scheduleDao.getScheduleById(scheduleId) ?: return
        val updatedSchedule = schedule.copy(status = ScheduleStatus.FAILED.name)
        scheduleDao.updateSchedule(updatedSchedule)
    }
}