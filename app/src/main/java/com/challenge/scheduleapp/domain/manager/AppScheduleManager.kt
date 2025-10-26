package com.challenge.scheduleapp.domain.manager

interface AppScheduleManager {
    fun scheduleApp(scheduleId: Long, packageName: String, time: Long)
    fun cancelAppSchedule(scheduleId: Long)
    fun updateAppSchedule(scheduleId: Long, packageName: String, newScheduledTime: Long)
}