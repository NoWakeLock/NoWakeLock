# WakeLock Management

WakeLocks prevent devices from entering sleep mode and are a key mechanism in the Android system that affects battery life.

!!! danger "⚠️ Important: Emergency Recovery Mode"
    **If your device becomes stuck, bootloops, or experiences system anomalies after WakeLock configuration**:
    
    **Situation 1: LSPosed Framework Issues (problems after installation before configuration)**:
    1. Long press power button for 10 seconds to force restart
    2. When screen goes black, immediately press any hardware button repeatedly
    3. After feeling 2 short vibrations, continue pressing the same button 4 times quickly
    4. After the 4th press, feel for a long vibration indicating LSPosed is disabled
    5. After normal boot, disable NoWakeLock module in LSPosed
    
    **Situation 2: Configuration Issues (can enter Recovery)**:
    1. Enter Recovery → File Manager
    2. Navigate to /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock
       (xxx-xxx-xxx is a long random string that may be different for each device)
    3. Delete the entire folder
    4. Restart device
    
    **When uncertain about the cause**: Directly clear NoWakeLock app data, and avoid blocking critical system components when reconfiguring.

## WakeLock Types

### Main Types

| Type | Constant Value | Description | Battery Impact |
|------|----------------|-------------|----------------|
| PARTIAL_WAKE_LOCK | 1 | Keep CPU running | High |
| SCREEN_DIM_WAKE_LOCK | 6 | Screen dims but doesn't turn off (deprecated) | Medium |
| SCREEN_BRIGHT_WAKE_LOCK | 10 | Keep screen brightness (deprecated) | High |
| FULL_WAKE_LOCK | 26 | CPU + full screen brightness (deprecated) | Extremely High |
| PROXIMITY_SCREEN_OFF_WAKE_LOCK | 32 | Proximity sensor control | Low |

!!! warning "Type Notes"
    Except for PARTIAL_WAKE_LOCK and PROXIMITY_SCREEN_OFF_WAKE_LOCK, other types have been deprecated since Android API 17. Modern apps mainly use PARTIAL_WAKE_LOCK.

### Special Identifiers

**System WakeLocks**:
- `PowerManagerService.WakeLocks`
- `AlarmManager`
- `AudioMix`

**Network Related**:
- `WifiManager`
- `ConnectivityService`

**Location Services**:
- `LocationManagerService`
- `GpsLocationProvider`

## Interface Description

### WakeLock List

【Screenshot needed: WakeLock list page】

**List Information**:
- **Name** - WakeLock identifier
- **App** - Source package name
- **Type** - WakeLock type icon
- **Status** - Current status indicator
- **Statistics** - Acquisition count and cumulative duration

### Status Indicators

| Status | Icon | Description |
|--------|------|-------------|
| Allow | 🟢 | Normal operation, no restrictions |
| Limit | 🟡 | Timeout time set |
| Block | 🔴 | Completely prevent acquisition |
| Active | ⚡ | Currently being held |

### Filtering and Sorting

**Filter Options**:
- All
- Allow
- Limit
- Block
- Currently Active

**Sort Methods**:
- By name
- By app
- By acquisition count
- By cumulative duration
- By last activity time

## Configuration Options

### Processing Modes

#### Allow Mode
- No restrictions imposed
- WakeLock acquires and releases normally
- Default mode, suitable for most situations

#### Limit Mode
- Set maximum hold time
- Force release after timeout
- Only use when BBS confirms a WakeLock has excessive duration

!!! warning "Timeout Setting Principle"
    Timeout must be determined based on actual BBS analysis data, not preset values. Observe the normal duration of the WakeLock, then set a timeout slightly larger than the normal value.

#### Block Mode
- Completely prevent WakeLock acquisition
- App cannot hold this WakeLock
- Only use when confirmed the WakeLock is completely unnecessary and severely impacts battery

## Usage Methods

### Basic Operation Flow

!!! warning "Important: Must Read Before Configuration"
    1. **Diagnose first, configure later** - Use BBS to confirm problems before configuration
    2. **Individual processing** - Configure for specific problems one by one, avoid batch operations
    3. **Continuous monitoring** - Continue using BBS to verify effects after configuration

### View and Analyze WakeLocks

1. Click "Wakelocks" tab at the bottom
2. Browse current list and statistics
3. Analyze abnormal items combined with BBS data

### Targeted Configuration

1. **Confirm problem** - Based on BBS analysis results
2. **Click target WakeLock** to enter configuration page
3. **Choose minimal intervention** - Prioritize limit mode
4. **Set parameters** - Based on actually observed data
5. **Verify functionality** - Confirm app functions normally

!!! danger "Prohibit Batch Configuration"
    Do not use batch operation features for preset configurations. Each WakeLock problem is specific and needs individual analysis and handling.

## Effect Verification

### Post-Configuration Monitoring

**Required Steps**:
1. **Continue data monitoring** of post-configuration effects
   - Prioritize using BBS for comprehensive evaluation
   - Also use NoWakeLock built-in statistics as reference
2. **Verify app functionality** - Confirm all app functions still work normally
3. **Evaluate battery improvement** - Compare actual battery consumption before and after configuration

**Rollback Preparation**:
- If app functionality is affected, immediately cancel configuration
- If battery improvement is not significant, re-evaluate necessity of restrictions

## Technical Details

### Hook Implementation

NoWakeLock intercepts key methods in PowerManagerService:

```kotlin
// Main Hook methods (parameters vary by Android version)
acquireWakeLockInternal(...)
releaseWakeLockInternal(...)
```

**Version Compatibility Handling**:
- Uses parameter position caching mechanism
- Supports different method signatures for Android 7.0-15.0
- Automatic detection and adaptation of parameter positions

### Compatibility Handling

**Version Adaptation**:
- Supports Android 7.0-15.0
- Dynamic parameter position detection
- Fallback strategy handling

**Performance Optimization**:
- Hook call overhead < 1ms
- Rule matching uses caching
- Asynchronous processing of statistics

### Data Storage

**Real-time Data**:
- Currently active WakeLocks
- Memory cached, cleared after restart

**Session Statistics**:
- Current session WakeLock activity records
- Temporary database storage, cleared after device restart

## Related Features

- [App Management](app-management.md) - View all WakeLocks by app
- [Rules System](rules-regex.md) - Use regex for batch configuration

!!! warning "Usage Recommendation"
    Modifying WakeLocks of critical system services may affect device stability. Recommend starting with third-party apps and gradually adjusting system services.