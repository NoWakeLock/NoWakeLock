# σ₃: Technical Context
*v1.1 | Updated: 2026-05-27*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

## 🛠️ Technology Stack
- 🖥️ Frontend: Jetpack Compose, Material 3, Compose Navigation
- 🗄️ Backend: Kotlin, Android SDK, Xposed Framework, Shizuku API
- 📊 Database: Room Persistence Library
- 🧪 Testing: JUnit, Mockito, Truth
- 🚀 Deployment: GitHub Releases
- 🔧 Settings: Context API-based state management, LocalStorage persistence

## ⚙️ Development Environment
- [E₁] Android Studio ⟶ Latest version
- [E₂] Gradle 8.x ⟶ For build automation with Kotlin DSL
- [E₃] Git ⟶ For version control
- [E₄] GitHub Actions ⟶ For CI/CD
- [E₅] Rooted or Shizuku-enabled Android device/emulator ⟶ For testing hook/polling functionality
- [E₆] Android SDK 35 ⟶ Latest target SDK

## 📦 Dependencies
- [D₁] Kotlin Coroutines ⟶ For asynchronous programming
- [D₂] Jetpack Compose ⟶ For modern UI development
- [D₃] Room ⟶ For local database operations
- [D₄] Koin ⟶ For dependency injection
- [D₅] Xposed Framework ⟶ For system-level hooks
- [D₆] Coil ⟶ For app icon loading
- [D₇] Kotlinx Serialization ⟶ For type-safe navigation
- [D₈] Kotlinx Collections Immutable ⟶ For immutable collections
- [D₉] DataStore Preferences ⟶ For preferences storage
- [D₁₀] Navigation Compose ⟶ For navigation
- [D₁₁] Core Splashscreen ⟶ For splash screen
- [D₁₂] Material 3 ⟶ For modern UI components
- [D₁₃] ConstraintLayout Compose ⟶ For complex layouts
- [D₁₄] Shizuku API ⟶ For rootless ADB system interaction

## 🚧 Technical Constraints
- [T₁] System Access ⟶ Requires either Xposed Framework (root) OR Shizuku (ADB) to monitor/block activities.
- [T₂] Android Version Compatibility ⟶ Minimum SDK 24 (Android 7.0), Target SDK 35
- [T₃] Battery Usage ⟶ Monitoring must have minimal impact on battery (Xposed hooks are passive; Shizuku polling runs on an interval).
- [T₄] Permission Model ⟶ Requires QUERY_ALL_PACKAGES permission
- [T₅] Xposed Module Scope ⟶ Limited to specific packages defined in scopes array
- [T₆] Multi-user Support ⟶ Must handle data for different Android user profiles
- [T₇] Edge-to-edge UI ⟶ Modern UI requires proper edge-to-edge handling
- [T₈] Exception Handling ⟶ Must fail gracefully during IPC deserialization to avoid accidental database wipes.
- [T₉] Theme Consistency ⟶ All components must respect theme settings
- [T₁₀] Settings Version Control ⟶ Need versioning mechanism for settings format changes

## 🔧 Key Technical Components

### 📝 SavedStateHandle Integration
- SavedStateHandle provides automatic state restoration during configuration changes and process death
- Parameter constant classes define string keys for all SavedStateHandle parameters
- Screen parameters are passed through navigation arguments automatically
- Custom properties with get/set accessors simplify interaction with SavedStateHandle

**Implementation Pattern:**
```kotlin
// Parameter constant class
object AppsScreenParams {
    const val USER_ID = "userId"
    const val SEARCH_QUERY = "searchQuery"
    const val SORT_ORDER = "sortOrder"
}

// SavedStateHandle usage in ViewModel
class AppsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val appRepository: AppRepository
) : ViewModel() {
    // Type-safe accessor method
    var userId: Int
        get() = savedStateHandle.get<Int>(AppsScreenParams.USER_ID) ?: 0
        set(value) { savedStateHandle[AppsScreenParams.USER_ID] = value }
}
```

### 🛠️ Navigation System Architecture
- Type-based navigation using serializable data classes for type-safety
- Hybrid approach combining type-safety with string-based navigation for legacy screens
- Route detection using pattern matching for complex paths
- NavGraph preserves SavedStateHandle functionality while supporting mixed navigation

### ⏱️ Wakelock Timing Implementation
- Thread-safe counter operations using AtomicInteger 
- Timestamp visibility ensured with @Volatile annotation
- Non-overlapping duration calculation for accurate statistics
- Low overhead design with minimal synchronization

### 🚀 Data Loading and UI Performance Optimization
- Unified data loading trigger with debounce mechanism
- Flow chain optimization with conflate, distinctUntilChanged, and debounce operators
- In-memory caching with 30-second validity period
- Component lifecycle optimization with LaunchedEffect

### 🔍 Database Migration Strategy
- Multi-path migration for supporting version jumps
- Transaction protection ensuring atomic operations
- Table rebuilding approach for resolving index conflicts
- Comprehensive logging and exception handling

### 🧪 Testing Architecture 
- Singleton reset using reflection for reliable unit testing
- Test class separation for better focus and maintenance
- Test suite organization controlling execution order
- Before/After methods ensuring test isolation

### 📱 Xposed Hook System & Samsung Compatibility
- Unified hooking strategy across Android versions.
- **Samsung OneUI Adapter**: Hooks multiple `call` signatures within `SettingsProvider` to bypass OEM custom argument counts.
- Parameter position caching for performance optimization.
- Adaptive parameter extraction for compatibility.

### 🛡️ Shizuku Rootless System
- Uses standard ADB shell commands executed via the Shizuku Binder.
- Parses `dumpsys power`, `dumpsys alarm`, and `dumpsys activity services` using regex pattern matching inside `ShizukuParser`.
- Blocks applications by intercepting the `cmd appops set [pkg] WAKE_LOCK ignore` and forcing stops.
- Monitored continuously via `ShizukuMonitorService` running as a Foreground Service to prevent termination.

### 🔄 Boot Detection and Reset
- Reliable device reboot detection with SystemClock.elapsedRealtime()
- Database table reset on application restart after device reboot
- DataStore preferences for persistent timestamp storage

### 🛡️ IPC Resiliency and Safe Fallbacks
- ContentProvider IPC calls (Xposed/Shizuku) are resilient against unbinding.
- Exceptions during serialization no longer trigger `ClearData` on the host app's database. This prevents accidental stat wipes during disconnections.

## 🔮 Future Technical Considerations
- Further optimization of large list rendering with LazyColumn recycling
- Improved TypedRoute path handling for complex navigation requirements
- Enhanced unit test coverage for core components
- Migration from Gson to Kotlinx.Serialization for better Kotlin integration
- Standardization on Material 3 components across the entire UI
- Addressing hidden API access warnings in Room database implementation