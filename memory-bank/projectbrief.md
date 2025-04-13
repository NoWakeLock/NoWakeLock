# σ₁: Project Brief
*v1.0 | Created: 2025-04-13 | Updated: 2025-04-13*
*Π: INITIALIZING | Ω: PLAN*

## 🏆 Overview
NoWakeLock is an Android application that allows users to control the frequency and duration of waking up their Android device to save power consumption. It functions as an Xposed module to intercept and manage wakelock, alarm, and service operations on Android devices.

## 📋 Requirements
- [R₁] Control Wakelock/Alarm/Service by application
- [R₂] Support for application-level regular expression interception
- [R₃] Limited multi-user support
- [R₄] Compatible with Android N (7.0) and above
- [R₅] Open source with no private data collection
- [R₆] Support for monitoring and blocking wakelock operations
- [R₇] Support for monitoring and blocking alarm operations
- [R₸] Support for monitoring and blocking service operations

## 🎯 Target Audience
- Power users wanting granular control over device wake patterns
- Users with battery life concerns
- Xposed/LSPosed framework users
- Android developers testing wake behavior

## 🔑 Key Features
- Block specific wakelocks by application
- Set time intervals between wakelocks
- Regular expression support for pattern-based blocking
- Monitor wakelock, alarm and service activity
- Custom rules for different apps

## 📊 Success Metrics
- Reduced device wake-ups
- Extended battery life
- Detailed logging of system activity
- User control over battery consumption

## 🧰 Technical Stack
- Kotlin
- Xposed Framework
- Android SDK
- Room Database (inferred)
- Koin for dependency injection

## 📝 Notes
- Currently in Beta testing stage
- Version 2.0 is not compatible with previous configurations
- Tested with EdXposed / LSPosed 