# σ₁: Project Brief
*v1.1 | Created: 2025-04-09 | Updated: 2025-04-11*
*Π: INITIALIZING | Ω: RESEARCH*

## 🏆 Overview
NoWakeLock is an Android application that allows users to control the frequency and duration of waking up their Android devices to save power consumption. The app provides full control over Wakelock/Alarm/Service components, allowing users to intercept and manage these system operations at the application level.

## 📋 Requirements
- [R₁] Control Wakelock/Alarm/Service by application
- [R₂] Support for application-level regular expression interception
- [R₃] Limited multi-user support
- [R₄] Modern UI with Material Design 3 and Jetpack Compose
- [R₅] Support for Android 7 (N) and above
- [R₆] Fully open source with no private data collection

## ✅ Success Criteria
- [C₁] Successfully monitor and block wakelocks, alarms, and services
- [C₂] Improve battery life for Android devices
- [C₃] Provide a modern, intuitive user interface
- [C₄] Complete migration to Jetpack Compose from previous UI

## 🔍 Scope
### ✓ In Scope
- [S₁] UI redesign using Material 3 and Jetpack Compose
- [S₂] Record and block alarms, wakelocks, and services
- [S₃] Regular expression support for filtering
- [S₄] Data backup and recovery features
- [S₅] Application statistics

### ❌ Out of Scope
- [O₁] Support for Android versions below 7.0
- [O₂] Root-only features (relies on Xposed framework)
- [O₃] Automatic optimization without user input

## ⏱️ Timeline
- [T₁] Compose UI Migration: Ongoing
- [T₂] Feature Parity with Legacy Version: Q2 2025
- [T₃] Release-ready Version: Q3 2025

## 👥 Stakeholders
- [STK₁] NoWakeLock Users: End users seeking battery optimization
- [STK₂] App Developers: Contributors to the open source project

---
*σ₁ foundation document informing all other memory files*