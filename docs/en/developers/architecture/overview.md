# System Overview

NoWakeLock adopts modern Android development architecture, combining Xposed framework's system-level Hook capabilities with Jetpack Compose's declarative UI design.

## Overall Architecture

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Android System                        │
├─────────────────────────────────────────────────────────┤
│  PowerManagerService │ AlarmManagerService │ ActiveServices │
│         ↓ Hook             ↓ Hook               ↓ Hook     │
├─────────────────────────────────────────────────────────┤
│                 Xposed Module Layer                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │WakelockHook │ │ AlarmHook   │ │ ServiceHook │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
├─────────────────────────────────────────────────────────┤
│                   Application Layer                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │ Presentation│ │  Domain     │ │    Data     │        │
│  │   (UI)      │ │ (Business)  │ │ (Storage)   │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
└─────────────────────────────────────────────────────────┘
```

## Technology Stack

### Core Technologies
- **Kotlin** 1.9.25 - Primary programming language
- **Jetpack Compose** 2025.04.01 - Declarative UI framework
- **Room** 2.7.1 - Database ORM framework
- **Coroutines** - Asynchronous programming
- **Flow** - Reactive data streams

### Xposed Integration
- **Xposed API** 82 - System-level Hook framework
- **LSPosed** - Primary target framework
- **EdXposed** - Backward compatibility support
- **Reflection Mechanism** - Cross-version API adaptation

### Dependency Injection
- **Koin** 4.0.4 - Lightweight DI framework
- **Modular Configuration** - Feature-based injection grouping
- **ViewModel Management** - Automatic lifecycle binding

## Architecture Layers

### 1. Xposed Module Layer
```kotlin
// Entry point
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                WakelockHook.hook(lpparam)
                AlarmHook.hook(lpparam)
                ServiceHook.hook(lpparam)
            }
        }
    }
}
```

**Responsibilities**:
- System service Hook interception
- Cross-process data communication
- Version compatibility handling

### 2. Data Layer

#### Dual Database Architecture
```kotlin
// Main business database
@Database(
    entities = [AppInfo::class, WakelockRule::class],
    version = 13
)
abstract class AppDatabase : RoomDatabase()

// Event logging database  
@Database(
    entities = [InfoEvent::class],
    version = 12
)
abstract class InfoDatabase : RoomDatabase()
```

**Responsibilities**:
- Application information management
- Event data recording
- Rule configuration storage
- User preference settings

#### Repository Pattern
```kotlin
interface DARepository {
    fun getApps(userId: Int): Flow<List<AppDas>>
    suspend fun updateRule(rule: Rule)
}

class DARepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val xProvider: XProvider
) : DARepository {
    // Implement data access logic
}
```

**Features**:
- Unified data access interface
- Local database + Xposed data source
- Reactive data streams

### 3. Domain Layer

#### ViewModel Architecture
```kotlin
class DAsViewModel(
    private val repository: DARepository,
    private val userRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DAsUiState())
    val uiState: StateFlow<DAsUiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            repository.getApps()
                .distinctUntilChanged()
                .collect { apps ->
                    _uiState.update { it.copy(apps = apps) }
                }
        }
    }
}
```

**Features**:
- MVVM architecture pattern
- StateFlow state management
- Reactive data binding

### 4. Presentation Layer

#### Compose UI Architecture
```kotlin
@Composable
fun NoWakeLockApp() {
    val navController = rememberNavController()
    
    NoWakeLockTheme {
        NavHost(navController = navController) {
            composable<Apps> { AppsScreen() }
            composable<Wakelocks> { WakelocksScreen() }
            composable<Services> { ServicesScreen() }
        }
    }
}
```

**Features**:
- Declarative UI components
- Type-safe navigation
- Material Design 3

## Core Features

### 1. Multi-User Support
```kotlin
class UserManager {
    fun getCurrentUsers(): List<User> {
        return UserManagerService.getUsers()
    }
    
    fun switchUser(userId: Int) {
        // Switch user context
    }
}
```

### 2. Version Compatibility
```kotlin
object VersionCompat {
    fun getParameterIndices(method: Method): IntArray {
        return when (Build.VERSION.SDK_INT) {
            in 24..28 -> intArrayOf(0, 1, 2)
            in 29..30 -> intArrayOf(1, 2, 3)
            else -> intArrayOf(2, 3, 4)
        }
    }
}
```

### 3. Performance Optimization
- **Parameter Position Caching** - Reduce reflection overhead
- **Flow distinctUntilChanged** - Avoid duplicate updates
- **Database Indexing** - Optimize query performance
- **Lazy Loading** - Load data on demand

## Data Flow Design

### Event Processing Flow
```
System Call → Hook Intercept → Rule Match → Execute Action → Log Event → Update UI
    ↓            ↓              ↓            ↓              ↓          ↓
PowerManager → WakelockHook → RuleEngine → Block/Allow → InfoEvent → Flow Update
```

### State Management
```kotlin
data class DAsUiState(
    val apps: List<AppDas> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterOption: FilterOption = FilterOption.ALL,
    val sortOption: SortOption = SortOption.NAME
)
```

## Communication Mechanisms

### 1. Xposed ↔ Application Communication
- **XProvider** - ContentProvider cross-process communication
- **SharedPreferences** - Configuration data sharing
- **File System** - Temporary data exchange

### 2. Inter-Component Communication
- **Repository Pattern** - Data layer abstraction
- **StateFlow** - Reactive state sharing
- **Navigation** - Inter-screen parameter passing

## Extension Points

### 1. Adding New Hook Types
```kotlin
// Create new Hook class
object NewFeatureHook {
    fun hook(lpparam: LoadPackageParam) {
        // Hook implementation
    }
}

// Register in XposedModule
NewFeatureHook.hook(lpparam)
```

### 2. Adding New UI Screens
```kotlin
// Add new route
@Serializable
data class NewFeature(val param: String = "")

// Add navigation and screen
composable<NewFeature> { NewFeatureScreen() }
```

### 3. Integrating New Data Sources
```kotlin
// Extend Repository interface
interface ExtendedRepository : DARepository {
    fun getNewData(): Flow<List<NewData>>
}
```

## Design Principles

### 1. Single Responsibility
Each class and module has clear responsibility boundaries, making them easy to maintain and test.

### 2. Dependency Inversion
High-level modules don't depend on low-level modules; both depend on abstract interfaces.

### 3. Open-Closed Principle
Open for extension, closed for modification, making it easy to add new features.

### 4. Reactive Design
Use Flow and StateFlow to implement reactive data streams, ensuring UI-data synchronization.

!!! info "Architecture Advantages"
    This layered architecture design enables NoWakeLock to handle complex system-level operations while maintaining good code organization and maintainability.

!!! warning "Important Notes"
    System-level Hook modifications require careful handling of version compatibility. It's recommended to thoroughly test across multiple Android versions.