# σ₂: System Patterns
*v1.1 | Updated: 2026-05-27*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

## 🏛️ Architecture Overview
NoWakeLock Extended employs a modern Android application architecture, integrating the MVVM architectural pattern, Jetpack Compose UI framework, Koin dependency injection, and Room database. The project has clear separation of concerns, with the upper UI interacting with underlying data sources via ViewModels, while relying on the Xposed framework and Shizuku API to implement system-level hook and polling functionalities.

## 🧱 Component Structure
- [C₁] UI Layer ⟶ Compose-based screens, navigation, and components with MD3 styling
- [C₂] ViewModel Layer ⟶ Screen-specific ViewModels for data transformation and business logic
- [C₃] Repository Layer ⟶ Data access abstraction for wakelocks, alarms, and services
- [C₄] Database Layer ⟶ Room entities, DAOs, and converters for local storage
- [C₅] Xposed Layer ⟶ System-level hooks to monitor wakelocks, alarms, and services (Rooted)
- [C₆] Shizuku Layer ⟶ Rootless ADB-level monitoring via dumpsys parsing and AppOps command execution
- [C₇] Model Layer ⟶ Data classes representing wakelocks, alarms, and application information
- [C₈] Utility Layer ⟶ Helper functions, extensions, and shared tools
- [C₉] Visualization Layer ⟶ Custom Canvas-based charts and data visualization components
- [C₁₀] Settings Layer ⟶ Context-based state management with persistence and UI components

## 🔄 Design Patterns
- [P₁] MVVM ⟶ For separation of UI and business logic
- [P₂] Repository Pattern ⟶ For data access abstraction
- [P₃] Dependency Injection ⟶ Using Koin for service locator pattern
- [P₄] Observer Pattern ⟶ State management with Compose state and Flow
- [P₅] Factory Pattern ⟶ For creating instances of repositories and databases
- [P₆] Adapter Pattern ⟶ For transforming data between layers
- [P₇] Strategy Pattern ⟶ Different hooking strategies for different Android versions and root vs rootless platforms
- [P₈] Composition Pattern ⟶ Building complex UI components from smaller specialized components
- [P₉] Provider Pattern ⟶ Context-based state management for settings
- [P₁₀] Reducer Pattern ⟶ Structured state updates for complex settings

## 🔌 Key Interfaces
- [I₁] XposedModule ⟶ Entry point for Xposed framework integration
- [I₂] ShizukuManager & Parser ⟶ Entry points for rootless framework integration
- [I₃] Hook Implementations ⟶ Classes for monitoring specific system components
- [I₄] Repository Interfaces ⟶ Data access contracts
- [I₅] ViewModel Factories ⟶ For creating ViewModels with dependencies
- [I₆] Navigation Routes ⟶ For screen navigation
- [I₇] DAOs ⟶ Data Access Objects for database operations
- [I₈] UI Components ⟶ Reusable UI elements for consistency
- [I₉] Visualization Components ⟶ Chart and data display interfaces
- [I₁₀] Settings Provider ⟶ Context provider for settings management

## 🔐 Critical Implementation Paths
- [Path₁] Wakelock Detection ⟶ WakelockHook / ShizukuCollector → Record → Repository → Database
- [Path₂] Wakelock Display ⟶ Database → Repository → ViewModel → UI
- [Path₃] Wakelock Control ⟶ UI → ViewModel → Repository → XpNSP / ShizukuRestricter → System
- [Path₄] Alarm Detection ⟶ AlarmHook / ShizukuCollector → Record → Repository → Database
- [Path₅] Service Detection ⟶ ServiceHook / ShizukuCollector → Record → Repository → Database
- [Path₆] User Switching ⟶ UI → ViewModel → Repositories → Database Queries
- [Path₇] Data Backup ⟶ UI → ViewModel → Repository → Serialization → Storage
- [Path₈] Activity Visualization ⟶ Database → Repository → ViewModel → TimelineChart → Canvas

