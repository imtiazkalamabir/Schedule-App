package com.challenge.scheduleapp.domain.model

data class AppSchedule(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val appIconPath: String? = null,
    val scheduledTime: Long,
    val status: ScheduleStatus,
    val createdAt: Long,
    val executedAt: Long? = null
)

enum class ScheduleStatus {
    PENDING,
    EXECUTED,
    CANCELLED,
    FAILED
}
