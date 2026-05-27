# σ₁: Project Brief
*v1.1 | Updated: 2026-05-27*
*Π: 🏗️DEVELOPMENT | Ω: 🔍R*

## 🏆 Overview
NoWakeLock Extended is an Android application that functions as an Xposed module and a Shizuku client to intercept and manage wakelock, alarm, and service operations on Android devices. It helps users control the frequency and duration of waking up their devices to save power consumption and optimize battery life. The app provides granular control at the application level with support for regular expression-based filtering.

Currently refactoring the UI with Material Design 3 and enhancing statistics and battery optimization features, alongside adding rootless Shizuku support.

## 📋 Requirements
- [R₁] Control Wakelock/Alarm/Service by application
- [R₂] Support for application-level regular expression interception
- [R₃] Limited multi-user support
- [R₄] Compatible with Android N (7.0) and above
- [R₅] Open source with no private data collection
- [R₆] Support for monitoring and blocking wakelock operations
- [R₇] Support for monitoring and blocking alarm operations
- [R₈] Support for monitoring and blocking service operations
- [R₉] User-friendly interface with clear battery impact visualization
- [R₁₀] Comprehensive statistics to help users understand device wake patterns
- [R₁₁] Rootless support via Shizuku
- [R₁₂] Samsung OneUI Compatibility

## 🎯 Goals
- [G₁] Refactor UI using Material Design 3.
- [G₂] Full support for Wakelock, Alarm, and Service through both Xposed and Shizuku.
- [G₃] Others: Multi-user support, backup functionality, and documentation for Wakelock/alarm/service.
- [G₄] Enhance battery usage statistics and visualization.
- [G₅] Provide more detailed battery impact analysis.
- [G₆] Achieve 100% rootless capability for widespread adoption.

## 🔍 Success Criteria
- [S₁] Proper control of wakelock/alarm/service.
- [S₂] MD3 UI functioning correctly.
- [S₃] Multi-user support, backups, and explanations working normally.
- [S₄] Reduce device wakeups and extend battery life.
- [S₅] Detail system activity and provide user control over battery consumption.
- [S₆] Fully functional on non-rooted devices via Shizuku.
- [S₇] Fully functional on Samsung devices.

## 🧩 Project Scope
- [In-scope] Proper control of wakelock/alarm/service.
- [In-scope] MD3 UI functioning correctly.
- [In-scope] Multi-user; Backups; Explanations.
- [In-scope] Battery usage impact assessment and visualization.
- [In-scope] Detailed wake statistics and blocking percentage calculations.
- [In-scope] Shizuku ADB-level operation monitoring and blocking.
- [Out-of-scope] System-level modifications requiring custom ROMs.
- [Out-of-scope] Automatic wakelock optimization.

## 🎯 Target Audience
- Power users wanting granular control over device wake patterns
- Users with battery life concerns
- Xposed/LSPosed framework users
- Users without root access (via Shizuku)
- Android developers testing wake behavior

## 🔑 Key Features
- Block specific wakelocks by application
- Set time intervals between wakelocks
- Regular expression support for pattern-based blocking
- Monitor wakelock, alarm and service activity
- Custom rules for different apps
- Battery impact visualization
- Detailed statistics with blocking percentages
- Shizuku Rootless Fallback
- Universal compatibility including Samsung OneUI

## 🧰 Technical Stack
- Kotlin
- Xposed Framework
- Shizuku API
- Android SDK
- Room Database
- Koin for dependency injection 

## 🚀 Recent Improvements
- Added Shizuku support for rootless operation.
- Fixed Samsung OneUI incompatibility by refactoring `SettingsProviderHook`.
- Fixed a critical data wipe bug triggered by IPC deserialization errors.
- Enhanced UI with Material Design 3.
- Improved data management capabilities.
- Fixed message persistence bug in settings screen.