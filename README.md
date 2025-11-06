# Schedule App

A professional Android application that allows users to schedule app launches at specific times using **MVVM Clean Architecture with Framework Layer**.

## 🎯 Features

### Core Functionality
✅ **Schedule App Launches** - Schedule any installed Android app to start at a specific time  
✅ **Background Launch Support** - Launches apps even when screen is locked or app is in background (Android 10+)  
✅ **Edit Schedules** - Modify the scheduled time for pending schedules  
✅ **Cancel Schedules** - Cancel pending schedules before they execute  
✅ **Delete History** - Remove executed, cancelled, or failed schedules  
✅ **Conflict Prevention** - Prevents multiple schedules at the same time (1-minute window)  
✅ **Schedule History** - View all schedules including executed, cancelled, and failed ones  
✅ **Boot Persistence** - Reschedules all pending alarms after device reboot  
✅ **Failure Handling** - Marks missed schedules as failed with notifications  

### Advanced Features
✅ **Overlay Window Launch** - Uses SYSTEM_ALERT_WINDOW for background launches on Android 10+  
✅ **Foreground Service** - Reliable background operation with proper notification  
✅ **Record Notifications** - Non-clickable notifications for tracking launch history  
✅ **Battery Optimization** - Requests exclusion from Doze mode for reliability  
✅ **Smart Scrolling** - Auto-scroll to new/edited schedules with conflict handling  
✅ **Modern UI** - Clean, user-friendly Material Design interface with empty states  

## 🏗️ Architecture

The project follows **Clean Architecture with MVVM** pattern, organized into **4 distinct layers**:

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│               (UI & ViewModels)             │
└──────────────────────┬──────────────────────┘
                       ↓
┌─────────────────────────────────────────────┐
│               Domain Layer                  │
│   (Business Logic, Use Cases, Interfaces)   │
└──────────────┬───────────────┬──────────────┘
               ↑               ↑
       ┌───────┴───────────────┴────────┐
       │                                │
┌──────▼─────────┐  ┌───────────────────▼──┐
│   Data Layer   │  │  Framework Layer     │
│   (Data        │  │  (Android System     │
│   Sources)     │  │   Components)        │
└────────────────┘  └──────────────────────┘
```

### Package Structure

```
com.challenge.scheduleapp/
├── framework/              # Framework Layer (NEW!)
│   └── scheduler/
│       ├── AppLaunchReceiver.kt       # Handles alarm triggers
│       ├── BootReceiver.kt            # Reschedules after boot
│       └── OverlayLauncherService.kt  # Launches via overlay
│
├── presentation/           # Presentation Layer
│   ├── ui/
│   │   └── ScheduleListActivity.kt
│   ├── viewmodel/
│   │   └── ScheduleViewModel.kt
│   ├── adapter/
│   │   ├── ScheduleListAdapter.kt
│   │   └── AppListAdapter.kt
│   └── model/
│       └── ScheduleListUiState.kt
│
├── domain/                 # Domain Layer (Core)
│   ├── model/
│   │   ├── AppSchedule.kt
│   │   |── InstalledApp.kt
│   │   |── ScheduleStatus.kt
│   │   |── ProcessResult.kt
│   │   |── InvalidTimeException.kt
│   │   |── TimeConflictException.kt
│   ├── repository/         # Abstractions
│   │   ├── ScheduleRepository.kt
│   │   └── AppRepository.kt
│   ├── manager/
│   │   └── AppScheduleManager.kt
│   └── usecase/            # Business Logic
│       ├── AddAppScheduleUseCase.kt
│       ├── UpdateAppScheduleUseCase.kt
│       ├── CancelAppScheduleUseCase.kt
│       ├── DeleteAppScheduleUseCase.kt
│       ├── GetAllAppSchedulesUseCase.kt
│       └── GetInstalledAppsUseCase.kt
│ 
└── data/                   # Data Layer
    ├── local/
    │   ├── dao/
    │   │   └── ScheduleDao.kt
    │   ├── database/
    │   │   └── ScheduleAppDatabase.kt
    │   └── entity/
    │       └── ScheduleEntity.kt
    ├── repository/         # Implementations
    │   ├── ScheduleRepositoryImpl.kt
    │   └── AppRepositoryImpl.kt
    └── scheduler/
        └── AppScheduleManagerImpl.kt
