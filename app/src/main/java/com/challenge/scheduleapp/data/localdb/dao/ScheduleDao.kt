package com.challenge.scheduleapp.data.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.challenge.scheduleapp.data.localdb.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Query("SELECT * FROM app_schedules ORDER BY scheduledTime DESC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM app_schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): ScheduleEntity?

    @Query("UPDATE app_schedules SET status = :newStatus WHERE id = :scheduleId")
    suspend fun updateStatusById(scheduleId: Long, newStatus: String)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM app_schedules WHERE id = :scheduleId")
    suspend fun deleteSchedule(scheduleId: Long)

    @Query("SELECT * FROM app_schedules WHERE status = 'PENDING' AND scheduledTime BETWEEN :startTime AND :endTime AND id != :excludeId")
    fun getPendingSchedulesCountInBetween(startTime: Long, endTime: Long, excludeId: Long = -1): Int

}