# Frequently Asked Questions

Collection of the most commonly encountered questions and solutions.

## Installation Related

### Q: Module doesn't work after installation?
**A**: Check the following items:
1. Confirm the Xposed framework is running normally
2. Check the NoWakeLock module in the framework manager
3. Restart the device and check module status
4. Confirm scope includes `android` system framework

### Q: Which Xposed frameworks are supported?
**A**: Supported frameworks:
- **LSPosed** - Recommended, best compatibility
- **EdXposed** - Supported on some devices
- **TaiChi** - Not recommended, may have compatibility issues

### Q: Module check shows red ❌?
**A**: Handle based on specific item:
- **Framework not activated** → Check Xposed installation
- **Module not loaded** → Confirm module is checked and restart
- **Hook failed** → May be system version incompatibility

## Feature Usage

### Q: Rules have no effect after setting?
**A**: Possible reasons:
1. **Rules not effective** - Check if rules are enabled
2. **App restart** - Some rules require app restart
3. **System cache** - Wait a few minutes for system to apply new rules
4. **Permission issues** - Confirm NoWakeLock has necessary permissions

### Q: Some apps cannot receive push notifications?
**A**: Solution steps:
1. View the app's WakeLock list
2. Find push-related WakeLocks (usually contain Push, GCM, FCM)
3. Change these WakeLocks to "Allow" mode
4. Restart app to test push functionality

### Q: Does app become laggy or function abnormally?
**A**: Immediate actions:
1. Temporarily disable all rules for that app
2. Enable rules one by one to find the problem rule
3. Adjust problem rule parameters or change to limit mode
4. Avoid intercepting critical system services

### Q: How to determine if optimization is effective?
**A**: Observe metrics:
- **Short term (24 hours)**: Check interception statistics, app functions normally
- **Medium term (one week)**: Battery life significantly improved
- **Long term**: Overall system smoothness improved

## Compatibility Issues

### Q: Android version compatibility?
**A**: Support status:
- **Android 7.0-16** - AOSP Full support

### Q: Specific brand device issues?
**A**: Known issues:
- **MIUI** - Need to disable MIUI optimization, allow background running
- **ColorOS** - May need to allow auto-start in permission management
- **EMUI** - Recommend adding NoWakeLock to protected apps
- **OneUI** - ⚠️ **Currently not supported** - Due to OneUI modifying Android source code, Hook locations have been tried with various methods but consistently fail to work

### Q: Module fails after system update?
**A**: Handling steps:
1. Check if Xposed framework still works
2. Confirm NoWakeLock module is still activated
3. Reinstall module if necessary
4. Reconfigure scope settings

## Performance Issues

### Q: NoWakeLock itself consumes battery?
**A**: Under normal circumstances NoWakeLock consumes very little battery:
- **CPU usage** < 1%
- **Memory usage** < 50MB
- **Background activity** minimized

If abnormal battery consumption is found, please check configuration or contact support.

### Q: Device becomes slow or laggy?
**A**: Possible reasons:
1. **Too many rules** - Reduce unnecessary rules
2. **Complex regex** - Simplify regular expressions
3. **Frequent interception** - Adjust interception strategy
4. **Insufficient memory** - Restart device to free memory

### Q: App startup becomes slow?
**A**: Check if:
1. Intercepted Services necessary for app startup
2. Over-restricted initialization-related WakeLocks
3. Rules affected app startup process

## Data Issues

### Q: Statistics data inaccurate?
**A**: Possible situations:
1. **Data delay** - Statistics update every 5 minutes
2. **Device restart** - Some real-time data reset after restart
3. **Timezone issues** - Check device timezone settings
4. **Storage issues** - Clear database cache

### Q: Historical data lost?
**A**: Data retention policy:
- **Real-time data** - Cleared after device restart
- **Statistics data** - Retained for 30 days
- **Configuration data** - Permanently saved (unless manually cleared)

### Q: Exported configuration cannot be imported?
**A**: Check items:
1. File format correct (JSON)
2. Version compatibility
3. File not corrupted
4. Sufficient permissions

## Multi-user Issues

### Q: How to use in multi-user environment?
**A**: Notes:
1. Each user needs separate configuration
2. System apps share settings across all users
3. Configurations don't affect each other when switching users
4. Some features require primary user permissions

### Q: Cannot use in work profile?
**A**: Work profile limitations:
1. Need to install Xposed framework separately in work profile
2. Some enterprise policies may prohibit Xposed modules
3. Contact administrator to confirm related policies

## Security Issues

### Q: Is NoWakeLock safe?
**A**: Security measures:
- **Open source code** - All code publicly available for review
- **No network permissions** - Won't upload any data
- **Local storage** - All data saved only on device locally
- **Minimal permissions** - Only requests necessary system permissions

### Q: Will it affect system stability?
**A**: Safety considerations:
1. Use system standard Hook mechanism
2. Minimize impact on system calls
3. Automatic degradation handling in exception cases
4. Don't modify system core files

### Q: Will privacy data leak?
**A**: Privacy protection:
- **No data collection** - Don't collect any personal information
- **No network communication** - Don't communicate with external servers
- **Local processing** - All analysis performed locally on device
- **User control** - Users have complete control over all data

## Troubleshooting

### Q: What to do if completely unable to use?
**A**: Reset steps:
1. Disable module in Xposed
2. Restart device
3. Clear NoWakeLock app data
4. Re-enable module and configure

### Q: Some apps frequently crash?
**A**: Emergency handling:
1. Immediately disable all rules for that app
2. Check error information in system logs
3. Gradually restore rules and observe
4. Add app to whitelist if necessary

