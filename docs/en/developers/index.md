# Developer Documentation

Deep dive into NoWakeLock's technical implementation and contribute code to the project.

## Documentation Navigation

### 🏗️ Architecture Design
- [System Overview](architecture/overview.md) - Overall architecture and design concepts
- [Xposed Hooks](architecture/xposed-hooks.md) - Hook system implementation
- [Data Flow Design](architecture/data-flow.md) - Data flow within the system
- [Database Design](architecture/database.md) - Data storage architecture

### ⚙️ Implementation Details
- [Hook Details](implementation/hook-details.md) - Specific Hook implementations
- [Counter System](implementation/counter-system.md) - Statistics calculation mechanism
- [Inter-Process Communication](implementation/ipc.md) - Module-App communication

### 📚 API Reference
- [ContentProvider](api/content-provider.md) - Data access interface
- [Internal API](api/internal-api.md) - Module internal interfaces

## Quick Start

### Environment Requirements
- **Android Studio** - Arctic Fox or newer
- **JDK** - 17 or newer
- **Android SDK** - API 24-35
- **Git** - Version control tool

### Get Source Code
```bash
git clone https://github.com/NoWakeLock/NoWakeLock.git
cd NoWakeLock
git checkout dev
```

### Build Project
```bash
# Install dependencies
./gradlew clean

# Build Debug version
./gradlew assembleDebug

# Run tests
./gradlew test
```

## Technology Stack

### Core Technologies
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI framework
- **Room** - Database abstraction layer
- **Coroutines** - Asynchronous programming
- **Flow** - Reactive data streams

### Xposed Integration
- **LSPosed API** - Primary Hook framework
- **EdXposed Compatibility** - Backward compatibility support
- **Reflection Mechanism** - Cross-version API adaptation

### Dependency Injection
- **Koin** - Lightweight DI framework
- **ViewModel** - UI state management
- **Repository Pattern** - Data access abstraction

## Core Modules

### XposedHook Module
```
xposedhook/
├── XposedModule.kt      # Module entry point
├── hook/               # Hook implementations
│   ├── WakelockHook.kt
│   ├── AlarmHook.kt
│   └── ServiceHook.kt
└── model/              # Data models
    └── XpNSP.kt
```

### Data Layer
```
data/
├── db/                 # Database
│   ├── AppDatabase.kt
│   ├── InfoDatabase.kt
│   └── entity/
├── repository/         # Data repositories
└── counter/           # Counter system
```

### UI Layer
```
ui/
├── screens/           # Screen components
├── components/        # Common components
├── theme/            # Theme styling
└── navigation/       # Navigation logic
```

## Development Workflow

### Feature Development
1. **Requirements Analysis** - Define functional requirements and technical approach
2. **Branch Creation** - Create feature branch from `dev` branch
3. **Code Implementation** - Follow coding standards to implement functionality
4. **Unit Testing** - Write and run test cases
5. **Integration Testing** - Test on real devices
6. **Code Review** - Submit Pull Request
7. **Merge Release** - Merge to `dev` branch

### Bug Fixing
1. **Issue Reproduction** - Confirm bug reproduction steps
2. **Root Cause Analysis** - Analyze the root cause of the issue
3. **Fix Implementation** - Write minimal fix code
4. **Regression Testing** - Ensure fix doesn't affect other functionality
5. **Release Deployment** - Choose release timing based on severity

## Code Architecture

### MVVM Architecture
```kotlin
// ViewModel
class WakelocksViewModel(
    private val repository: WakelockRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WakelocksUiState())
    val uiState: StateFlow<WakelocksUiState> = _uiState.asStateFlow()
    
    fun loadWakelocks() {
        viewModelScope.launch {
            repository.getWakelocks()
                .collect { wakelocks ->
                    _uiState.update { it.copy(wakelocks = wakelocks) }
                }
        }
    }
}

// Compose UI
@Composable
fun WakelocksScreen(viewModel: WakelocksViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn {
        items(uiState.wakelocks) { wakelock ->
            WakelockItem(wakelock = wakelock)
        }
    }
}
```

### Repository Pattern
```kotlin
interface WakelockRepository {
    fun getWakelocks(): Flow<List<WakelockInfo>>
    suspend fun updateWakelock(wakelock: WakelockInfo)
}

class WakelockRepositoryImpl(
    private val dao: WakelockDao,
    private val xpProvider: XProvider
) : WakelockRepository {
    override fun getWakelocks(): Flow<List<WakelockInfo>> {
        return dao.getAllWakelocks()
            .map { entities -> entities.map { it.toDomain() } }
    }
}
```

### Hook Implementation Pattern
```kotlin
object WakelockHook {
    fun hookWakeLocks(lpparam: LoadPackageParam) {
        findAndHookMethod(
            PowerManagerService::class.java,
            "acquireWakeLockInternal",
            *parameterTypes
        ) { param ->
            val result = processWakeLockAcquire(param.args)
            if (result.shouldBlock) {
                param.result = null // Intercept call
                return@findAndHookMethod
            }
            // Continue original call
        }
    }
}
```

## Testing Strategy

### Unit Testing
- **ViewModel Testing** - Business logic testing
- **Repository Testing** - Data layer testing
- **Utility Testing** - Helper function testing

### Integration Testing
- **Database Testing** - Room database operations
- **Hook Testing** - Xposed Hook functionality
- **UI Testing** - Compose interface testing

### Device Testing
- **Compatibility Testing** - Multi-version Android devices
- **Performance Testing** - Memory, CPU, battery consumption
- **Stability Testing** - Long-running tests

## Release Process

### Version Management
- **Major Version** - Major feature updates
- **Minor Version** - New feature additions
- **Patch Version** - Bug fixes

### Branching Strategy
- **master** - Stable version
- **dev** - Development version
- **feature/*** - Feature branches
- **hotfix/*** - Emergency fixes

### CI/CD Pipeline
```yaml
# GitHub Actions workflow
name: Build and Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Build APK
        run: ./gradlew assembleDebug
```

## Debugging Tips

### Xposed Debugging
```kotlin
// Use XposedBridge.log for debug output
XposedBridge.log("NoWakeLock: Hook executed")

// Use conditional compilation for debug code
if (BuildConfig.DEBUG) {
    XposedBridge.log("Debug info: ${param.args}")
}
```

### Log Analysis
```bash
# Filter NoWakeLock logs
adb logcat | grep -i nowakelock

# Monitor performance metrics
adb shell dumpsys meminfo com.js.nowakelock
adb shell top | grep nowakelock
```

## Community Contribution

### Ways to Participate
- **Code Contribution** - Feature development and bug fixes
- **Documentation Contribution** - Improve documentation and tutorials
- **Testing Contribution** - Device compatibility testing
- **Translation Contribution** - Multi-language support

### Communication Channels
- **GitHub Issues** - Bug reports and feature requests
- **GitHub Discussions** - Technical discussions and idea exchange
- **Telegram** - Real-time communication and quick support
- **Discord** - In-depth technical discussions

### Code Review
All contributions require code review:
- Code quality check
- Security assessment
- Performance impact analysis
- Compatibility verification

!!! info "Developer Agreement"
    Contributing code implies agreement with the project's open source license (GPL v3.0) and contributor agreement.

!!! tip "Newcomer Friendly"
    The project welcomes newcomer contributors. We have tasks marked as `good-first-issue` for beginners to get started.