# Boot Detection and Database Reset Feature

## Overview
This feature ensures that the `info` and `info_event` database tables are reset after a device restart, when the app is first opened. The reset operation will occur only once per boot cycle, avoiding unnecessary table resets during subsequent app launches.

## Core Components

### BootResetManager
- Detects device restarts using SystemClock.elapsedRealtime()
- Resets the database tables when needed
- Updates preferences to track reset status

### XposedModule Boot Detection
- Implements a multi-layer boot detection mechanism in `XposedModule.kt`
- Uses a dedicated `hookBootCompletedMethods` method for better code organization
- Attempts to hook three different system methods to ensure reliable boot detection:
  1. `KeyguardServiceDelegate.onBootCompleted`
  2. `ActivityManagerService.finishBooting`
  3. `WindowManagerService.systemReady`

### UserPreferencesRepository Extensions
- Stores boot-related preferences using DataStore
- Manages lastBootTime and resetDoneForCurrentBoot flags

### Integration in BasicApp
- Initializes the BootResetManager after Koin setup
- Ensures tables are reset early in the app's lifecycle

## Implementation Details

### Boot Detection Logic
```kotlin
// Device has been restarted if currentBootTime is less than lastRecordedTime
// or if this is the first run (lastRecordedTime = 0)
val isAfterReboot = currentBootTime < lastRecordedTime || lastRecordedTime == 0L
```

This approach works because:
1. `SystemClock.elapsedRealtime()` returns milliseconds since boot
2. When a device restarts, this counter resets to zero and starts incrementing
3. If the current value is less than the previously stored value, it indicates a reboot has occurred

### XposedModule Boot Detection Implementation
```kotlin
// In XposedModule.kt
private fun hookBootCompletedMethods(lpparam: LoadPackageParam) {
    try {
        // First attempt: KeyguardServiceDelegate.onBootCompleted
        XposedHelpers.findAndHookMethod(
            "com.android.server.policy.keyguard.KeyguardServiceDelegate",
            lpparam.classLoader,
            "onBootCompleted",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    WakelockHook.booted = true
                    ServiceHook.booted = true
                    AlarmHook.booted = true
                }
            })
    } catch (e: Exception) {
        XpUtil.log("${e.message}")
        XpUtil.log("${e.stackTrace}")

        try {
            // Second attempt: ActivityManagerService.finishBooting
            XposedHelpers.findAndHookMethod(
                "com.android.server.am.ActivityManagerService",
                lpparam.classLoader,
                "finishBooting",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        WakelockHook.booted = true
                        ServiceHook.booted = true
                        AlarmHook.booted = true
                    }
                })
        } catch (e: Exception) {
            XpUtil.log("${e.message}")
            XpUtil.log("${e.stackTrace}")

            try {
                // Third attempt: WindowManagerService.systemReady
                XposedHelpers.findAndHookMethod(
                    "com.android.server.wm.WindowManagerService",
                    lpparam.classLoader,
                    "systemReady",
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            WakelockHook.booted = true
                            ServiceHook.booted = true
                            AlarmHook.booted = true
                        }
                    })
            } catch (e: Exception) {
                XpUtil.log("${e.message}")
                XpUtil.log("${e.stackTrace}")
            }
        }
    }
}
```

The boot detection implementation:
1. Attempts to hook `KeyguardServiceDelegate.onBootCompleted` first, which is called when the keyguard is ready after boot
2. If that fails, tries `ActivityManagerService.finishBooting`, which is called when the core system services are initialized
3. As a last resort, hooks `WindowManagerService.systemReady`, which indicates the window manager is ready to display UIs
4. Sets the booted flags in all three core hook systems (WakelockHook, ServiceHook, AlarmHook) when any of these methods are called
5. Provides comprehensive error logging to help diagnose issues on different device models

This multi-layered approach ensures boot detection works reliably across different Android versions and manufacturer customizations.

### Shizuku Rootless Boot Detection
When running in Shizuku mode, `XposedModule` hooks are not available. Instead, the application relies on the system broadcasting the standard `ACTION_BOOT_COMPLETED` intent or starting the `ShizukuMonitorService` once the Shizuku manager application starts and re-grants permissions. Shizuku's own polling mechanism assumes the device is booted if Shizuku is accessible.

### Single Reset Guarantee
```kotlin
// Only reset if after reboot and reset not done for this boot cycle
if (isAfterReboot || !resetDone) {
    // Reset tables
    // ...
    
    // Mark as done for this boot cycle
    userPreferencesRepository.setResetDone(true)
}
```

This ensures that the reset operation happens exactly once after each device restart.

### Error Handling
```kotlin
try {
    // Reset logic
} catch (e: Exception) {
    Log.e(TAG, "Error during boot reset check: " + e.message, e)
    // Show error toast
    showErrorToast("Failed to reset database after restart")
    return false
}
```

Proper error handling ensures that:
1. Exceptions are logged for debugging
2. User is notified of any issues via toast message
3. App continues to function even if reset fails

## Technical Considerations

### Synchronous Execution
The reset operation runs synchronously during app initialization to ensure it completes before the UI is shown. This is acceptable because:
- The operation is fast (just clearing two tables)
- It only happens once per device restart
- It's critical for proper app functioning

### Preference Storage with DataStore
```kotlin
// In UserPreferencesRepository
val LAST_BOOT_TIME_KEY = longPreferencesKey("last_boot_time")
val RESET_DONE_KEY = booleanPreferencesKey("reset_done_for_current_boot")

val lastBootTime: Flow<Long> = context.dataStore.data
    .map { preferences -> preferences[LAST_BOOT_TIME_KEY] ?: 0L }

val resetDoneForCurrentBoot: Flow<Boolean> = context.dataStore.data
    .map { preferences -> preferences[RESET_DONE_KEY] ?: false }
```

DataStore provides a modern, type-safe way to store preferences with Kotlin coroutines support.

### Integration with Dependency Injection
```kotlin
// In BasicApp.onCreate()
val userPreferencesRepository: UserPreferencesRepository by inject(UserPreferencesRepository::class.java)
val bootResetManager = BootResetManager(context, userPreferencesRepository)
```

Koin is used for dependency injection, ensuring that components are properly initialized before use.

## Testing Considerations

### Test Scenarios
1. First app installation
2. Normal app launch (no device restart)
3. App launch after device restart
4. Error handling when database access fails

### Manual Testing
- Restart device and verify that tables are cleared on first app launch
- Check logs to confirm proper operation
- Verify that subsequent launches don't clear the tables again

## Future Improvements

### Potential Enhancements
1. Add unit tests for the boot detection logic
2. Consider async execution with UI blocking if performance becomes an issue
3. Expand to support selective table clearing based on configuration 
4. Add additional boot detection methods for Samsung and other manufacturer-specific Android versions
5. Implement a fallback mechanism if all boot detection methods fail 