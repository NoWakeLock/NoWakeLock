# NoWakeLock

NoWakeLock is an Android Xposed module for managing device WakeLocks, Alarms, and Services to help optimize battery life.

!!! warning "Important Disclaimer and Usage Recommendations"
    **Use at your own risk. Developers are not responsible for device damage.**
    
    **Important**: If your device does not have battery life issues, it is not recommended to use this software. Android 11+ background management has been significantly optimized. Only use this tool when you have confirmed abnormal battery drain issues through tools like BetterBatteryStats.
    
    NoWakeLock is a targeted solution for specific problems, not a general optimization tool.

!!! danger "⚠️ Important: Emergency Recovery Mode"
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

## Core Features

- **WakeLock Management** - Monitor and control application wake locks
- **Alarm Control** - Manage system scheduled tasks
- **Service Management** - Control background service startup
- **Application Management** - View and configure all components by application
- **Rule System** - Flexible configuration with regular expression support

## Quick Start

1. [Installation Guide](getting-started/installation.md) - Install NoWakeLock module
2. [Problem Analysis](getting-started/problem-analysis.md) - Analyze battery issues (must read before use)
3. [Quick Start](getting-started/quick-start.md) - 5-minute basic configuration
4. [Module Check](getting-started/module-check.md) - Verify module status

## Main Features

### 📱 Application Management
- [Application Management](features/app-management.md) - View and configure by application

### ⚡ System Control
- [WakeLock Management](features/wakelocks.md) - Wake lock mechanisms that prevent device sleep
- [Alarm Management](features/alarms.md) - System scheduled task control
- [Service Management](features/services.md) - Background service control

### 🔧 Configuration Tools
- [Rules & Regular Expressions](features/rules-regex.md) - Flexible matching rules
- [Application Management](features/app-management.md) - Unified management by application

## Usage Guide

The main application interface has five tabs:
- **Apps** - Application list and overall management
- **WakeLocks** - WakeLock monitoring and control
- **Alarms** - Scheduled task management
- **Services** - Background service control
- **Settings** - Global settings and configuration

## Getting Help

- [FAQ](reference/faq.md) - Answers to most common questions
- [Troubleshooting](reference/troubleshooting.md) - Problem diagnosis and solutions
- [Glossary](reference/glossary.md) - Technical term explanations

## Compatibility

- **Android Version**: 7.0 (API 24) to 15.0 (API 35)
- **Xposed Framework**: LSPosed (recommended), EdXposed
- **Architecture Support**: ARM64, ARM32
- **Current Version**: 3.0.3 (stable release)

!!! error "Device Compatibility Limitations"
    **Samsung devices with OneUI are currently not supported**
    
    Due to OneUI modifications to Android source code, hook locations have been attempted through various methods but consistently fail to work. Other manufacturer Android devices usually work normally.

## Community & Support

- **Telegram**: [@nowakelock](https://t.me/nowakelock)
- **Discord**: [NoWakelock](https://discord.gg/kewmG5AShQ)
- **GitHub**: [NoWakeLock/NoWakeLock](https://github.com/NoWakeLock/NoWakeLock)

## Developers

Interested in technical implementation or contributing code?

- [Developer Documentation](developers/) - Technical architecture and implementation details
- [Development Environment](developers/) - How to participate in development

---

!!! warning "Usage Reminder"
    NoWakeLock requires Xposed framework. Please backup important data before use. Developers are not responsible for device issues.

!!! info "License"
    This project is open source under [GNU General Public License v3.0](https://github.com/NoWakeLock/NoWakeLock/blob/master/LICENSE).