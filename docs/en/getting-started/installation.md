# Installation Guide

!!! danger "⚠️ Emergency Recovery Mode - Most Important!"
    **If your device becomes stuck, bootloops, or experiences system anomalies after installation**:
    
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
    
    **Prevention**: Configure carefully when first using, test rules gradually.

## Prerequisites

### System Requirements
- Android 7.0 (API 24) or higher

!!! error "Device Compatibility Limitations"
    **Samsung devices with OneUI are currently not supported**
    
    Due to OneUI modifications to Android source code, hook locations have been attempted through various methods but consistently fail to work. We are researching solutions, but currently cannot work normally on Samsung OneUI devices.
    
    Other manufacturer Android devices usually work normally.

### Xposed Framework
Install one of the following frameworks:

| Framework | Supported Versions | Recommendation |
|-----------|-------------------|----------------|
| LSPosed | Android 8.1+ | ⭐⭐⭐⭐⭐ |
| EdXposed | Android 8.0-11 | ⭐⭐⭐ |

!!! info "Framework Selection"
    LSPosed is recommended for better compatibility and stability.

## Download NoWakeLock

### Official Channels

[![GitHub](https://img.shields.io/badge/GitHub-Releases-blue)](https://github.com/NoWakeLock/NoWakeLock/releases)
[![IzzyOnDroid](https://img.shields.io/badge/IzzyOnDroid-F-Droid-green)](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock)

**Download Methods**:
- **GitHub Releases** - Direct APK file download
- **IzzyOnDroid** - Install after adding IzzyOnDroid repository to F-Droid
- **Official F-Droid** - Planned

!!! tip "F-Droid Repository Setup"
    To install through IzzyOnDroid:
    1. Add repository to F-Droid app: `https://apt.izzysoft.de/fdroid/repo`
    2. Search for NoWakeLock to install

### Version Selection

- **Stable Release** - Download from GitHub Releases or IzzyOnDroid
- **Beta Version** - Built from dev branch

!!! warning "Official Versions Only"
    Support is only provided for versions downloaded from official channels.

## Installation Steps

### 1. Download APK
Download the latest version APK file from official channels.

### 2. Install Application
```bash
# Install using ADB (optional)
adb install nowakelock-v3.x.x.apk
```

Or install the APK file directly on your device.

[Screenshot needed: Installation interface]

### 3. Enable Module
1. Open Xposed Manager (LSPosed/EdXposed)
2. Go to "Modules" page
3. Check NoWakeLock
4. Restart device

[Screenshot needed: LSPosed module list]

### 4. Configure Scope
Set module scope in LSPosed:

**Required Scope**:
- `android` (System Framework)

!!! tip "Scope Explanation"
    NoWakeLock only needs `android` system framework scope to work properly.

[Screenshot needed: Scope configuration]

## Verify Installation

### Check Module Status
1. Open NoWakeLock application
2. Go to "Module Check" page
3. Confirm all items show green checkmarks ✅

[Screenshot needed: Module check page]

### Verification Items

| Check Item | Description |
|------------|-------------|
| Xposed Framework Active | Framework running normally |
| Module Loaded | NoWakeLock module recognized |
| Hook Working Normally | System call interception successful |
| Configuration Read Success | Application can read configuration |

### Test Functionality
1. Check if "Apps" page displays installed applications
2. Verify "WakeLocks" page has data
3. Try setting a simple rule

## Common Issues

### Module Not Activated
**Symptoms**: Module check shows ❌  
**Solutions**:
1. Confirm Xposed framework is running normally
2. Check if module is checked
3. Restart device and check again

### Hook Not Working
**Symptoms**: No WakeLock/Alarm data  
**Solutions**:
1. Confirm scope includes `android`
2. Check SELinux policies
3. Review Xposed logs

### Application Crashes
**Symptoms**: App crashes immediately on open  
**Solutions**:
1. Check Android version compatibility
2. Clear application data
3. Reinstall module

## Uninstall Module

### Complete Uninstall Steps
1. Uncheck module in Xposed Manager
2. Restart device
3. Uninstall NoWakeLock application

## Next Steps

After installation completion:

1. [Quick Start](quick-start.md) - 5-minute setup configuration
2. [Module Check](module-check.md) - Detailed module status verification
3. [WakeLock Management](../features/wakelocks.md) - Start managing WakeLocks