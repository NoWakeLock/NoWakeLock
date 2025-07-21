# App Management

App management functionality allows you to view and configure all WakeLocks, Alarms, and Services by application, providing a unified management view from the app perspective.

## Feature Overview

### Core Functions
- Display all activities categorized by app
- App-level statistical information
- Batch configuration of all app components
- App behavior pattern analysis

### Management Advantages
- Unified app perspective
- Easy identification of problematic apps
- Simplified batch configuration process
- Intuitive comparative analysis

## Interface Description

### App List

【Screenshot needed: App list page】

**Display Information**:
- **App icon and name**
- **Package name** - App unique identifier
- **Statistics summary** - WakeLock/Alarm/Service counts
- **Activity status** - Recent activity indicator
- **User** - User identifier in multi-user environment

### Statistics Cards

Each app displays three types of statistics:

| Type | Metrics | Description |
|------|---------|-------------|
| WakeLock | Count/Duration | Acquisition count and cumulative hold time |
| Alarm | Count/Frequency | Trigger count and average interval |
| Service | Count/Duration | Startup count and running time |

### Filtering and Sorting

**Filter Options**:
- All apps
- System apps
- User apps
- Apps with activity
- Apps with configured rules

**Sort Methods**:
- By app name
- By installation time
- By activity frequency
- By resource consumption
- By configuration status

## App Details Page

### Enter Details Page
1. Click any app in the app list
2. Enter app details page

【Screenshot needed: App details page】

### Details Page Content

#### App Information
- App name, version, package name
- Installation time, update time
- Permission list
- Current running status

#### Activity Statistics
- **Timeline chart** - 12-hour activity trends
- **Categorized statistics** - Detailed WakeLock/Alarm/Service data
- **Resource consumption** - CPU, memory, battery usage

#### Component Lists
Display by tab categories:
- **WakeLocks** - All WakeLocks of this app
- **Alarms** - All Alarms of this app
- **Services** - All Services of this app

## Configuration Features

### App-level Configuration

#### Global Settings
Set unified rules for apps:
```
Allow Mode:
- All components run normally
- Suitable for important apps

Limit Mode:
- Unified time restrictions
- Suitable for general apps

Block Mode:
- Block all background activities
- Suitable for problematic apps
```

#### Inheritance Mechanism
- Component level can inherit app settings
- Support exception configuration for individual components
- Priority: Component settings > App settings > Global defaults

## Multi-user Support

### User Switching
On multi-user devices:
1. Top user selector
2. Switch to view different users' apps
3. Independent configuration for each user

【Screenshot needed: User switching interface】

### User Isolation
- Each user's configuration doesn't affect others
- System apps are shared among all users
- User apps only display under corresponding user

## Import/Export

### Configuration Backup

#### Export Configuration
```json
{
  "version": "3.0",
  "timestamp": "2024-01-01T00:00:00Z",
  "user_id": 0,
  "apps": [
    {
      "package_name": "com.example.app",
      "app_config": {
        "mode": "limit",
        "wakelock_timeout": 60000
      },
      "components": [
        {
          "type": "wakelock",
          "name": "ExampleWakeLock",
          "mode": "allow"
        }
      ]
    }
  ]
}
```

#### Import Configuration
- Support full configuration import
- Selective import of specific apps
- Conflict resolution strategy selection

## Related Features

- [WakeLock Management](wakelocks.md) - Detailed WakeLock control
- [Alarm Management](alarms.md) - Detailed Alarm control
- [Service Management](services.md) - Detailed Service control
- [Rules System](rules-regex.md) - Use regex for batch configuration

!!! tip "Usage Recommendations"
    App management is the entry feature of NoWakeLock. Recommend new users start here to understand behavior patterns of each app, then perform targeted optimization.