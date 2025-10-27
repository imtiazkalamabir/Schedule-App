package com.challenge.scheduleapp.framework.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import com.challenge.scheduleapp.domain.repository.ScheduleRepository
import com.challenge.scheduleapp.framework.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground Service for launching app from overlay window - It creates a transparent 1*1 overlay window to avoid
 * background activity launch restrictions in Android 10+.
 */
@AndroidEntryPoint
class OverlayLauncherService : Service() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var windowManager: WindowManager? = null

    private var overlayView: FrameLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "OverlayLauncherService"
        private const val NOTIFICATION_ID = 1
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val packageName = intent?.getStringExtra(EXTRA_PACKAGE_NAME)
        val appName = intent?.getStringExtra(EXTRA_APP_NAME)
        val scheduleId = intent?.getLongExtra(EXTRA_SCHEDULE_ID, -1L) ?: -1L

        // Creating notification for foreground services for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification =
                NotificationHelper.createForegroundServiceNotification(this, appName!!)
            startForeground(NOTIFICATION_ID, notification)
        }

        if (packageName != null && appName != null && scheduleId != -1L) {
            Log.d(TAG, "Starting overlay service for: $appName, scheduleId: $scheduleId")
            launchAppFromOverlay(packageName, appName, scheduleId)
        } else {
            Log.e(
                TAG,
                "Invalid package name or app name: packageName: $packageName, appName: $appName, scheduleId: $scheduleId"
            )
        }

        return START_NOT_STICKY
    }

    private fun launchAppFromOverlay(packageName: String, appName: String, scheduleId: Long) {
        var isLaunchSuccess = false

        try {
            // Checking if app is installed
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent == null) {
                Log.e(TAG, "App not found: $packageName")
                markAsFailed(scheduleId)
                showFailureNotification(appName, scheduleId, "App not found")
                cleanup()
                return
            }

            // Creating a small overlay overlay window
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            overlayView = FrameLayout(this)

            val layoutParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                )
            }

            layoutParams.gravity = Gravity.TOP or Gravity.START
            layoutParams.x = 0
            layoutParams.y = 0

            // Adding the overlay to the windowManager
            windowManager?.addView(overlayView, layoutParams)

            // Launching the app here
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(launchIntent)
            Log.d(TAG, "Successfully launched $appName from overlay")
            isLaunchSuccess = true

            markAsExecuted(scheduleId)
            showSuccessNotification(appName, scheduleId)

            overlayView?.postDelayed(
                {
                    cleanup()
                }, 500
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}", e)
            if (!isLaunchSuccess) {
                markAsFailed(scheduleId)
                showFailureNotification(appName, scheduleId, "Error launching app")
            }
            // Cleaning up the window and overlay view immediately after launch
            cleanup()
        }
    }

    private fun markAsExecuted(scheduleId: Long) {
        scope.launch {
            try {
                scheduleRepository.markAsExecuted(scheduleId)
                Log.d(TAG, "Schedule marked as executed: scheduleId: $scheduleId")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking schedule as executed: ${e.message}", e)
            }
        }
    }

    private fun markAsFailed(scheduleId: Long) {
        scope.launch {
            try {
                scheduleRepository.markAsFailed(scheduleId)
                Log.d(TAG, "Schedule marked as failed: scheduleId: $scheduleId")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking schedule as failed: ${e.message}", e)
            }
        }
    }

    private fun showSuccessNotification(appName: String, scheduleId: Long) {
        NotificationHelper.showLaunchResultNotification(this, appName, scheduleId, true, "")
    }

    private fun showFailureNotification(appName: String, scheduleId: Long, reason: String) {
        NotificationHelper.showLaunchResultNotification(this, appName, scheduleId, false, reason)
    }

    private fun cleanup() {
        try {
            overlayView?.let { view ->
                windowManager?.removeView(view)
            }
            overlayView = null
            windowManager = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up: ${e.message}", e)
        } finally {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        Log.d(TAG, "Overlay launch service destroyed")
    }
}