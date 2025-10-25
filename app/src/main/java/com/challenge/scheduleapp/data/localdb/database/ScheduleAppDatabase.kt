package com.challenge.scheduleapp.data.localdb.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.challenge.scheduleapp.data.localdb.dao.ScheduleDao
import com.challenge.scheduleapp.data.localdb.entity.ScheduleEntity

@Database(
    entities = [ScheduleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScheduleAppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
}
