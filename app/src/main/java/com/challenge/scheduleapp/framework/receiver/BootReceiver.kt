package com.challenge.scheduleapp.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import com.challenge.scheduleapp.domain.model.ScheduleStatus
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import com.challenge.scheduleapp.framework.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var appScheduleManager: AppScheduleManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "BootReceiver"
    }


    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()

            scope.launch {
                try {
                    val schedules = scheduleRepository.getAllSchedules().first()
                    val currentTime = System.currentTimeMillis()

                    // Future schedules
                    val futureSchedules = schedules.filter {
                        it.status == ScheduleStatus.PENDING && it.scheduledTime > currentTime
                    }

                    // Past missed schedules
                    val missedSchedules = schedules.filter {
                        it.status == ScheduleStatus.PENDING && it.scheduledTime <= currentTime
                    }

                    // Rescheduling future schedules
                    futureSchedules.forEach { schedule ->
                        appScheduleManager.scheduleApp(
                            scheduleId = schedule.id,
                            packageName = schedule.packageName,
                            time = schedule.scheduledTime
                        )
                    }

                    if (futureSchedules.isNotEmpty()) {
                        Log.d(TAG, "Scheduled ${futureSchedules.size} future schedules")
                    }

                    // Updating missed schedules as FAILED and showing notification
                    missedSchedules.forEach { schedule ->
                        scheduleRepository.markAsFailed(schedule.id)
                        showMissedScheduleNotification(context, schedule.appName, schedule.id)
                        Log.d(
                            TAG,
                            "Marked missed schedule as FAILED: ${schedule.appName}, which was scheduled at ${schedule.scheduledTime}"
                        )
                    }

                    if (missedSchedules.isNotEmpty()) {
                        Log.d(TAG, "Marked ${missedSchedules.size} missed schedules as FAILED")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing schedules after boot: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showMissedScheduleNotification(
        context: Context,
        appName: String,
        scheduleId: Long
    ) {
        NotificationHelper.showLaunchResultNotification(
            context,
            appName,
            scheduleId,
            false,
            "Missed schedule"
        )
    }
}