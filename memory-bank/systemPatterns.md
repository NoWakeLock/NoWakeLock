# σ₂: System Patterns
*v1.0 | Created: 2025-04-13 | Updated: 2025-04-13*
*Π: INITIALIZING | Ω: PLAN*

## 🏛️ Architecture Overview
NoWakeLock follows a modular architecture with clear separation between the Xposed module functionality and the user-facing application. The application uses an MVVM pattern with Repository pattern for data management.

## 🧩 Core Components

### 1. Xposed Module Layer
- **XposedModule**: Entry point for the Xposed framework integration
- **Hook Implementations**:
  - WakelockHook: Intercepts and manages Android wakelock operations
  - AlarmHook: Intercepts and manages Android alarm operations
  - ServiceHook: Intercepts and manages Android service operations
  - SettingsProviderHook: Manages settings communication

### 2. Data Layer
- **Repository Pattern**: Abstracts data sources from the rest of the application
- **Room Database**: Manages persistent storage for settings and event logs
- **Shared Preferences**: Handles lightweight configuration settings via XpNSP model

### 3. UI Layer
- **MVVM Architecture**: ViewModels mediate between UI and data layers
- **Fragment-Based UI**: Modular UI components organized in fragments
- **Dependency Injection**: Koin is used for dependency management

## 📱 Module Interactions

```
┌─────────────────┐       ┌────────────────┐       ┌───────────────┐
│                 │       │                │       │               │
│  User Interface │◄─────►│  View Models   │◄─────►│  Repositories │
│                 │       │                │       │               │
└─────────────────┘       └────────────────┘       └───────┬───────┘
                                                           │
                                                           ▼
┌─────────────────┐       ┌────────────────┐       ┌───────────────┐
│                 │       │                │       │               │
│  Android System │◄─────►│ Xposed Hooks   │◄─────►│  Databases    │
│                 │       │                │       │               │
└─────────────────┘       └────────────────┘       └───────────────┘
```

## 🔄 Data Flow

1. **System Event Flow**:
   - Android system generates wakelock/alarm/service events
   - Xposed hooks intercept these events
   - Events are processed based on user settings
   - Events either proceed or are blocked
   - Event data is recorded in the database

2. **User Interaction Flow**:
   - User configures settings via UI
   - ViewModels process and validate settings
   - Repositories store settings in database
   - Xposed hooks query settings during runtime
   - User views event logs and statistics

## 🧠 Design Decisions

### Multi-layered Blocking Strategy
- Flag-based blocking (complete block)
- Lock screen detection (block during screen off)
- Time interval-based blocking (regulate frequency)
- Regular expression pattern matching (flexible targeting)

### Version-specific Implementation
- Different hook strategies for Android versions 7-11 vs Android 12+
- Conditional code paths based on API level detection

### Error Handling Strategy
- Try-catch blocks around hook functions
- Logging mechanism for debugging
- Fallback mechanisms when expected methods aren't found

## 🔌 Integration Points

- **Xposed Framework**: Hooks into Android system processes
- **Android Power Management**: Controls wakelock operations
- **AlarmManager**: Controls system alarm scheduling
- **ActivityManager**: Controls service operations
- **Settings Provider**: Monitors system settings changes 