### Q: How to collect debug information?
**A**: Information collection:
```bash
# Device information
adb shell getprop ro.build.version.release
adb shell getprop ro.product.model

# App logs
adb logcat | grep -i nowakelock

# Xposed logs
adb logcat | grep -i xposed
```

## Basic Concepts

### Q: What are WakeLock/Alarm/Service? How to configure for best results?

**A**: Core concept explanation:

**WakeLock (Wake Lock)**:
- Mechanism to prevent device from entering sleep state
- Types: PARTIAL (CPU running), SCREEN (screen on), etc.
- Official documentation: [Android WakeLock Guide](https://developer.android.com/training/scheduling/wakelock)

**Alarm (Scheduled Tasks)**:
- System timer that triggers tasks at specific times
- Types: RTC, ELAPSED_REALTIME, etc.
- Official documentation: [Android Alarms Guide](https://developer.android.com/training/scheduling/alarms)

**Service (Services)**:
- App components running in background
- Types: foreground service, background service, bound service
- Official documentation: [Android Services Guide](https://developer.android.com/guide/components/services)

**Recommended learning resources**:
- XDA Guide: ["Complete WakeLock Guide for Beginners"](https://forum.xda-developers.com)
- [Amplify](https://forum.xda-developers.com/t/mod-xposed-amplify-battery-extender-control-alarms-services-and-wakelocks.2853874/) - Provides WakeLock/Alarm/Service information reference lists
- [WakeBlock](https://github.com/MrLast98/WakeBlock) - Also provides WakeLock information

**Note**: Unfortunately there's no perfect universal reference, differences between devices are huge. Need to adjust based on specific device and apps.

## Critical Issue Handling

### Q: What to do if device won't boot due to incorrect operations?

**A**: Emergency recovery steps:

**Situation 1: Boot issues caused by NoWakeLock module**:

**Method 1: Hardware button safe mode (Recommended)**:
```bash
1. Long press power button for 10 seconds to force restart
2. Immediately after screen goes black, repeatedly press any hardware button (volume or power)
3. After feeling 2 short vibrations, continue quickly pressing same button 4 times
4. After 4th press feel long vibration, indicating Xposed is disabled
5. After normal boot, disable NoWakeLock module in LSPosed
```

**Method 2: Recovery filesystem method**:
```bash
1. Enter TWRP Recovery
2. Tap Advanced → File Manager
3. Navigate to /data/adb/lspd/
4. Delete config folder
5. Restart to system
```

**Method 3: Device-specific method (e.g., Pixel)**:
```bash
Press volume down frantically after Google Logo appears during boot
Until device vibrates confirming safe mode entry
```

**Situation 2: Mistakenly intercepted important system components**:
```bash
1. Enter Recovery → File Manager
2. Navigate to /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock
   # xxx-xxx-xxx is a long random string, may be different for each device
3. Delete entire folder
4. Restart device
5. After entering system restore misoperation, if unclear about specific problem, directly clear NoWakeLock data
```

**When cause is uncertain**:
- After entering system directly clear NoWakeLock app data
- Reconfigure, avoid intercepting critical system components

!!! danger "Important Warning"
    Always backup configuration before modifying critical system components. Recommend starting debugging from third-party apps, gradually adjusting system services.

## Privacy & Security

### Q: Does NoWakeLock collect privacy data?

**A**: Privacy protection commitment:
- **Completely local** - All data stored only locally on device
- **Zero data upload** - No data uploaded to any servers
- **No privacy collection** - Don't collect or store any personal information
- **Open source transparency** - Source code completely public and reviewable

**Possible future features**:
- May add optional cloud configuration sync feature
- Users have complete control over whether to enable
- Will still adhere to privacy protection principles

## Contributing

### Q: What to do if need new features or found bugs?

**A**: Participation methods:
- **GitHub Issues** - [Submit issues or feature requests](https://github.com/NoWakeLock/NoWakeLock/issues)
- **Detailed description** - Provide complete problem description and reproduction steps
- **Active feedback** - Developers will try their best to handle all feedback

### Q: How to help update translations?

**A**: Translation contributions:
- **Pull Request** - Submit translation PR directly
- **Multi-language support** - Welcome translation contributions in various languages
- **Community collaboration** - Can discuss translation details in community groups

## Getting Help

### Q: Where to report issues?
**A**: Support channels:
- **GitHub Issues** - Detailed technical issues and feature requests
- **Telegram Group** - [@nowakelock](https://t.me/nowakelock) Quick questions and discussions
- **Discord Community** - [NoWakelock](https://discord.gg/kewmG5AShQ) In-depth technical exchanges

### Q: How to provide effective issue reports?
**A**: Include information:
1. **Device info** - Brand, model, Android version
2. **Framework info** - Xposed framework type and version
3. **Problem description** - Specific problem phenomena
4. **Reproduction steps** - How to reproduce the problem
5. **Log information** - Relevant error logs
6. **Screenshots** - Screenshots of problem interface

### Q: How long to get response?
**A**: Response time:
- **Community groups** - Usually within a few hours
- **GitHub Issues** - 1-3 business days
- **Emergency issues** - Will be prioritized

!!! tip "Usage Suggestions"
    When encountering problems, recommend checking documentation and FAQ first. If problem persists, please provide detailed information to better assist in resolving.

!!! warning "Important Reminder"
    Please backup important data before use. Incorrect configuration may affect device normal operation, please operate carefully.