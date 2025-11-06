package com.challenge.scheduleapp.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.challenge.scheduleapp.R
import com.challenge.scheduleapp.domain.manager.AppScheduleManager
import com.challenge.scheduleapp.framework.receiver.AppLaunchReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AppScheduleManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppScheduleManager {

    companion object {
        private const val TAG = "AppScheduleManagerImpl"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleApp(scheduleId: Long, packageName: String, time: Long) {
        val intent = Intent(context, AppLaunchReceiver::class.java)
        intent.putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val scheduledTime =
            SimpleDateFormat(context.getString(R.string.schedule_time_pattern), Locale.getDefault())
                .format(Date(time))

        // Using exact alarm here for precise scheduling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
                Log.d(
                    TAG,
                    "Exact Alarm scheduled at $scheduledTime for package: $packageName with scheduleId: $scheduleId"
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm permission not granted, scheduling using normal alarm")
                Log.d(
                    TAG,
                    "Normal Alarm scheduled at $scheduledTime for package: $packageName with scheduleId: $scheduleId"
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
            Log.d(
                TAG,
                "Normal Alarm scheduled at $scheduledTime for package: $packageName with scheduleId: $scheduleId"
            )
        }
    }

    override fun cancelAppSchedule(scheduleId: Long) {
        val intent = Intent(context, AppLaunchReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Alarm cancelled for scheduleId: $scheduleId")
    }

    override fun updateAppSchedule(scheduleId: Long, packageName: String, newScheduledTime: Long) {
        cancelAppSchedule(scheduleId)
        scheduleApp(scheduleId, packageName, newScheduledTime)
    }
}