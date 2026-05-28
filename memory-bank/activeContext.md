# σ₄: Active Context
*v1.1 | Updated: 2026-05-27*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

## 🔮 Current Focus

### 1. Shizuku Integration & Rootless Support
We have successfully implemented Shizuku support as a fallback for users without root access.
- **Problem**: Previously, Xposed/Root was strictly required, heavily limiting the user base.
- **Solution**: 
  - Created `ShizukuManager`, `ShizukuParser`, `ShizukuDataCollector`, and `ShizukuMonitorService`.
  - Used `dumpsys power`, `dumpsys alarm`, and `dumpsys activity services` to parse data rootlessly.
  - Used `cmd appops` and `am force-stop` as rootless alternatives to intercepting wake locks and services.

### 2. Samsung OneUI Compatibility
- **Problem**: `SettingsProviderHook` failed on Samsung devices due to varying method signatures for `call` in the Android `ContentProvider`.
- **Solution**: Dynamically hooked all matching `call` methods regardless of their parameter count (3, 4, or 5 arguments). This ensures the app works flawlessly on Samsung OneUI and other heavily customized OEMs.

### 3. Critical IPC Data Wipe Bug Fix
- **Problem**: When Shizuku disconnected (or any IPC deserialization error occurred in `XProvider` queries), the app caught the exception and aggressively commanded a `ClearData` operation, wiping the user's entire local database (scores, times, and counts).
- **Solution**: Removed the destructive `ClearData` fallback from `AppDasAR.kt` and `DARepositoryImpl.kt`. The app now gracefully logs the IPC error and skips the sync, preserving all user data.

### 4. DAsScreen TopAppBar Refresh Button Fix
- **Problem**: The refresh button in the TopAppBar on DAsScreen (Wakelock, Alarm, Service screens) wasn't triggering actual data refreshes.
- **Solution**: Added event passing (`onTopAppBarEvent`) through the NavGraph to `DAsScreen`, correctly linking the RefreshClicked event to `viewModel.refreshData()`.

### 5. App Performance & Data Loading Optimization
- **Optimization**: Implemented a unified `triggerDataLoad` mechanism with a 300ms debounce.
- **Flow Optimization**: Applied `conflate`, `distinctUntilChanged`, and `debounce` to the Flow chains.
- **Memory Cache**: Implemented a 30-second TTL memory cache to massively reduce database access frequency.

### 6. AppDetailScreen State Persistence
- **Problem**: Navigating from AppDetailScreen to DADetail and back caused tab states to reset.
- **Solution**: Replaced standard `remember` with `rememberSaveable` for UI state and updated navigation config with `launchSingleTop=true` and `restoreState=true`.

### 7. Module Detection & UI Improvements
- **Implementation**: Comprehensive detection for Module Active status, Hook status, and Configuration Path validity.
- **Shizuku**: Added Shizuku active status to the ModuleCheckScreen.

### 8. Xposed Hook System Refactoring
- **Unified Strategy**: Refactored Wakelock, Alarm, and Service hooks to dynamically discover parameter positions instead of hardcoding them by Android version.

## 🔄 Recent Changes
- [Change₆₁] 2026-05-27 ⟶ Added Shizuku support and Samsung OneUI compatibility.
- [Change₆₂] 2026-05-27 ⟶ Fixed critical data wipe bug caused by IPC deserialization exceptions.
- [Change₆₀] 2025-05-24 ⟶ Fixed DAsScreen TopAppBar refresh button unresponsiveness.
- [Change₅₉] 2025-05-20 ⟶ Implemented unified data loading trigger and Flow chain optimization.

## 🔄 Next Steps
- [Step₁₉] Test ServiceHook adaptive extraction on Android 16 devices, High
- [Step₁₇] Resolve inconsistencies between wakelock countTime and utility display time, Medium
- [Step₂₀] Monitor Shizuku battery impact and optimize polling frequency, High

## 🤔 Active Decisions
- [Decision₃₆] ✅ ⟶ Implemented Shizuku polling via a Foreground Service to allow rootless monitoring.
- [Decision₃₇] ✅ ⟶ Dropped the destructive `ClearData` fallback on `getSerializable` exceptions to prevent accidental user data loss.
- [Decision₃₄] ✅ ⟶ Applied debounce mechanisms and memory caching to solve multi-data load issues and UI lag.
- [Decision₃₃] ✅ ⟶ For AppDetailScreen state persistence, adopted `rememberSaveable` and navigation config improvements to retain selected tabs.
- [Decision₃₀] ✅ ⟶ Implemented comprehensive module detection covering Shizuku status alongside Xposed.

## 📎 Context References
- 💻 `ShizukuRestricter.kt`, `ShizukuManager.kt`, `ShizukuDataCollector.kt`
- 💻 `SettingsProviderHook.kt`
- 💻 `AppDasAR.kt`, `DARepositoryImpl.kt`

## 🔬 Research Findings
### IPC Deserialization Bug
1. **Description**: Scores suddenly reset to zero upon Shizuku disconnect or Xposed query failure.
2. **Root Cause**: `getSerializable` throws an Exception when the IPC binder dies. The catch block maliciously executed `ProviderMethod.ClearData`.
3. **Resolution**: Catch block now simply logs the exception, allowing the app to preserve the last known state.

### SettingsProviderHook (Samsung Compatibility)
1. **Description**: ContentProvider calls failed on Samsung devices.
2. **Root Cause**: Samsung's internal `SettingsProvider` implements a differently signatured `call` method (5 arguments instead of 3).
3. **Resolution**: Hooked all methods named `call` dynamically and resolved parameters by offset.