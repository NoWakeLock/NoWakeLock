# σ₃: Technical Context
*v1.1 | Created: 2025-04-09 | Updated: 2025-04-11*
*Π: INITIALIZING | Ω: RESEARCH*

## 🛠️ Technology Stack
- 🖥️ Frontend: Material Design 3, Jetpack Compose UI
- 🔧 Backend: Xposed Framework API, Android Framework
- 💾 Database: Room Persistence Library
- ☁️ Infrastructure: Android Application (APK)

## 🚀 Development Environment
```bash
# Android Studio setup
# Ensure you have Android Studio Iguana (2023.2.1) or higher
# Java 17 is required for the project
# SDK 35 (Android 15) is the target
```

## 📦 Dependencies
- [D₁] Jetpack Compose: 2025.03.01 BOM - Modern UI toolkit
- [D₂] Room: 2.6.1 - Database persistence
- [D₃] Kotlin: 2.1.0 - Programming language
- [D₄] Koin: 4.0.4 - Dependency injection
- [D₅] Navigation Compose: 2.8.9 - Navigation components
- [D₆] Lifecycle: 2.8.7 - Lifecycle-aware components
- [D₇] Xposed Framework: API 82+ - System modification
- [D₈] Coil: 2.7.0 - Image loading library
- [D₉] Kotlinx Collections Immutable: 0.3.7 - Immutable collections

## 🚧 Technical Constraints
- [C₁] Minimum SDK: 24 (Android 7.0 / N)
- [C₂] Target SDK: 35 (Android 15)
- [C₃] Requires Xposed Framework (EdXposed/LSPosed) 
- [C₄] Java compatibility level: 17

## 🔄 Build & Deployment
- 🏗️ Build: Gradle with Kotlin DSL
- 🚀 Deploy: F-Droid, GitHub Releases, IzzyOnDroid
- 🔄 CI/CD: GitHub Actions workflow for automated builds

## 🧪 Testing Approach
- 🔬 Unit: JUnit for business logic
- 🔌 Integration: Android Instrumentation Tests
- 🖥️ E2E: Manual testing on various Android versions

---
σ₃ describes technologies and configuration