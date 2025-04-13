# σ₄: Active Context
*v1.0 | Created: 2025-04-13 | Updated: 2025-04-13*
*Π: INITIALIZING | Ω: PLAN*

## 🔮 Current Focus
System initialization and analysis of the NoWakeLock codebase to understand its architecture and functionality.

## 📎 Context References

### 📄 Active Files
- app/src/main/java/com/js/nowakelock/xposedhook/XposedModule.kt
- app/src/main/java/com/js/nowakelock/xposedhook/hook/WakelockHook.kt
- app/src/main/java/com/js/nowakelock/BasicApp.kt
- README.md

### 💻 Active Code
- XposedModule.handleLoadPackage
- WakelockHook.hookWakeLocks
- WakelockHook.handleWakeLockAcquire
- WakelockHook.handleWakeLockRelease

### 📚 Active Docs
- README.md
- memory-bank/projectbrief.md
- memory-bank/systemPatterns.md
- memory-bank/techContext.md

### 📁 Active Folders
- app/src/main/java/com/js/nowakelock/xposedhook/hook
- app/src/main/java/com/js/nowakelock/xposedhook
- app/src/main/java/com/js/nowakelock
- memory-bank

### 🔄 Git References
- N/A

### 📏 Active Rules
- CodeProtection.mdc
- RIPERsigma1.0.3.mdc

## 📡 Context Status

### 🟢 Active
- WakelockHook.kt - Core functionality for intercepting wakelocks
- XposedModule.kt - Entry point for Xposed framework integration
- SystemPatterns.md - Architecture understanding
- TechContext.md - Technical stack understanding

### 🟡 Partially Relevant
- README.md - Project overview but lacks technical details
- BasicApp.kt - Application initialization but minimal XPosed integration

### 🟣 Essential
- WakelockHook.handleWakeLockAcquire - Critical functionality
- WakelockHook.handleWakeLockRelease - Critical functionality
- XposedModule.handleLoadPackage - Entry point for all hooks

### 🔴 Deprecated
- N/A

## 🔍 Key Insights

1. **Core Architecture**:
   - Xposed module hooks into system services (PowerManagerService, etc.)
   - Different hook implementation based on Android version (24-30 vs 31+)
   - Four main hook types: Wakelock, Alarm, Service, Settings

2. **Wakelock Control Logic**:
   - Intercepts wakelock acquisition and release
   - Uses multi-layered decision strategy for blocking
   - Tracks wakelock statistics and duration

3. **Decision Flow**:
   - System triggers wakelock/alarm/service
   - NoWakeLock intercepts via Xposed
   - Rules applied based on app package, name, pattern
   - Action taken (allow/block)
   - Event recorded

4. **Data Management**:
   - XpNSP class appears to manage configuration settings
   - XpRecord class handles recording events

## 📋 Next Steps

1. Understand the remaining hook implementations (AlarmHook, ServiceHook)
2. Explore the data management layer (repositories, database)
3. Examine UI implementation and how it interacts with the hooks
4. Document protection strategies for critical code sections
5. Identify optimization opportunities 