```

See **[ARCHITECTURE.md](ARCHITECTURE.md)** for detailed architecture documentation.

## 🛠️ Technologies Used

### Core Technologies
- **Kotlin** - 100% Kotlin codebase
- **MVVM Architecture** - Model-View-ViewModel pattern
- **Clean Architecture** - 4-layer architecture with framework separation
- **Hilt** - Dependency injection
- **Room Database** - Local data persistence with Flow
- **Coroutines & Flow** - Asynchronous programming and reactive streams
- **LiveData** - Lifecycle-aware data holder

### Android Components
- **AlarmManager** - Exact alarm scheduling with `setExactAndAllowWhileIdle()`
- **BroadcastReceiver** - Alarm triggers and boot completion
- **Foreground Service** - Reliable background operation
- **SYSTEM_ALERT_WINDOW** - Overlay window for background launches
- **NotificationManager** - High-priority notifications with sound and vibration

### Testing & Quality
- **JUnit 4** - Unit testing framework
- **Mockito** - Mocking framework

### UI/UX
- **Material Design 3** - Modern UI components
- **RecyclerView** - Efficient list rendering with DiffUtil
- **ViewBinding** - Type-safe view access
- **TimePickerDialog** - Native time selection

## 📦 Key Features Implementation

### 1. Background App Launching (Android 10+)

Uses overlay window to bypass background activity launch restrictions:

```kotlin
// Framework Layer: OverlayLauncherService
private fun launchAppFromOverlay(packageName: String, appName: String, scheduleId: Long) {
    // Create 1x1 transparent overlay
    val overlayView = FrameLayout(this)
    val layoutParams = WindowManager.LayoutParams(
        1, 1,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )
    windowManager.addView(overlayView, layoutParams)
    
    // Launch from overlay context
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
    startActivity(launchIntent)
    
    // Update status via repository
    scheduleRepository.markAsExecuted(scheduleId)
}
```

### 2. Exact Alarm Scheduling

Uses AlarmManager for precise scheduling with battery optimization:

```kotlin
// Data Layer: AppScheduleManagerImpl
override fun scheduleApp(scheduleId: Long, packageName: String, timeInMillis: Long) {
    if (alarmManager.canScheduleExactAlarms()) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }
}
```

### 3. Boot Persistence

Reschedules alarms after device reboot:

```kotlin
// Framework Layer: BootReceiver
override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
        val schedules = scheduleRepository.getAllSchedules().first()
        
        // Reschedule future alarms
        futureSchedules.forEach { schedule ->
            appScheduleManager.scheduleApp(schedule.id, schedule.packageName, schedule.scheduledTime)
        }
        
        // Mark missed schedules as failed
        missedSchedules.forEach { schedule ->
            scheduleRepository.markAsFailed(schedule.id)
        }
    }
}
```

### 4. Time Conflict Prevention

Prevents scheduling within 1-minute window:

```kotlin
// Domain Layer: AddAppScheduleUseCase
override suspend fun hasTimeConflict(scheduledTime: Long, excludeId: Long?): Boolean {
    val timeWindow = 60000L // 1 minute
    val startTime = scheduledTime - timeWindow
    val endTime = scheduledTime + timeWindow
    return countSchedulesInTimeRange(startTime, endTime, excludeId) > 0
}
```

### 5. Reactive UI Updates

Uses Flow for automatic UI updates:

```kotlin
// Presentation Layer: ScheduleViewModel
init {
    viewModelScope.launch {
        getAllAppSchedulesUseCase()
            .debounce(300)
            .distinctUntilChanged()
            .collect { schedules ->
                _uiState.postValue(ScheduleUiState(schedules = schedules))
            }
    }
}
```

## 📱 Database Schema

### Schedules Table
```sql
CREATE TABLE schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    packageName TEXT NOT NULL,
    appName TEXT NOT NULL,
    scheduledTime INTEGER NOT NULL,
    status TEXT NOT NULL,        -- PENDING, EXECUTED, CANCELLED, FAILED
    createdAt INTEGER NOT NULL
);
```

### Schedule Status Enum
- `PENDING` 🟠 - Schedule is waiting to execute
- `EXECUTED` 🟢 - Schedule has been executed successfully
- `CANCELLED` 🔵 - Schedule was cancelled by user
- `FAILED` 🔴 - Schedule failed to execute (device off, app uninstalled, etc.)

## 🔑 Permissions Required

### Runtime Permissions
- `POST_NOTIFICATIONS` - Show notifications (Android 13+)
- `SCHEDULE_EXACT_ALARM` - Schedule precise alarms (Android 12+)
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - Exclude from Doze mode
- `SYSTEM_ALERT_WINDOW` - Draw overlay window for background launches

### Manifest Permissions
- `USE_EXACT_ALARM` - Use exact alarm API
- `WAKE_LOCK` - Wake device for alarm execution
- `RECEIVE_BOOT_COMPLETED` - Receive boot completion events
- `QUERY_ALL_PACKAGES` - Query all installed apps
- `VIBRATE` - Vibrate for notifications
- `FOREGROUND_SERVICE` - Run foreground service
- `USE_FULL_SCREEN_INTENT` - Full-screen intent capability

## 🚀 Building the Project

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or higher
- Android SDK with API 36

### Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run the app on a device or emulator (API 24+)

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## 📖 Usage Guide

### 1. Add Schedule
1. Tap the floating action button (+)
2. Select an app from the list (search available)
3. Choose date and time
4. Tap "OK" to create schedule
5. Grant required permissions if prompted

### 2. Edit Schedule
1. Tap "Edit" button on a **PENDING** schedule
2. Select new date and time
3. Confirm to update
4. List auto-scrolls to edited schedule

### 3. Cancel Schedule
1. Tap "Cancel" button on a **PENDING** schedule
2. Confirm cancellation in dialog
3. Status changes to **CANCELLED**

### 4. Delete Schedule
1. Tap "Delete" button on **EXECUTED**, **CANCELLED**, or **FAILED** schedules
2. Confirm deletion in dialog
3. Schedule is permanently removed

### 5. View History
- All schedules are visible in a single list
- Auto-sorted by scheduled time (newest first)
- Status is color-coded for easy identification
- Empty state shown when no schedules exist

## 🎨 Status Colors

| Status | Color | Icon | Description |
|--------|-------|------|-------------|
| **PENDING** | 🟠 Orange | ⏱️ | Scheduled but not executed |
| **EXECUTED** | 🟢 Green | ✅ | Successfully launched |
| **CANCELLED** | 🔵 Blue | ❌ | Cancelled by user |
| **FAILED** | 🔴 Red | ⚠️ | Failed to execute |

## 🧪 Testing

The project includes comprehensive unit tests:

### Test Coverage
- ✅ Use Case tests (business logic)
- ✅ ViewModel tests (UI state management)
- ✅ Repository tests (data operations)
- ✅ Mocked dependencies using Mockito

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "UpdateAppScheduleUseCaseTest"

# Generate test coverage report
./gradlew jacocoTestReport
```

