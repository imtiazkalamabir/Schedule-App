# Architecture Documentation

## Overview

This Android application follows **Clean Architecture with MVVM** design pattern, organized into four distinct layers.

---

## 📐 Architecture Layers

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

---

## 🎯 Layer Responsibilities

### 1. Presentation Layer (`presentation/`)
**Purpose:** Handle UI logic and user interaction

**Components:**
- `ScheduleListActivity` - Main UI controller
- `ScheduleViewModel` - UI state management
- `ScheduleListAdapter` - RecyclerView adapter
- `AppListAdapter` - App selection adapter

**Dependencies:** 
- ✅ Domain layer only (Use Cases)
- ❌ Never depends on Data or Framework layers

**Example:**
```kotlin
class ScheduleViewModel @Inject constructor(
    private val addAppScheduleUseCase: AddAppScheduleUseCase,  // Domain
    private val getAllAppSchedulesUseCase: GetAllAppSchedulesUseCase  // Domain
) : ViewModel()
```

---

### 2. Domain Layer (`domain/`)
**Purpose:** Core business logic, independent of Android framework

**Components:**
- **Models** (`model/`): Business entities
  - `AppSchedule` - Schedule domain model
  - `InstalledApp` - App information model
  
- **Use Cases** (`usecase/`): Business operations
  - `AddAppScheduleUseCase` - Add new schedule with validation
  - `UpdateAppScheduleUseCase` - Update existing schedule
  - `CancelAppScheduleUseCase` - Cancel pending schedule
  - `DeleteAppScheduleUseCase` - Delete executed schedule
  - `GetAllAppSchedulesUseCase` - Retrieve all schedules
  
- **Repositories** (`repository/`): Data access abstractions
  - `ScheduleRepository` (interface)
  - `AppRepository` (interface)
  
- **Manager** (`manager/`): Scheduling abstraction
  - `AppScheduleManager` (interface)

**Dependencies:** 
- ❌ **NO dependencies on any other layer**
- ✅ Pure Kotlin/Java code (framework-agnostic)

---

### 3. Data Layer (`data/`)
**Purpose:** Manage data sources and implement domain interfaces

**Components:**
- **Local Database** (`local/`):
  - `ScheduleAppDatabase` - Room database
  - `ScheduleDao` - Data access object
  - `ScheduleEntity` - Database entity
  
- **Repositories** (`repository/`):
  - `ScheduleRepositoryImpl` - Implements `ScheduleRepository`
  - `AppRepositoryImpl` - Implements `AppRepository`
  
- **Manager** (`manager/`):
  - `AppScheduleManagerImpl` - Implements `AppScheduleManager` using AlarmManager

**Dependencies:** 
- ✅ Domain layer (implements interfaces)
- ✅ Framework layer (for coordination)
- ❌ Never depends on Presentation

**Architectural Note on AppScheduleManagerImpl:**
While `AlarmManager` is an Android framework service, `AppScheduleManagerImpl` resides in the data layer following the standard pattern where interface implementations are co-located with other data sources. This is pragmatic and widely accepted in the Android community.

---

### 4. Framework Layer (`framework/`)
**Purpose:** Android system components that serve as entry points

**Components:**
- **Receivers** (`receiver/`):
  - `AppLaunchReceiver` - Handles alarm triggers
  - `BootReceiver` - Reschedules alarms after device boot
  
- **Services** (`service/`):
  - `OverlayLauncherService` - Launches apps via overlay window

**Dependencies:** 
- ✅ Domain layer (repositories, manager)
- ❌ Uses Repository directly (NOT Use Cases)
- ❌ Never depends on Presentation or Data

**Key Design Decision:**
Framework components use `ScheduleRepository` directly for status updates instead of going through Use Cases, as they're framework-level operations, not application-level business logic.

---

## 🔄 Data Flow

### User Action Flow:
```
User Interaction
    ↓
ScheduleListActivity
    ↓
ScheduleViewModel
    ↓
Use Case (with business logic)
    ↓
Repository (interface - Domain)
    ↓
Repository Implementation (Data)
    ↓
Room DAO / AlarmManager
```

### System Trigger Flow:
```
AlarmManager Trigger
    ↓
AppLaunchReceiver (Framework)
    ↓
Repository (interface - Domain)
    ↓
Repository Implementation (Data)
    ↓
Room DAO (update status)
    ↓
Flow emits to ViewModel
    ↓
UI updates automatically
```

---

## 📦 Dependency Injection (Hilt)

### Module Structure:

**`DatabaseModule`** - Provides Room database and DAOs
```kotlin
@Provides
fun provideScheduleAppDatabase(context: Context): ScheduleAppDatabase
```

**`RepositoryModule`** - Binds repository implementations
```kotlin
@Binds
fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository
```

**`ManagerModule`** - Binds manager implementation
```kotlin
@Binds
fun bindAppScheduleManager(impl: AppScheduleManagerImpl): AppScheduleManager
```

---

## ✅ Architecture Compliance Checklist

| Rule | Status |
|------|--------|
| Presentation only depends on Domain | ✅ |
| Domain has NO dependencies | ✅ |
| Data implements Domain interfaces | ✅ |
| Framework uses Domain abstractions | ✅ |
| Use Cases contain business logic | ✅ |
| Repositories abstract data sources | ✅ |
| ViewModels don't reference Android framework | ✅ |
| Framework components don't call Use Cases | ✅ |

---

## 🎓 Benefits of This Architecture

### Testability
- **Domain layer**: Pure Kotlin, easy to unit test
- **ViewModels**: Test with mocked use cases
- **Repositories**: Test with mocked DAOs

### Maintainability
- Clear separation of concerns
- Each layer has single responsibility
- Easy to locate and fix bugs

### Flexibility
- Can swap Room for another database
- Can replace AlarmManager with WorkManager
- UI changes don't affect business logic

### Scalability
- Easy to add new features
- Multiple developers can work on different layers
- Clear contracts via interfaces

---

## 📚 References

This architecture follows industry best practices from:

1. [Android Guide to App Architecture](https://developer.android.com/topic/architecture)
2. [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
3. [Google's architecture-samples](https://github.com/android/architecture-samples)
4. [NowInAndroid](https://github.com/android/nowinandroid) - Official Google sample

---

## 🚀 Future Improvements

Potential enhancements while maintaining clean architecture:

1. **Add Use Case Tests** - Unit tests for all use cases
2. **Repository Tests** - Test with fake DAOs
3. **ViewModel Tests** - Test with fake use cases
4. **Modularization** - Split into feature modules
5. **Kotlin Multiplatform** - Share business logic across platforms

---






