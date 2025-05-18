# σ₁: Project Brief
*v1.0 | Created: 2025-04-15 | Updated: 2025-04-17*
*Π: 🏗️DEVELOPMENT | Ω: 🔍R*

## 🏆 Overview
NoWakeLock is an Android application that functions as an Xposed module to intercept and manage wakelock, alarm, and service operations on Android devices. It helps users control the frequency and duration of waking up their devices to save power consumption and optimize battery life. The app provides granular control at the application level with support for regular expression-based filtering.

目前正在使用 md3 重构 UI 并加强统计和电池优化功能;

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

## 🎯 Goals
- [G₁] 使用 md3 重构 UI;
- [G₂] Wakelock alarm service 完整支持
- [G₃] 其他的: 多用户 备份 Wakelock/alarm/service 说明
- [G₄] 加强电池使用统计和可视化功能
- [G₅] 提供更详细的电池影响分析

## 🔍 Success Criteria
- [S₁] 正常控制 wakelock/alarm/service
- [S₂] MD3 UI 正常工作
- [S₃] 多用户 备份 Wakelock/alarm/service 说明等正常
- [S₄] 减少设备唤醒次数，延长电池寿命
- [S₅] 详细记录系统活动，提供用户对电池消耗的控制

## 🧩 Project Scope
- [In-scope] 正常控制 wakelock/alarm/service
- [In-scope] MD3 UI 正常工作
- [In-scope] 多用户; 备份; Wakelock/alarm/service 说明
- [In-scope] 电池使用影响评估和可视化
- [In-scope] 详细的唤醒统计和阻止百分比计算
- [Out-of-scope] 系统级修改 需要 自定义 ROM
- [Out-of-scope] 自动 wakelock 优化

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
- Battery impact visualization
- Detailed statistics with blocking percentages

## 🧰 Technical Stack
- Kotlin
- Xposed Framework
- Android SDK
- Room Database
- Koin for dependency injection 

## 🚀 Recent Improvements
- Enhanced UI with Material Design 3
- Improved data management capabilities 
- Added automatic message clearing to improve user experience
- Multi-language support with localized messages in English, Chinese, and French
- Fixed message persistence bug in settings screen 