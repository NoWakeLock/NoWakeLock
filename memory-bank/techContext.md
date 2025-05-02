# σ₃: Technical Context
*v1.0 | Created: 2025-04-15 | Updated: 2025-04-30*
*Π: 🏗️DEVELOPMENT | Ω: 🔍R*

## 🛠️ Technology Stack
- 🖥️ Frontend: Jetpack Compose, Material 3, Compose Navigation
- 🗄️ Backend: Kotlin, Android SDK, Xposed Framework
- 📊 Database: Room Persistence Library
- 🧪 Testing: JUnit, Espresso
- 🚀 Deployment: Google Play Store, F-Droid
- 🔧 Settings: Context API-based state management, LocalStorage persistence

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
- [D₁₄] React Hook Form ⟶ Version 7.x, For form validation in settings
- [D₁₅] Lodash ⟶ For nested object access in settings system
- [D₁₆] CSS-in-JS ⟶ For themed styling in settings components

## 🚧 Technical Constraints
- [T₁] Root Access ⟶ Required for complete functionality with Xposed
- [T₂] Android Version Compatibility ⟶ Minimum SDK 24 (Android 7.0), Target SDK 35
- [T₃] Battery Usage ⟶ Monitoring must have minimal impact on battery
- [T₄] Permission Model ⟶ Requires QUERY_ALL_PACKAGES permission
- [T₅] Xposed Module Scope ⟶ Limited to specific packages defined in scopes array
- [T₆] Multi-user Support ⟶ Must handle data for different Android user profiles
- [T₇] Edge-to-edge UI ⟶ Modern UI requires proper edge-to-edge handling
- [T₈] Settings Persistence ⟶ LocalStorage size limitations (5-10MB based on browser)
- [T₉] Theme Consistency ⟶ All components must respect theme settings
- [T₁₀] Settings Version Control ⟶ Need versioning mechanism for settings format changes

## 🔧 Tool Usage Patterns
- [Tool₁] Xposed Framework ⟶ For monitoring system wakelocks, alarms, and services
- [Tool₂] Android Debug Bridge ⟶ For development and testing
- [Tool₃] Battery Stats ⟶ For measuring app impact
- [Tool₄] System API Hooks ⟶ For intercepting wakelock, alarm, and service calls
- [Tool₅] Compose Tooling ⟶ UI previews and testing
- [Tool₆] Material 3 Components ⟶ For consistent, modern UI
- [Tool₇] Data Serialization ⟶ For backup/restore functionality
- [Tool₈] Context API ⟶ For state management across component trees
- [Tool₉] Reducer Pattern ⟶ For complex state updates in settings
- [Tool₁₀] Component Composition ⟶ For building flexible, maintainable UI

## 📝 SavedStateHandle Integration

### Parameter Management
- SavedStateHandle provides automatic state restoration during configuration changes and process death
- All ViewModels now use SavedStateHandle to store and retrieve screen parameters
- Parameter constant classes define string keys for all SavedStateHandle parameters
- Screen parameters are passed through navigation arguments automatically
- Custom properties with get/set accessors simplify interaction with SavedStateHandle

### Navigation Parameter Flow
- Navigation parameters are passed via route parameters (composable<Type>)
- Parameters are automatically stored in SavedStateHandle by the Compose Navigation framework
- ViewModels access these parameters through SavedStateHandle
- UI components observe ViewModel state that incorporates these parameters
- LaunchedEffect blocks synchronize UI state with ViewModel state

## 🛠️ Navigation System Architecture

### Type-Based Navigation
- Route classes (Apps, Wakelocks, Alarms, Services) define type-safe navigation targets
- composable<Type> used for type-safe navigation for main screens
- String-based navigation retained for Settings screen
- Navigation parameters like packageName and userId automatically passed through SavedStateHandle
- Hybrid approach combines benefits of type-safety with backwards compatibility

### Route Detection
- Type-based routes generate complex path strings ("com.js.nowakelock.ui.navigation.Apps?parameters...")
- TopAppBar uses pattern matching (route.contains()) instead of exact matching (route == NavRoutes.APPS)
- BottomNavBar uses special handling for Settings screen string navigation
- NavGraph preserves SavedStateHandle functionality while supporting mixed navigation approach

## 🧩 Settings Architecture

### Core Components
- [SC₁] Settings Provider ⟶ Top-level state container with context provider
- [SC₂] Settings Reducer ⟶ State management through action creators
- [SC₃] Settings Hook ⟶ Custom hook for accessing settings
- [SC₄] Settings UI Kit ⟶ Reusable UI components for settings

### Data Flow
- [DF₁] User Interaction ⟶ Component UI events trigger actions
- [DF₂] Action Dispatching ⟶ Actions passed to reducer for state updates
- [DF₃] State Updates ⟶ Reducer processes actions and updates state
- [DF₄] UI Updates ⟶ Components rerender with new state
- [DF₅] Persistence ⟶ State changes saved to LocalStorage
- [DF₆] Restoration ⟶ State loaded from LocalStorage on initialization

