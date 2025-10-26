package com.challenge.scheduleapp.data.manager

import android.content.Context
import android.content.Intent
import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppScheduleManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppScheduleManager {

    companion object {
        private const val TAG = "AppScheduleManagerImpl"
    }

    override fun scheduleApp(scheduleId: Long, packageName: String, time: Long) {
        // val intent = Intent(context, )

    }

    override fun cancelAppSchedule(scheduleId: Long) {

    }

    override fun updateAppSchedule(scheduleId: Long, packageName: String, newScheduledTime: Long) {

    }

}