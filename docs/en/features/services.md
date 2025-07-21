# Service Management

Service is an Android component for performing long-running tasks in the background. Too many background services consume system resources and battery.

!!! danger "⚠️ Rescue Mode - Important Notice!"
    **If incorrect WakeLock configuration causes device boot failure**:
    
    **Situation 1: LSPosed framework issue (problems occur after installation without configuration)**:
    1. Long press power button for 10 seconds to force restart
    2. Immediately press any hardware button repeatedly after screen goes black
    3. After feeling 2 short vibrations, continue pressing the same button 4 times quickly
    4. After the 4th press, feel a long vibration indicating LSPosed is disabled
    5. After normal boot, disable NoWakeLock module in LSPosed
    
    **Situation 2: Misconfiguration issues (can enter Recovery)**:
    1. Enter Recovery → File Management
    2. Navigate to /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock
       (xxx-xxx-xxx is a long random string that may be different for each device)
    3. Delete the entire folder
    4. Restart device
    
    **When unsure of the cause**: Directly clear NoWakeLock app data, avoid intercepting system critical components when reconfiguring.

## Feature Overview

### Service Functions
- Background data processing
- Network communication and downloads
- Media services like music playback
- System monitoring and maintenance

### Management Objectives
- Monitor service startup and binding behavior
- Control unnecessary background services
- Optimize service startup frequency
- Reduce resource usage

## Interface Description

### Service List

【Screenshot needed: Service list page】

**List Information**:
- **Service Name** - Service class name
- **App** - Source package name
- **Type** - Service type icon
- **Status** - Running status and blocking settings
- **Statistics** - Startup count and running duration

### Status Indicators

| Status | Icon | Description |
|--------|------|-------------|
| Allow | 🟢 | Normal startup and running |
| Limit | 🟡 | Limit startup frequency |
| Block | 🔴 | Prevent startup |
| Running | ▶️ | Currently running |
| Stopped | ⏹️ | Service stopped |

### Service Types

**Foreground Services**:
- Display persistent notifications
- User-perceivable services
- Such as music playback, navigation

**Background Services**:
- No user interface
- Execute tasks silently
- Such as data sync, cleanup

**Bound Services**:
- Bound to other components
- Provide interface calls
- Lifecycle tied to binder

## Configuration Options

### Processing Modes

#### Allow Mode
- Services start and run normally
- No restrictions imposed
- Suitable for important functional services

#### Limit Mode
- Control startup frequency
- Limit concurrent running count
- Auto-stop long-running services

#### Block Mode
- Completely prevent service startup
- Including explicit and implicit startup
- May affect app core functionality

### Advanced Options

**Smart Scheduling**:
- Delay non-urgent service startup
- Merge services with similar functions
- Adjust based on system load

**Resource Limits**:
- CPU usage limits
- Memory usage control
- Network traffic limits

## Usage Methods

### View Service List

1. Click "Services" tab at the bottom
2. View all detected services
3. Use filters to view specific status or app

### Configure Service Rules

1. Click target service item
2. Select processing mode
3. Set specific limit parameters:
   - Startup interval time
   - Maximum running duration
   - Resource usage limits

【Screenshot needed: Service configuration page】

### Batch Management

**Filter by app**:
- View all services of specific app
- Batch set app-level rules

**Filter by type**:
- Manage foreground services separately
- Uniformly limit background services

## Practical Applications

### Problem Identification

#### Abnormal Service Characteristics

**Frequent Startup**:
- Startup intervals less than 10 seconds
- Repeated startup of same service in short time
- Still starting services when app not in use

**Resource Consumption**:
- Long running time (over 30 minutes)
- High CPU usage (> 5%)
- Large memory usage (> 100MB)

**Invalid Services**:
- Stop immediately after startup
- Empty services with no actual function
- Services only used for keep-alive

## Technical Implementation

### Hook Mechanism

Intercept service management methods in ActivityManagerService:
```kotlin
// Service startup interception
startServiceLocked(
    IApplicationThread caller,
    Intent service,
    String resolvedType,
    int callingPid,
    int callingUid,
    boolean fgRequired,
    String callingPackage,
    int userId
)

// Service binding interception  
bindServiceLocked(
    IApplicationThread caller,
    IBinder token,
    Intent service,
    String resolvedType,
    IServiceConnection connection,
    int flags,
    String callingPackage,
    int userId
)
```

### Data Tracking

**Real-time Monitoring**:
- Record information when service starts
- Track service running status
- Calculate resource usage

**Historical Statistics**:
- Database storage of service history
- Analyze startup patterns and trends
- Generate optimization suggestions

### Compatibility Handling

**Version Adaptation**:
- Adapt to Android 8.0+ background service restrictions
- Handle API differences between versions
- Special handling for foreground services

**Performance Optimization**:
- Minimize hook call overhead
- Efficient rule matching
- Asynchronous processing of statistics

## Related Features

- [App Management](app-management.md) - View all services by app
- [WakeLock Management](wakelocks.md) - Service-related WakeLocks
- [Rules System](rules-regex.md) - Use regex for batch configuration

!!! info "Android 8.0+ Changes"
    Android 8.0 started limiting background services, system automatically stops most background services. NoWakeLock's service management mainly targets foreground services and bound services.

!!! warning "Handle with Caution"
    Blocking critical services may cause app functionality issues. Recommend using limit mode first, then consider blocking after confirming no impact.