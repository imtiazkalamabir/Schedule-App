# Android References Code Challenge

Here are the android development references used for the **Code Challenge** project.  
This includes documentation and articles on permissions, exact alarms, background services, overlays, system alart window and other related Android components.

---

## Package Visibility:

- [Package Visibility Overview](https://developer.android.com/training/package-visibility)
- [Declaring Package Visibility](https://developer.android.com/training/package-visibility/declaring)
- [Play Store Policy on Package Visibility](https://support.google.com/googleplay/android-developer/answer/10158779)

---

## LiveData (Observe Once):

- [Observe LiveData Only Once (Medium Article)](https://medium.com/@ttdevelopment/observe-livedata-only-once-in-android-kotlin-3acb969c4864)

---

## Runtime Permissions:

- [Permissions Overview](https://developer.android.com/guide/topics/permissions/overview)
- [Requesting Permissions](https://developer.android.com/training/permissions/requesting)
- [POST_NOTIFICATIONS](https://developer.android.com/reference/android/Manifest.permission#POST_NOTIFICATIONS)
- [SCHEDULE_EXACT_ALARM](https://developer.android.com/reference/android/Manifest.permission#SCHEDULE_EXACT_ALARM)
- [REQUEST_IGNORE_BATTERY_OPTIMIZATIONS](https://developer.android.com/reference/android/Manifest.permission#REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
- [SYSTEM_ALERT_WINDOW](https://developer.android.com/reference/android/Manifest.permission#SYSTEM_ALERT_WINDOW)

---

## Exact Alarm:

- [Services & Alarms Overview](https://developer.android.com/develop/background-work/services/alarms)
- [AlarmManager Reference](https://developer.android.com/reference/android/app/AlarmManager)
- [Behavior Changes in Android 12 (Exact Alarm Permission)](https://developer.android.com/about/versions/12/behavior-changes-12#exact-alarm-permission)
- [USE_EXACT_ALARM](https://developer.android.com/reference/android/Manifest.permission#USE_EXACT_ALARM)
- [SCHEDULE_EXACT_ALARM](https://developer.android.com/reference/android/Manifest.permission#SCHEDULE_EXACT_ALARM)

---

## Optimization for Doze & App Standby:

- [Doze and App Standby Overview](https://developer.android.com/training/monitoring-device-state/doze-standby)
- [setExactAndAllowWhileIdle()](https://developer.android.com/reference/android/app/AlarmManager#setExactAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent))
- [Support for Other Use Cases](https://developer.android.com/training/monitoring-device-state/doze-standby#support_for_other_use_cases)

---

## Restrictions on Background Activity Starts:

- [Starting Activities from Background](https://developer.android.com/guide/components/activities/background-starts)

---

## Foreground Services:

- [Foreground Services Overview](https://developer.android.com/develop/background-work/services/fgs)
- [Android 14: Foreground Service Types Required](https://developer.android.com/about/versions/14/changes/fgs-types-required)
- [Short Service Requirements](https://developer.android.com/about/versions/14/changes/fgs-types-required#short-service)
- [`startForeground()` Method](https://developer.android.com/reference/android/app/Service#startForeground(int,%20android.app.Notification))
- [`startForegroundService()` Method](https://developer.android.com/reference/android/content/Context#startForegroundService(android.content.Intent))

---

## Overlay Permissions:

- [Manage Overlay Permission](https://developer.android.com/reference/android/provider/Settings#ACTION_MANAGE_OVERLAY_PERMISSION)
- [Check Overlay Permission](https://developer.android.com/reference/android/provider/Settings#canDrawOverlays(android.content.Context))

---

## System Alert Window:

- [SYSTEM_ALERT_WINDOW Permission](https://developer.android.com/reference/android/Manifest.permission#SYSTEM_ALERT_WINDOW)
- [TYPE_APPLICATION_OVERLAY](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY)
- [Android 15 System Alert Window Changes](https://developer.android.com/about/versions/15/behavior-changes-15#fgs-sysalert)
- [Overlay View App with AlarmManager (Medium)](https://medium.com/@silvalucas52816/android-viewoverlay-app-with-alarmmanager-85dbb9ff629d)
- [Overlay Activity from Service (Medium)](https://medium.com/@kanhaiyayadav7221/how-to-launch-and-manage-an-overlay-like-activity-from-a-service-in-android-1e2a8d59d54e)
- [Draw over other apps (GeekforGeeks)](https://www.geeksforgeeks.org/android/how-to-draw-over-other-apps-in-android/)

---

## LaunchIntent for Package:

- [`getLaunchIntentForPackage`  (Stackoverflow)](https://stackoverflow.com/a/68224776)
- [`getLaunchIntentForPackage`  (Android documentation)](https://developer.android.com/reference/android/content/pm/PackageManager#getLaunchIntentForPackage(java.lang.String))
- [Launch another Android app using an intent (Github)](https://gist.github.com/201949/295f6402537d6da28f8176eb32d43aed)

---

## Boot Complete Broadcast:

- [`ACTION_BOOT_COMPLETED`](https://developer.android.com/reference/android/content/Intent#ACTION_BOOT_COMPLETED)
- [Alarms & Boot Handling](https://developer.android.com/develop/background-work/services/alarms#boot)

---

## PendingIntent:

- [`PendingIntent.getBroadcast()`](https://developer.android.com/reference/android/app/PendingIntent#getBroadcast(android.content.Context,%20int,%20android.content.Intent,%20int))

---

### Summary:

This reference list is designed to help during development and review of Android components dealing with:
- Background execution limits
- Foreground and overlay services
- Permission handling
- Power optimization and alarm scheduling
- Boot completion and PendingIntent management

---


