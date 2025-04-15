# σ₃: Technical Context
*v1.0 | Created: 2025-04-15 | Updated: 2025-04-15*
*Π: 🏗️DEVELOPMENT | Ω: 🔍R*

## 🛠️ Technology Stack
- 🖥️ Frontend: Jetpack Compose, Material 3, Compose Navigation
- 🗄️ Backend: Kotlin, Android SDK, Xposed Framework
- 📊 Database: Room Persistence Library
- 🧪 Testing: JUnit, Espresso
- 🚀 Deployment: Google Play Store, F-Droid

## ⚙️ Development Environment
- [E₁] Android Studio ⟶ Latest version
- [E₂] Gradle 8.x ⟶ For build automation with Kotlin DSL
- [E₃] Git ⟶ For version control
- [E₄] GitHub Actions ⟶ For CI/CD
- [E₅] Rooted Android device/emulator ⟶ For testing Xposed functionality
- [E₆] Android SDK 35 ⟶ Latest target SDK

## 📦 Dependencies
- [D₁] Kotlin Coroutines ⟶ Version latest, For asynchronous programming
- [D₂] Jetpack Compose ⟶ Version 2025.x.x, For modern UI development
- [D₃] Room ⟶ Version 2.7.0, For local database operations
- [D₄] Koin ⟶ Version 4.0.4, For dependency injection
- [D₅] Xposed Framework ⟶ Version 93+, For system-level hooks
- [D₆] Coil ⟶ Version 2.7.0, For app icon loading
- [D₇] Kotlinx Serialization ⟶ Version 1.7.1, For type-safe navigation
- [D₈] Kotlinx Collections Immutable ⟶ Version 0.3.7, For immutable collections
- [D₉] DataStore Preferences ⟶ Version 1.1.4, For preferences storage
- [D₁₀] Navigation Compose ⟶ Version 2.8.9, For navigation
- [D₁₁] Core Splashscreen ⟶ Version 1.0.1, For splash screen
- [D₁₂] Material 3 ⟶ Latest version, For modern UI components
- [D₁₃] ConstraintLayout Compose ⟶ Version 1.1.1, For complex layouts

## 🚧 Technical Constraints
- [T₁] Root Access ⟶ Required for complete functionality with Xposed
- [T₂] Android Version Compatibility ⟶ Minimum SDK 24 (Android 7.0), Target SDK 35
- [T₃] Battery Usage ⟶ Monitoring must have minimal impact on battery
- [T₄] Permission Model ⟶ Requires QUERY_ALL_PACKAGES permission
- [T₅] Xposed Module Scope ⟶ Limited to specific packages defined in scopes array
- [T₆] Multi-user Support ⟶ Must handle data for different Android user profiles
- [T₇] Edge-to-edge UI ⟶ Modern UI requires proper edge-to-edge handling

## 🔧 Tool Usage Patterns
- [Tool₁] Xposed Framework ⟶ For monitoring system wakelocks, alarms, and services
- [Tool₂] Android Debug Bridge ⟶ For development and testing
- [Tool₃] Battery Stats ⟶ For measuring app impact
- [Tool₄] System API Hooks ⟶ For intercepting wakelock, alarm, and service calls
- [Tool₅] Compose Tooling ⟶ UI previews and testing
- [Tool₆] Material 3 Components ⟶ For consistent, modern UI
- [Tool₇] Data Serialization ⟶ For backup/restore functionality 