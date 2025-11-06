package com.challenge.scheduleapp.framework.receiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import com.challenge.scheduleapp.data.manager.AppScheduleManagerImpl.Companion.EXTRA_PACKAGE_NAME
import com.challenge.scheduleapp.data.manager.AppScheduleManagerImpl.Companion.EXTRA_SCHEDULE_ID
import com.challenge.scheduleapp.framework.notification.NotificationHelper
import com.challenge.scheduleapp.framework.service.OverlayLauncherService
import dagger.hilt.android.AndroidEntryPoint
import com.challenge.scheduleapp.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AppLaunchReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "AppLaunchReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called with action: ${intent.action} and intent: $intent")

        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)

        if (scheduleId != -1L && packageName == null) {
            Log.d(
                TAG,
                "Invalid schedule date or package name: scheduleId: $scheduleId, packageName: $packageName"
            )
            return
        }

        val pendingResult: PendingResult = goAsync() // For extending receiver lifetime for async operation

        // Fetching schedule information from database to initiate launch
        scope.launch {
            try {
                val schedule = scheduleRepository.getScheduleById(scheduleId)
                val appName = schedule?.appName ?: packageName as String

                // Logging the alarm trigger
                val currentTime = SimpleDateFormat(
                    context.getString(R.string.schedule_time_pattern),
                    Locale.getDefault()
                )
                    .format(Date(System.currentTimeMillis()))

                Log.d(
                    TAG,
                    "Alarm triggered at $currentTime for app $appName, packageName: $packageName, scheduleId=$scheduleId"
                )

                // Logging to check app foreground or background status
                val isAppInForeground = isAppInForeground(context)
                Log.d(TAG, "Foreground check: isAppInForeground: $isAppInForeground")

                processLaunch(context, scheduleId, packageName!!, appName, isAppInForeground)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing launch: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processLaunch(
        context: Context,
        scheduleId: Long,
        packageName: String,
        appName: String,
        isAppInForeground: Boolean
    ) {
        // Launching the app using overlay service for Android 10+ background compat issue
        val packageManager = context.packageManager
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {

                var launched = false

                val hasOverlayPermission = Settings.canDrawOverlays(context)

                // using overlay service to bypass Android 10+ background restrictions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && hasOverlayPermission && !isAppInForeground) {
                    Log.d(TAG, "Using overlay service to launch: $appName")
                    launchAppWithOverlay(context, packageName, appName, scheduleId)
                    launched = true
                } else {
                    // Direct launching during foreground or when background for Android 9-
                    try {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(launchIntent)
                        launched = true
                        Log.d(TAG, "Launched app using direct launch service: $appName")
                        markAsExecuted(scheduleId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error launching app: ${e.message}", e)
                        markAsFailed(scheduleId)
                        launched = false
                    }
                }

                // Showing notification only for direct launch
                if (launched) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q || !hasOverlayPermission) {
                        showRecordNotification(context, appName, scheduleId, true, "")
                    }
                    Log.d(TAG, "Successfully triggered launch for: $appName")
                } else {
                    showRecordNotification(context, appName, scheduleId, false, "")
                    Log.d(TAG, "Failed to trigger launch for: $appName")
                }

            } else {
                Log.e(TAG, "Launch intent not found for package: $packageName")
                markAsFailed(scheduleId)
                showRecordNotification(context, appName, scheduleId, false, "")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing launch: ${e.message}", e)
            markAsFailed(scheduleId)
            showRecordNotification(context, appName, scheduleId, false, "")
        }
    }

    private fun showRecordNotification(
        context: Context,
        appName: String,
        scheduleId: Long,
        isLaunchSuccess: Boolean,
        reason: String
    ) {
        NotificationHelper.showLaunchResultNotification(
            context,
            appName,
            scheduleId,
            isLaunchSuccess,
            reason
        )
    }

    private suspend fun launchAppWithOverlay(
        context: Context,
        packageName: String,
        appName: String,
        scheduleId: Long
    ) {
        try {
            val serviceIntent = Intent(context, OverlayLauncherService::class.java)
            serviceIntent.putExtra(OverlayLauncherService.EXTRA_PACKAGE_NAME, packageName)
            serviceIntent.putExtra(OverlayLauncherService.EXTRA_APP_NAME, appName)
            serviceIntent.putExtra(OverlayLauncherService.EXTRA_SCHEDULE_ID, scheduleId)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Log.d(TAG, "Overlay service started for: $appName, scheduleId: $scheduleId")

        } catch (e: Exception) {
            Log.e(TAG, "Error launching app with overlay: ${e.message}", e)
            markAsFailed(scheduleId)
            showRecordNotification(context, appName, scheduleId, false, "")
        }
    }


    private suspend fun markAsExecuted(scheduleId: Long) {
        try {
            scheduleRepository.markAsExecuted(scheduleId)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking schedule as executed: ${e.message}", e)
        }
    }

    private suspend fun markAsFailed(scheduleId: Long) {
        try {
            scheduleRepository.markAsFailed(scheduleId)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking schedule as failed: ${e.message}", e)
        }
    }

    private fun isAppInForeground(context: Context?): Boolean {
        return try {
            val activityManager =
                context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses ?: emptyList()
            val myProcess = appProcesses.find { it.processName == context.packageName }
            myProcess?.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        } catch (e: Exception) {
            Log.e(TAG, "Error checking foreground state", e)
            false
        }
    }
}