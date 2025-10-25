package com.challenge.scheduleapp.data.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.challenge.scheduleapp.data.localdb.entity.ScheduleEntity

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long
}