package com.challenge.scheduleapp.data.localdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.model.ScheduleStatus

@Entity(tableName = "app_schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val appIconPath: String? = null,
    val scheduledTime: Long,
    val status: String,
    val createdAt: Long,
    val executedAt: Long? = null
)

fun ScheduleEntity.toDomain(): AppSchedule {
    return AppSchedule(
        id = id,
        packageName = packageName,
        appName = appName,
        appIconPath = appIconPath,
        scheduledTime = scheduledTime,
        status = ScheduleStatus.valueOf(status),
        createdAt = createdAt,
        executedAt = executedAt
    )
}

fun AppSchedule.toEntity(): ScheduleEntity {
    return ScheduleEntity(
        id = id,
        packageName = packageName,
        appName = appName,
        appIconPath = appIconPath,
        scheduledTime = scheduledTime,
        status = status.name,
        createdAt = createdAt,
        executedAt = executedAt
    )

}