### UI Components
- [UI₁] Input Fields ⟶ For text and numeric settings
- [UI₂] Toggle Switches ⟶ For boolean settings
- [UI₃] Dropdown Selects ⟶ For option selection
- [UI₄] Color Pickers ⟶ For theme customization
- [UI₅] Section Headers ⟶ For grouping related settings
- [UI₆] Search Field ⟶ For filtering settings
- [UI₇] Validation Messages ⟶ For input feedback

## ⏱️ Wakelock Timing Implementation

### Core Components
- [WC₁] WakelockCounter ⟶ Tracks individual wakelock activity and calculates non-overlapping duration
- [WC₂] WakelockRegistry ⟶ Manages all wakelocks and provides a unified interface
- [WC₃] XProvider Integration ⟶ Modified to use Registry for accurate calculations

### Thread Safety Approach
- [TS₁] AtomicInteger ⟶ For thread-safe counter operations
- [TS₂] @Volatile annotation ⟶ For ensuring visibility of timestamp changes across threads
- [TS₃] ConcurrentHashMap ⟶ For thread-safe wakelock registry storage

### Timing Algorithm
- [TA₁] State Tracking ⟶ Counter represents currently active instances of a wakelock
- [TA₂] Timestamp Management ⟶ Updates on transitions from 0→1 and 1→0
- [TA₃] Accumulation Logic ⟶ Duration added only when all instances are released
- [TA₄] Overlapping Prevention ⟶ Single timestamp for acquisition, regardless of count
- [TA₅] Edge Cases ⟶ Handles invalid states like negative counts

### Performance Considerations
- [PC₁] In-Memory Data Structure ⟶ Avoids database operations for timing
- [PC₂] Low Overhead Operations ⟶ Minimal impact on system performance
- [PC₃] No Serialization ⟶ Registry data is transient, not persisted across reboots
- [PC₄] Minimal Synchronization ⟶ Atomics and volatile references instead of locks

### Implementation Pattern
```kotlin
class WakelockCounter {
    // Thread-safe counter for active wakelocks
    private val activeCount = AtomicInteger(0)
    
    // Timestamp of when the counter transitioned from 0 to positive
    @Volatile
    private var activeTimestamp: Long = 0
    
    // Total accumulated non-overlapping time
    @Volatile
    private var accumulatedDuration: Long = 0
    
    // Called when a wakelock is acquired
    fun increment(): Int {
        val newCount = activeCount.incrementAndGet()
        
        // Only update timestamp on transition from 0 to 1
        if (newCount == 1) {
            activeTimestamp = System.currentTimeMillis()
        }
        
        return newCount
    }
    
    // Called when a wakelock is released
    fun decrement(): Int {
        // Get current timestamp before decrementing to ensure accurate duration
        val currentTime = System.currentTimeMillis()
        val newCount = activeCount.decrementAndGet()
        
        // When count drops to 0, add duration to accumulated total
        if (newCount == 0 && activeTimestamp > 0) {
            accumulatedDuration += (currentTime - activeTimestamp)
        }
        
        return newCount
    }
    
    // Get total duration (accumulated + ongoing if active)
    fun getTotalDuration(): Long {
        val currentCount = activeCount.get()
        
        // Return accumulated duration if no active wakelocks
        if (currentCount <= 0 || activeTimestamp <= 0) {
            return accumulatedDuration
        }
        
        // Add current active duration to accumulated total
        return accumulatedDuration + (System.currentTimeMillis() - activeTimestamp)
    }
}
```

## 🖥️ UI Architecture Insights

### Component Structure
- **NoWakeLockApp.kt** serves as the main composition point, establishing theming and navigation
- **Navigation system** uses Jetpack Navigation with type-safe route parameters
- **Screen components** follow a standard pattern:
  - `*Screen.kt` - Main container with business logic connections
  - `*Content.kt` - UI structure and composition
  - `*State.kt` - Data structures for UI state representation
  - `components/` - Reusable UI elements specific to the screen
- **DADetailScreen** represents a typical complex screen with multiple sections:
  - Header with app/service information
  - Statistics card with key metrics
  - About section with description
  - Settings section with toggles
  - Activity timeline visualization
- **AppDetailScreen** planned structure follows a tabbed design:
  - App header with icon, name and core info
  - Statistics summary row with key metrics
  - TabRow with HorizontalPager for tab content
  - Four tabs: App, Wakelocks, Alarms, Services
  - Collapsible header that shrinks when scrolling
- **Global TopAppBar** handles navigation and context-specific actions
  - TopAppBarEvent system allows nested screens to modify title

### Visual Language
- **Material Design 3** principles guide UI design
- **Card-based UI** groups related information
  - ElevatedCard should be used instead of Card for consistent elevation
