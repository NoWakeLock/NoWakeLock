# Glossary

Technical terms and concepts related to NoWakeLock.

## Core Concepts

### WakeLock (Wake Lock)
Mechanism to prevent Android devices from entering sleep state. Apps hold WakeLocks to keep CPU running or screen on.

**Types**:
- **PARTIAL_WAKE_LOCK** - Keep CPU running, screen can turn off
- **SCREEN_DIM_WAKE_LOCK** - Keep screen on but allow dimming
- **SCREEN_BRIGHT_WAKE_LOCK** - Keep screen fully bright
- **FULL_WAKE_LOCK** - Keep both CPU and screen running

### Alarm (Scheduled Tasks)
Android system timer service that allows apps to execute tasks at specific times or intervals.

**Types**:
- **RTC** - Real-time clock based timer
- **RTC_WAKEUP** - Real-time based timer that wakes device
- **ELAPSED_REALTIME** - Timer based on device uptime
- **ELAPSED_REALTIME_WAKEUP** - Uptime based timer that wakes device

### Service (Services)
Android app components that run in background without providing user interface.

**Types**:
- **Foreground Service** - Performs user-noticeable tasks, shows persistent notification
- **Background Service** - Performs tasks not directly noticeable to user
- **Bound Service** - Provides client-server interface

## Android System

### Doze Mode
Power-saving mechanism introduced in Android 6.0+, device enters deep sleep when stationary.

### App Standby
System power-saving restrictions on apps that haven't been used for extended periods.

### Background Execution Limits
Android 8.0+ restrictions on background services and broadcast receivers.

### SELinux (Security-Enhanced Linux)
Mandatory access control security mechanism in Android system.

## Xposed Framework

### Xposed Framework
Framework that allows modifying system and app behavior without modifying APKs.

### Hook
Technique for intercepting and modifying function calls.

### LSPosed
Modern Xposed implementation based on Riru, supports Android 8.1+.

### EdXposed
Xposed implementation based on YAHFA and SandHook.

### Zygote
Parent process of all app processes in Android system.

## NoWakeLock Terms

### Interception Modes
- **Allow** - No restrictions, normal operation
- **Restrict** - Set time or frequency limits
- **Intercept** - Completely block operation

### Rule System
Configuration mechanism based on pattern matching, supports regular expressions.

### Components
Collective term for WakeLocks, Alarms, and Services.

### Scope
Application range where Xposed module takes effect.

### DA
Abbreviation for Detection/Action, refers to WakeLock, Alarm, Service activities detected by NoWakeLock.

## Performance Metrics

### Acquisition Count
Total number of times WakeLock was acquired.

### Cumulative Duration
Total time WakeLock was held.

### Trigger Frequency
Average trigger interval of Alarms.

### Start Count
Total number of times Service was started.

### Interception Rate
Percentage of intercepted operations out of total operations.

## Technical Terms

### API Level
API level number corresponding to Android version.

### Package Name
Unique identifier for apps, e.g., `com.example.app`.

### UID (User Identifier)
Unique numeric identifier assigned by system to each app.

### PID (Process Identifier)
Unique numeric identifier assigned by system to each process.

### ContentProvider
One of Android's four major components, used for cross-app data sharing.

### IPC (Inter-Process Communication)
Data exchange mechanism between different processes.

### JNI (Java Native Interface)
Interface for Java code to call native C/C++ code.

## Database Terms

### Room
Google's official SQLite abstraction layer framework.

### DAO (Data Access Object)
Interface that encapsulates database operations.

### Entity
Object mapping of database tables.

### Migration
Handling mechanism for database version upgrades.

## Development Terms

### Kotlin
Modern JVM programming language, preferred language for Android development.

### Jetpack Compose
Android's modern declarative UI toolkit.

### Coroutines
Kotlin's asynchronous programming mechanism.

### Flow
Kotlin's reactive data stream framework.

### ViewModel
Android architecture component that manages UI-related data.

### LiveData
Observable data holder class with lifecycle awareness.

### Koin
Lightweight dependency injection framework.

## Regular Expressions

### Metacharacters
Characters with special meaning, such as `.`, `*`, `+`, `?`, etc.

### Character Classes
Character sets enclosed in square brackets, such as `[abc]`.

### Quantifiers
Symbols specifying match count, such as `{n}`, `{n,m}`, etc.

### Groups
Subexpressions created with parentheses, such as `(abc)+`.

### Anchors
Symbols specifying match position, such as `^` (start), `$` (end).

## Configuration Terms

### Inheritance
Mechanism where child configurations automatically get settings from parent.

### Priority
Execution order when multiple rules conflict.

### Template
Preset configuration combinations that can be reused.

### Whitelist
List of apps or components not subject to rule restrictions.

### Blacklist
List of apps or components strictly restricted or intercepted.

## System Services

### PowerManagerService
System service managing device power states.

### AlarmManagerService
Service managing system scheduled tasks.

### ActivityManagerService
Service managing app lifecycles.

### PackageManagerService
Service managing app installation and permissions.

### WindowManagerService
Service managing window display.

## Permission Related

### QUERY_ALL_PACKAGES
Permission to query all installed apps (Android 11+).

### WAKE_LOCK
Permission to acquire WakeLocks.

### RECEIVE_BOOT_COMPLETED
Permission to receive boot broadcast.

### WRITE_EXTERNAL_STORAGE
Permission to write to external storage.

## Debug Terms

### ADB (Android Debug Bridge)
Command-line tool connecting development machine and Android device.

### Logcat
Android system log viewing tool.

### ANR (Application Not Responding)
Error when app main thread blocks for more than 5 seconds.

### Crash
Error when app terminates abnormally.

### Memory Leak
Problem where memory cannot be properly released during program execution.

## Performance Terms

### CPU Usage
Percentage of processor utilization.

### Memory Usage
Amount of RAM used by app.

### Battery Consumption
App's battery usage.

### Network Traffic
App's data transmission amount.

### Storage I/O
App's storage read/write activity.

!!! info "Term Updates"
    As the project develops, new terms will be continuously added to this list. If you have questions, please refer to relevant documentation or contact the community.

!!! tip "Learning Suggestions"
    Recommend new users first familiarize themselves with core concepts (WakeLock, Alarm, Service), then gradually understand technical details.