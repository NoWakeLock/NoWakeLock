# σ₃: Technical Context
*v1.0 | Created: 2025-04-13 | Updated: 2025-04-13*
*Π: INITIALIZING | Ω: PLAN*

## 🛠️ Technology Stack

### 🖥️ Core
- **Language**: Kotlin
- **Android SDK**: API 24+ (Android 7.0 Nougat and above)
- **JVM Target**: Java 8

### 📱 Application Framework
- **Xposed Framework**: For system-level hooking
- **LSPosed/EdXposed**: Modern Xposed framework implementations

### 🏗️ Architecture Components
- **MVVM Pattern**: For UI and business logic separation
- **Repository Pattern**: For data abstraction
- **Android Architecture Components**: LiveData, ViewModel

### 💾 Data Management
- **Room Database**: For persistent storage of events and settings
- **SharedPreferences**: For light configuration storage
- **JSON Serialization**: Using Gson for complex data structures

### 🧩 Dependency Injection
- **Koin**: Lightweight DI framework for Kotlin

### 📊 UI Components
- **AndroidX**: Modern Android UI components
- **ViewBinding**: For type-safe view access
- **Fragment Navigation**: For app navigation

## 🔌 Integrations

### 📋 Android System Hooks
- **PowerManagerService**: For wakelock interception
- **AlarmManagerService**: For alarm scheduling interception
- **ActivityManagerService**: For service operations interception
- **SettingsProvider**: For settings monitoring

### 🛡️ Security Considerations
- **Root Access**: Required for Xposed framework functionality
- **Module Verification**: Ensures module is active in Xposed
- **Isolation**: Each hook operates independently for stability

## 🧰 Development Environment

### 🔧 Build Tools
- **Gradle**: For build automation
- **Android Gradle Plugin**: For Android-specific build tasks
- **ProGuard**: For code optimization and obfuscation

### 🧪 Testing Framework
- **JUnit**: For unit testing
- **Android Instrumentation Tests**: For UI and integration testing

### 📦 Libraries
- **Kotlin Standard Library**: Core language utilities
- **Kotlin Coroutines**: For asynchronous programming
- **Android KTX**: Kotlin extensions for Android

## 🚀 Deployment

### 📲 Distribution Channels
- **GitHub Releases**: Primary distribution channel
- **IzzyOnDroid F-Droid Repository**: Alternative distribution

### 🏗️ CI/CD
- **GitHub Actions**: For automated builds and tests
- **Fastlane**: For streamlined deployment

## 🔄 Upgrade Considerations

### 🔄 Version Compatibility
- Version 2.0 not compatible with previous configurations
- Users must clear application data when upgrading

### 📚 API Level Targeting
- Different handling for:
  - API 24-30 (Android 7.0-11.0)
  - API 31+ (Android 12.0+)

## 📝 Coding Conventions

### 🧩 Project Structure
- **Package by Feature**: Organization structure
- **Clean Architecture Principles**: Separation of concerns

### 🔍 Code Quality
- **Modern Kotlin Practices**: Use of extension functions, higher-order functions
- **Error Handling**: Try-catch blocks around system interactions
- **Logging**: Xposed logging for debugging and diagnostics

## 🔍 Technical Debts & Considerations
- Implement application statistics feature
- Improve data backup and recovery
- Ensure compatibility with newer Android versions
- Optimize battery usage of the module itself 