## 🧩 Core Architectural Insights
- [Insight₁] The application uses different hook implementations based on Android version for compatibility.
- [Insight₂] Multi-user support is implemented through userId parameters in database queries.
- [Insight₃] The app uses Koin for dependency injection with modular organization.
- [Insight₄] Edge-to-edge UI implementation uses the new enableEdgeToEdge() API.
- [Insight₅] Data visualization components use Canvas API for minimal dependencies.
- [Insight₆] Settings management uses a Context API approach for global state access.
- [Insight₇] The app utilizes a rootless fallback via Shizuku if Xposed is not active.

## 🖼️ UI Architecture
### Screen Organization
- Feature-based organization in separate screen packages
- Shared component library in dedicated components package
- Type-safe navigation using Kotlin serialization
- Material 3 component integration with system theming
- Screen state management using collectAsState with Flow

### Navigation System
- Bottom Navigation for main app navigation with tab-based structure
- Central NavHost in main app composable manages all navigation
- Type-Safe Parameters using serializable data classes
- Detail Navigation with back navigation and shared data
- State Preservation during tab switching using rememberSaveable

### Data Visualization
- Canvas-Based Charts using Compose Canvas API
- Material Design 3 Styling for visual consistency
- Responsive Layout adapting to different screen sizes
- Data Transformation in ViewModel before visualization
- Remember Optimization caching calculations

## 🧪 Testing Architecture

### Singleton Testing Challenges and Solutions
- **Singleton Reset Pattern**: Use reflection techniques to reset singleton states between tests, ensuring test isolation.
- **Test Class Splitting Strategy**: Break large test classes down into small, focused test classes to improve maintainability.
- **Test Suite Organization**: Use JUnit Suite to control the execution order of tests and resolve dependencies between tests.

### Test Lifecycle Patterns
- **Test Environment Preparation and Cleanup**: Ensure consistent test environments using @Before and @After.
- **Explicit State Resets**: Explicitly reset state inside test methods to ensure independence.
- **Fluent Assertion Syntax**: Use the Truth library for highly readable assertions.

## 🧩 Component Design Patterns

### Component Splitting and Composition
- **Splitting Large Components**: Decompose complex UI components into small, single-responsibility composable functions.
- **Component Naming Conventions**: Use descriptive naming reflecting functionality. Internal components use private modifiers.

### State Management Patterns
- **Centralized State**: Encapsulate component state using data classes (e.g., `TopAppBarUiState`).
- **Stateful/Stateless Separation**: Top-level components manage state, while child components are designed to be stateless and receive required data via parameters.
- **State Memoization**: Cache calculation results using `remember` and `rememberSaveable`.

### Utility Class Patterns
- **Function Encapsulation**: Extract generic logic into utility classes or helper functions (e.g., `RouteUtils`).
- **Namespace Organization**: Organize related helper functions in the same object or file.

### Styling and Theming Patterns
- **Centralized Style Management**: Extract styling properties into reusable functions (e.g., `standardTopAppBarColors()`).
- **Theme Consistency**: Components use MaterialTheme properties rather than hardcoded values.

## 🧠 Performance Optimization Patterns

### 🔄 Initialization-Time Caching

**Pattern**: Perform expensive operations once at initialization and cache the results for future use.

**Application in NoWakeLock**:
- **ServiceHook Parameter Caching**: The app caches parameter positions for hooked methods after first successful extraction.
  - Implemented with `AtomicReference<ServiceParamPositions?>` for thread safety.
  - Performs parameter extraction only once per device boot.
  - Significantly reduces CPU usage for frequently called service operations.

**Benefits**:
- Reduces repeated computation overhead.
- Improves response time for frequent operations.
- Decreases battery consumption.

## 🔄 Loading and Caching Patterns

### Universal Data Loading Pattern
**Characteristics**:
- Centralized management of all load requests via `triggerDataLoad()`.
- Debounce processing to cancel in-progress requests and prevent redundant queries.
- Separation of critical and non-critical data processing.

### Multi-tier Caching Pattern
**Characteristics**:
- In-memory cache using `Map<String, Pair<Data, Timestamp>>`.
- Cache key generation based on sort and filter params.
- 30-second TTL (Time to Live) expiration mechanism.
- LRU strategy keeping only the 20 most recent queries in memory.

### Reactive Data Flow Pattern
**Characteristics**:
- Uses Flow API to construct reactive data streams.
- Applies operators like `conflate()`, `distinctUntilChanged()`, and `debounce()` to optimize data handling from Database to UI state.
