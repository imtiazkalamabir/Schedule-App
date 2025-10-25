package com.challenge.scheduleapp.data.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.challenge.scheduleapp.data.localdb.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Query("SELECT * FROM app_schedules ORDER BY scheduledTime DESC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>


}