## 🔧 Configuration

### Gradle Version Catalog
Dependencies are managed in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.0.21"
hilt = "2.52"
room = "2.6.1"
coroutines = "1.9.0"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android" }
room-ktx = { group = "androidx.room", name = "room-ktx" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android" }
```

## 🔮 Future Enhancements

- [ ] Recurring schedules (daily, weekly, monthly)
- [ ] Schedule categories and tags
- [ ] Widget support for quick access
- [ ] Export/Import schedules (JSON/CSV)
- [ ] Schedule templates
- [ ] Dark theme support
- [ ] Multi-language support
- [ ] Cloud backup integration
- [ ] Schedule sharing
- [ ] Analytics dashboard

## 📋 Requirements

- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Compile SDK**: API 36
- **Kotlin Version**: 2.0.21
- **Gradle Version**: 8.9

## 🏆 Architecture Highlights

### Clean Architecture Benefits
✅ **Testability** - Each layer can be tested independently  
✅ **Maintainability** - Clear separation of concerns  
✅ **Scalability** - Easy to add new features  
✅ **Flexibility** - Swap implementations without affecting other layers  
✅ **SOLID Principles** - Follows all 5 principles  

### Framework Layer (NEW!)
- **Purpose**: Isolates Android system components
- **Components**: BroadcastReceivers, Services
- **Benefit**: Keeps Data layer focused on data sources only
- **Pattern**: Follows Google's architecture-samples approach

See **[ARCHITECTURE.md](ARCHITECTURE.md)** for complete architectural documentation.

## 📚 References

This project follows best practices from:

1. [Android Guide to App Architecture](https://developer.android.com/topic/architecture)
2. [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
3. [Google's architecture-samples](https://github.com/android/architecture-samples)
4. [NowInAndroid](https://github.com/android/nowinandroid) - Official Google sample
5. [Android Developers - Foreground Services](https://developer.android.com/develop/background-work/services/foreground-services)

## 📄 License

This project is for educational and demonstration purposes.
