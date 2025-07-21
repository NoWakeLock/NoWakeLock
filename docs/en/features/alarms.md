# Alarm Management

Alarms are Android system timer mechanisms used to trigger operations at specific times. Frequent alarms can affect device battery life.

!!! danger "⚠️ Emergency Recovery"
    **If incorrect alarm configuration causes system issues**, see the [Emergency Recovery Guide](../reference/troubleshooting.md#emergency-recovery) for detailed instructions on how to disable the module and restore normal operation.

## Feature Overview

### Alarm Functions
- Execute tasks at scheduled times
- Trigger periodic operations
- System-level timers
- App keep-alive mechanism

### Management Objectives
- Monitor alarm setting and triggering
- Identify excessively frequent scheduled tasks
- Control alarm trigger frequency
- Reduce unnecessary wake-ups

## Interface Description

### Alarm List

【Screenshot needed: Alarm list page】

**List Information**:
- **Label** - Alarm identifier
- **App** - Source package name
- **Type** - Alarm type icon
- **Status** - Blocking status
- **Statistics** - Trigger count and time information

### Status Display

| Status | Icon | Description |
|--------|------|-------------|
| Allow | 🟢 | Normal triggering |
| Limit | 🟡 | Reduced trigger frequency |
| Block | 🔴 | Prevent triggering |
| Pending | ⏰ | Set and waiting to trigger |

## Alarm Types

### Classification by Trigger Condition

| Type | Description | Typical Use |
|------|-------------|-------------|
| RTC | Absolute time trigger | Alarms, reminders |
| RTC_WAKEUP | Absolute time wake device | Important notifications |
| ELAPSED_REALTIME | Relative time trigger | Periodic checks |
| ELAPSED_REALTIME_WAKEUP | Relative time wake | Background tasks |

### Classification by Repeat Pattern

**One-time Alarm**:
- Auto-cancel after one execution
- Used for specific time tasks

**Repeating Alarm**:
- Repeat trigger at fixed intervals
- Common for sync, update tasks

**Exact Alarm**:
- Precise time triggering
- Higher system resource consumption

## Configuration Options

### Processing Modes

#### Allow Mode
- Alarms set and trigger normally
- No intervention
- Suitable for important system functions

#### Limit Mode
- Reduce trigger frequency
- Merge nearby trigger times
- Delay non-urgent alarms

#### Block Mode
- Completely prevent alarm setting
- App cannot create this type of alarm
- May severely affect app functionality

### Advanced Options

**Smart Merging**:
- Merge alarms with similar times
- Reduce device wake-up frequency

**Batch Processing Mode**:
- Delay non-urgent alarms
- Execute together with other tasks

## Usage Methods

### View Alarm List

1. Click "Alarms" tab at the bottom
2. View currently active alarms
3. Use filters to view specific status

### Configure Alarm Rules

1. Click target alarm item
2. Select processing mode
3. Set specific parameters:
   - Minimum interval time
   - Delay time
   - Batch processing options

【Screenshot needed: Alarm configuration page】

### Batch Management

**Batch setting by app**:
1. Filter specific app's alarms
2. Select batch operation
3. Apply unified rules

**Batch setting by type**:
- Limit all WAKEUP types
- Reduce frequency of all repeating alarms
- Handle system alarms cautiously

## Practical Applications

### Problem Identification

#### Abnormal Alarm Characteristics

**High Frequency Triggering**:
- Repeating alarms with intervals less than 1 minute
- Frequent triggering during night hours
- Still running when device is stationary

## Technical Implementation

### Hook Mechanism

Intercept key methods in AlarmManagerService:
```kotlin
// System alarm setting calls
setImpl(
    int type,
    long triggerAtTime,
    long windowLength,
    long interval,
    PendingIntent operation,
    IAlarmListener directReceiver,
    String listenerTag,
    WorkSource workSource,
    AlarmManager.AlarmClockInfo alarmClock,
    int callingUid,
    String callingPackage
)

// Alarm trigger processing
triggerAlarmsLocked(ArrayList<Alarm> triggerList)
```

### Data Processing

**Real-time Processing**:
- Rule checking when alarm is set
- Frequency control when triggering
- Dynamic adjustment of trigger time

**Historical Records**:
- Database storage of trigger history
- Statistical analysis and trend calculation
- Automatic cleanup of expired data

### Compatibility

**Version Support**:
- Full support for Android 7.0+
- API adaptation for different versions
- Fallback compatibility strategy

**Performance Optimization**:
- Minimize hook overhead
- Efficient rule matching algorithm
- Asynchronous processing of statistics

## Related Features

- [App Management](app-management.md) - View all alarms by app
- [WakeLock Management](wakelocks.md) - Optimize together with WakeLocks
- [Rules System](rules-regex.md) - Use regex for batch configuration

!!! tip "Optimization Suggestions"
    Alarm optimization effects are obvious but need to balance functionality. Recommend starting with non-critical apps and gradually adjusting important app settings.

!!! warning "Precautions"
    Excessive restriction of system alarms may affect normal device functions, such as automatic time sync, system update checks, etc.