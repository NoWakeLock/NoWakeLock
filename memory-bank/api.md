# API Documentation: WakeLock Detail Screen
*Created: 2023-08-05*
*Updated: 2023-08-05*

## Repository Interfaces

### DAInfoRepository
Provides access to detailed information about Device Automation items.

```kotlin
interface DAInfoRepository {
    /**
     * Get detailed information for a specific device automation item
     * @param id Identifier of the DA item
     * @param packageName Optional package name for more precise matching
     * @return Detailed information or null if not found
     */
    suspend fun getInfo(id: String, packageName: String?): DAInfoEntry?
    
    /**
     * Check if detailed information exists for a specific device automation item
     * @param id Identifier of the DA item
     * @param packageName Optional package name for more precise matching
     * @return True if information exists, false otherwise
     */
    suspend fun hasInfo(id: String, packageName: String?): Boolean
}
```

### DARepository (Existing)
The application already has a DARepository interface with the following relevant methods:

```kotlin
interface DARepository {
    // Get a specific DA item
    suspend fun getDAItem(id: String, packageName: String?, userId: Int): DAItem
    
    // Get recent events for a DA item
    suspend fun getRecentEvents(id: String, packageName: String?, userId: Int, startTime: Long): List<InfoEvent>
    
    // Update blocking setting
    suspend fun updateBlockingSetting(id: String, packageName: String?, userId: Int, enabled: Boolean)
    
    // Update condition settings
    suspend fun updateConditionSettings(id: String, packageName: String?, userId: Int, sleepOnly: Boolean, screenOffOnly: Boolean)
    
    // Update time interval
    suspend fun updateTimeInterval(id: String, packageName: String?, userId: Int, seconds: Int)
}
```

## ViewModel Methods

### DADetailViewModel
```kotlin
class DADetailViewModel(
    private val daRepository: DARepository,
    private val infoRepository: DAInfoRepository
) : ViewModel() {
    // UI state
    val uiState: StateFlow<DADetailState>
    
    // Settings state
    val settingsState: StateFlow<DASettingsState>
    
    // Load data for a specific DA item
    fun loadDADetail(daId: String, packageName: String?, userId: Int)
    
    // Update blocking setting
    fun updateBlockingSetting(enabled: Boolean)
    
    // Update condition settings
    fun updateConditionSettings(sleepOnly: Boolean, screenOffOnly: Boolean)
    
    // Update time interval setting
    fun updateTimeInterval(seconds: Int)
}
```

## Data Models

### DAInfoEntry
```kotlin
data class DAInfoEntry(
    val id: String,
    val name: String,
    val type: String, // "wakelock", "alarm", "service"
    val packageName: String?,
    val safeToBlock: String, // "safe", "risky", "dangerous"
    val description: String,
    val recommendation: String?,
    val warning: String?,
    val pattern: String? = null,
    val tags: List<String> = emptyList()
)
```

### DAStatistics
```kotlin
data class DAStatistics(
    val count: Int,
    val blockedCount: Int,
    val totalTime: Long, // in milliseconds
    val savedTime: Long  // in milliseconds
)
```

### TimePoint
```kotlin
data class TimePoint(
    val hour: Int,
    val total: Int,
    val blocked: Int,
    val label: String
)
```

### EventItem
```kotlin
data class EventItem(
    val id: Long,
    val timestamp: Long,
    val duration: Long,
    val isAllowed: Boolean,
    val formattedTime: String,
    val formattedDuration: String
)
```

### DASettingsState
```kotlin
data class DASettingsState(
    val isBlocked: Boolean = false,
    val sleepOnly: Boolean = false,
    val screenOffOnly: Boolean = false,
    val timeInterval: Int = 0
)
```

## Navigation

### Routes
```kotlin
object NavRoutes {
    // Existing routes
    const val APPS = "apps"
    const val WAKELOCKS = "wakelocks"
    const val ALARMS = "alarms"
    const val SERVICES = "services"
    const val SETTINGS = "settings"
    
    // New route for detail screen
    const val DA_DETAIL = "da_detail/{daId}/{packageName}"
    
    // Navigation helper function
    fun navigateToDADetail(daId: String, packageName: String) = 
        "da_detail/$daId/$packageName"
}
```

---

*This document captures API specifications and examples.*
