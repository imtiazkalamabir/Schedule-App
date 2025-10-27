package com.challenge.scheduleapp.framework.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.challenge.scheduleapp.R

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    // Notification channels
    const val RECORD_CHANNEL_ID = "app_launch_record_channel"
    const val FOREGROUND_CHANNEL_ID = "overlay_service_channel"


    fun createRecordNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.deleteNotificationChannel(RECORD_CHANNEL_ID)

            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                RECORD_CHANNEL_ID,
                "Schedule app notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications record for scheduled app launch"
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                lightColor = 0xFF0000FF.toInt()
                setSound(defaultUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(false)
                importance = NotificationManager.IMPORTANCE_HIGH
            }

            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Record notification channel created")
        }
    }


    fun createForegroundServiceChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (notificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    "Schedule app foreground service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "For launching scheduled apps"
                }

                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Foreground service channel created")
            }
        }
    }


    fun showLaunchResultNotification(
        context: Context,
        appName: String,
        scheduleId: Long,
        isLaunchSuccess: Boolean,
        reason: String = ""
    ) {
        try {
            createRecordNotificationChannel(context)

            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val title = if (isLaunchSuccess) context.getString(R.string.notification_title, appName) else context.getString(
                R.string.failed_notification_title, appName
            )
            val text = if (isLaunchSuccess) {
                context.getString(R.string.notification_text, appName)
            } else {
                reason.ifEmpty { context.getString(R.string.failed_notification_text, appName) }
            }

            val notificationBuilder = NotificationCompat.Builder(context, RECORD_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setSound(defaultUri)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(false)

            val notification = notificationBuilder.build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(scheduleId.toInt(), notification)

            Log.d(
                TAG,
                "Launch result notification shown: $appName, isLaunchSuccess: $isLaunchSuccess, scheduleId=$scheduleId"
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission denied", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }


    /**
     * Creating a foreground notification for the OverlayLauncherService
     */
    fun createForegroundServiceNotification(context: Context, appName: String): Notification {

        createForegroundServiceChannel(context)

        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.launching_notification_title, appName))
            .setContentText(context.getString(R.string.launching_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}