- **Consistent spacing** uses 16dp between cards, 16dp internal padding
- **MD3 color system** leverages dynamic themes and semantic colors
- **Typography** follows MD3 type scale with appropriate weights

### UI Challenges

#### Card Styling Consistency
- Current implementation has inconsistent card styling
- Cards should use ElevatedCard with consistent elevation
- Padding should be managed consistently - card manages internal padding, parent manages spacing
- Dividers should be used sparingly, with proper alpha for subtlety

#### Timeline Chart Improvements
- Current implementation doesn't match design specifications
- Chart needs rounded corners on bars, better axis layout
- Time labels need optimization to avoid overlap
- Canvas API limitations require manual positioning calculations

#### Lazy Loading Tabs in Compose
- Issue identified with current lazy loading implementation in tabbed interfaces
- Original implementation showed loading indicator on first tab selection, actual content only on second selection
- Problem root cause: LaunchedEffect executes after composition, creating a timing issue
- Solution implemented using derivedStateOf to immediately include current tab in loaded tabs set:
  ```kotlin
  // Track persistent loaded tabs state
  val loadedTabs = remember { mutableStateOf(setOf(0)) }
  
  // Use derivedStateOf to immediately include current tab in loaded set
  val currentLoadedTabs by remember {
      derivedStateOf { 
          loadedTabs.value + selectedTabIndex 
      }
  }
  
  // Still persist loaded tabs for future recompositions
  LaunchedEffect(selectedTabIndex) {
      loadedTabs.value = loadedTabs.value + selectedTabIndex
  }
  ```
- This pattern ensures immediate UI response while preserving lazy loading benefits
- Important learning: Use derivedStateOf when UI needs to immediately react to state changes

#### Collapsible Header Implementation in Compose
- No direct equivalent to CollapsingToolbarLayout in Compose
- Implementation requires custom NestedScrollConnection to track scroll state
- TopAppBarScrollBehavior combined with graphicsLayer modifications can achieve collapsing effect
- Material 3 approach uses LargeTopAppBar with exitUntilCollapsedScrollBehavior()
- Animation timing and visual transitions need careful implementation

#### Tab Navigation
- Tabbed interfaces need coordination between TabRow selection and HorizontalPager
- Tabs require consistent styling following Material 3 guidelines
- Content caching needed to avoid recomposition when switching tabs
- State management for each tab must be handled appropriately
- LaunchedEffect with derivedStateOf ensures smooth tab transitions

## 📱 Mobile-Specific Considerations

### Multiple Screen Sizes
- UI must adapt to various screen sizes using Compose's layout system
- ListDetailPaneScaffold for master-detail pattern on larger screens
- Edge-to-edge UI requires proper inset handling

### Performance Considerations
- Recomposition optimization with remember, derivedStateOf and collectAsState
- Image loading with Coil should include crossfade and caching
- LazyList used for all long lists with proper key usage
- Canvas-based visualizations (TimelineChart) need optimization

## 🛠️ Technical Requirements

### Dependency Injection
- Koin is used for dependency injection throughout the application
- ViewModels are created using koinViewModel() in Compose
- AppDetailViewModel follows existing patterns with repository injection

### State Management
- UI state flows through ViewModel to Compose using StateFlow
- collectAsStateWithLifecycle used to respect Android lifecycle
- Shared ViewModels used for communication between related screens

### Architecture Pattern
- Application follows MVVM architecture:
  - Model: Repository pattern with Room database
  - View: Compose UI
  - ViewModel: State management and business logic with SavedStateHandle

## 🚀 MVVM Enhancement with SavedStateHandle

### ViewModel Improvements
- SavedStateHandle provides automatic state restoration across configuration changes
- Parameters are stored with string keys defined in constant classes
- VM properties provide type-safe access to SavedStateHandle values
- Default values ensure graceful handling of missing parameters
- Initial values are synced from navigation parameters or external configuration

### Type Safety Improvements
- Parameter constant classes (AppsScreenParams, DAsScreenParams) prevent typos
- Type-safe navigation with serialized classes ensures parameter consistency
- ViewModel property accessors provide compile-time type checking
- Navigation arguments are properly typed in route definitions

### Implementation Pattern
```kotlin
// Parameter constants
object DAsScreenParams {
    const val PACKAGE_NAME = "packageName"
    const val USER_ID = "userId"
    // ... other parameters
}

// ViewModel implementation
class DAsViewModel(
    repository: Repository,
    private val savedStateHandle: SavedStateHandle
) {
    // Type-safe accessor with default value
    var packageName: String?
        get() = savedStateHandle.get<String>(DAsScreenParams.PACKAGE_NAME)
        set(value) { savedStateHandle[DAsScreenParams.PACKAGE_NAME] = value }
        
    // ... UI state and business logic
}
``` 