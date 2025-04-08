# Patterns: WakeLock Detail Screen
*Created: 2023-08-05*
*Updated: 2023-08-05*

## Code Patterns

### StateFlow-based UI State
The screen follows the pattern of using immutable state with StateFlow:

```kotlin
// In ViewModel
private val _uiState = MutableStateFlow<DADetailState>(DADetailState.Loading)
val uiState: StateFlow<DADetailState> = _uiState.asStateFlow()

// In UI
val state by viewModel.uiState.collectAsState()
```

### Sealed Class for State Representation
Using sealed classes to represent different UI states:

```kotlin
sealed class DADetailState {
    object Loading : DADetailState()
    data class Success(...) : DADetailState()
    data class Error(...) : DADetailState()
}
```

### Repository Pattern
Following the existing codebase structure with repositories for data access:

```kotlin
interface DAInfoRepository {
    suspend fun getInfo(id: String, packageName: String?): DAInfoEntry?
    suspend fun hasInfo(id: String, packageName: String?): Boolean
}

class DAInfoRepositoryImpl(private val context: Context) : DAInfoRepository {
    // Implementation details
}
```

### Lazy Loading Pattern
Using lazy initialization for data that should be loaded only when needed:

```kotlin
private val infoData by lazy { loadInfoData() }
```

### Card-based UI Structure
Using cards to organize different sections of information:

```kotlin
LazyColumn {
    item { HeaderSection(...) }
    item { StatisticsCard(...) }
    item { InfoCard(...) }
    item { SettingsCard(...) }
    item { TimelineCard(...) }
    item { RecentActivitiesCard(...) }
}
```

## UI Patterns

### Material Design 3 Components
Using MD3 components consistently throughout the UI:

- **ElevatedCard**: For all card containers
- **Switch**: For blocking and condition toggles
- **OutlinedTextField**: For time interval input
- **Icons**: Material icons with appropriate colors

### Statistics Card Pattern
Consistent pattern for displaying statistics in a row:

```kotlin
Row(horizontalArrangement = Arrangement.SpaceEvenly) {
    StatItem(value = "47", label = "次数")
    VerticalDivider()
    StatItem(value = "12", label = "已阻止")
    VerticalDivider()
    StatItem(value = "2h 15m", label = "总时间")
    VerticalDivider()
    StatItem(value = "35m", label = "节省时间")
}
```

### Icon + Text Pattern
Consistent pattern for displaying icon with text:

```kotlin
Row(verticalAlignment = Alignment.Top) {
    Icon(...)
    Spacer(modifier = Modifier.width(8.dp))
    Text(...)
}
```

### Timeline Visualization Pattern
Consistent approach for timeline visualization:

- Gray bars for total activity
- Purple bars for blocked activity
- Bottom labels for time points
- Proportional height based on activity count

### Status Indicator Pattern
For recent activities, using color coded indicators:

- Green left border for allowed activities
- Red left border for blocked activities (if any)

## Data Patterns

### Matching Algorithm Pattern
Three-tier matching approach:

1. Exact match (ID + package)
2. ID-only match
3. Pattern-based match

### Time Formatting Pattern
Consistent time formatting:

- Duration: "2h 15m"
- Timestamp: "Today, 10:23 AM"
- Short time: "12AM", "4PM"

---

*This document captures recurring patterns and conventions.*
