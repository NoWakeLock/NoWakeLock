# Datenfluss-Design

Das Datenfluss-Design von NoWakeLock verwendet eine reaktive Architektur, die eine effiziente Koordination von Systemereignissen, Benutzeroperationen und UI-Updates gewährleistet.

## Datenfluss-Übersicht

### Gesamtes Datenflussdiagramm
```mermaid
graph TD
    A[Systemaufruf] --> B[Xposed Hook]
    B --> C[Regelmodul]
    C --> D{Aktion ausführen}
    D -->|Erlauben| E[System normal ausführen]
    D -->|Limitieren| F[Limitierungsparameter setzen]
    D -->|Abfangen| G[Ausführung blockieren]
    
    B --> H[Ereignisaufzeichnung]
    H --> I[XProvider]
    I --> J[Anwendungsdatenbank]
    J --> K[Repository]
    K --> L[ViewModel]
    L --> M[UI-Update]
    
    N[Benutzeroperation] --> O[ViewModel]
    O --> P[Repository]
    P --> Q[Datenbankupdate]
    Q --> R[Konfigurationssynchronisation]
    R --> S[Xposed-Modul]
```

## Datenschichtarchitektur

### Dual-Datenbank-Design
```kotlin
// Hauptgeschäftsdatenbank - Anwendungsinformationen und Konfiguration
@Database(
    entities = [
        AppInfo::class,
        WakelockRule::class,
        AlarmRule::class,
        ServiceRule::class
    ],
    version = 13,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
    abstract fun wakelockRuleDao(): WakelockRuleDao
    // ...andere DAOs
}

// Ereignisaufzeichnungsdatenbank - Laufzeitdaten
@Database(
    entities = [InfoEvent::class],
    version = 12,
    exportSchema = true
)
abstract class InfoDatabase : RoomDatabase() {
    abstract fun infoEventDao(): InfoEventDao
}
```

### Datenentitäts-Design
```kotlin
// Anwendungsinformations-Entität
@Entity(
    tableName = "appInfo",
    primaryKeys = ["packageName", "userId"],
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["userId"]),
        Index(value = ["system"])
    ]
)
data class AppInfo(
    var packageName: String = "",
    var uid: Int = 0,
    var label: String = "",
    var icon: Int = 0,
    var system: Boolean = false,
    var enabled: Boolean = false,
    var persistent: Boolean = false,
    var processName: String = "",
    var userId: Int = 0,
)

// Ereignisaufzeichnungs-Entität
@Entity(
    tableName = "info_event",
    indices = [
        Index(value = ["packageName", "userId"]),
        Index(value = ["type"]),
        Index(value = ["startTime"]),
        Index(value = ["isBlocked"])
    ]
)
data class InfoEvent(
    @PrimaryKey var instanceId: String = "",
    var name: String = "",
    var type: Type = Type.UnKnow,
    var packageName: String = "",
    var userId: Int = 0,
    var startTime: Long = 0,
    var endTime: Long? = null,
    var isBlocked: Boolean = false,
) {
    enum class Type {
        UnKnow, WakeLock, Alarm, Service
    }
}
```

## Repository Datenfluss

### Repository-Schnittstellendesign
```kotlin
// DA (Detection Action) Repository - Haupt-Datenrepository
interface DARepository {
    fun getApps(userId: Int): Flow<List<AppDas>>
    fun getWakelocks(userId: Int): Flow<List<WakelockDas>>
    fun getAlarms(userId: Int): Flow<List<AlarmDas>>
    fun getServices(userId: Int): Flow<List<ServiceDas>>
    
    suspend fun updateRule(rule: Rule)
    suspend fun deleteRule(rule: Rule)
    suspend fun syncAppInfo()
}

// Anwendungsdetail-Repository
interface AppDetailRepository {
    fun getAppDetail(packageName: String, userId: Int): Flow<AppDetail>
    fun getAppEvents(packageName: String, userId: Int): Flow<List<InfoEvent>>
    suspend fun updateAppConfig(config: AppConfig)
}
```

### Repository-Implementierung
```kotlin
class DARepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val infoEventDao: InfoEventDao,
    private val wakelockRuleDao: WakelockRuleDao,
    private val xProvider: XProvider
) : DARepository {
    
    override fun getApps(userId: Int): Flow<List<AppDas>> {
        return combine(
            appInfoDao.getAppsByUser(userId),
            infoEventDao.getEventStatsByUser(userId)
        ) { apps, eventStats ->
            apps.map { app ->
                val stats = eventStats[app.packageName] ?: EventStats()
                AppDas(
                    appInfo = app,
                    wakelockCount = stats.wakelockCount,
                    alarmCount = stats.alarmCount,
                    serviceCount = stats.serviceCount,
                    lastActivity = stats.lastActivity
                )
            }
        }.distinctUntilChanged()
    }
    
    override suspend fun updateRule(rule: Rule) {
        when (rule.type) {
            RuleType.WAKELOCK -> wakelockRuleDao.insertOrUpdate(rule.toWakelockRule())
            RuleType.ALARM -> alarmRuleDao.insertOrUpdate(rule.toAlarmRule())
            RuleType.SERVICE -> serviceRuleDao.insertOrUpdate(rule.toServiceRule())
        }
        
        // Mit Xposed-Modul synchronisieren
        xProvider.syncRule(rule)
    }
}
```

## ViewModel Datenfluss

### Zustandsverwaltungsmuster
```kotlin
// UI-Zustandsdefinition
data class DAsUiState(
    val apps: List<AppDas> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterOption: FilterOption = FilterOption.ALL,
    val sortOption: SortOption = SortOption.NAME,
    val currentUserId: Int = 0,
    val error: String? = null
)

// ViewModel-Implementierung
class DAsViewModel(
    private val repository: DARepository,
    private val userRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DAsUiState())
    val uiState: StateFlow<DAsUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            combine(
                repository.getApps(uiState.value.currentUserId),
                userRepository.searchQuery,
                userRepository.filterOption,
                userRepository.sortOption
            ) { apps, query, filter, sort ->
                apps.filter { app ->
                    // Anwendungssuche und Filterlogik
                    app.matchesQuery(query) && app.matchesFilter(filter)
                }.sortedWith(sort.comparator)
            }.catch { error ->
                _uiState.update { it.copy(error = error.message) }
            }.collect { filteredApps ->
                _uiState.update { 
                    it.copy(
                        apps = filteredApps,
                        isLoading = false
                    ) 
                }
            }
        }
    }
}
```

### Datenaktualisierungsablauf
```kotlin
// Durch Benutzeroperationen ausgelöster Datenfluss
fun updateWakelockRule(packageName: String, tag: String, action: ActionType) {
    viewModelScope.launch {
        try {
            val rule = WakelockRule(
                packageName = packageName,
                tag = tag,
                action = action,
                userId = uiState.value.currentUserId
            )
            
            repository.updateRule(rule)
            
            // UI wird automatisch durch Flow aktualisiert, keine manuelle Aktualisierung erforderlich
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }
}
```

## Prozessübergreifende Kommunikation

### XProvider Datenbrücke
```kotlin
class XProvider private constructor() {
    
    companion object {
        private const val AUTHORITY = "com.js.nowakelock.xprovider"
        
        // Ereigniseinfügungsablauf
        fun insertEvent(event: InfoEvent) {
            try {
                val data = bundleOf(
                    "instanceId" to event.instanceId,
                    "name" to event.name,
                    "type" to event.type.ordinal,
                    "packageName" to event.packageName,
                    "userId" to event.userId,
                    "startTime" to event.startTime,
                    "isBlocked" to event.isBlocked
                )
                
                // SystemProperties für prozessübergreifende Kommunikation verwenden
                val serialized = serializeBundle(data)
                SystemProperties.set("sys.nowakelock.event", serialized)
                
            } catch (e: Exception) {
                XposedBridge.log("XProvider insertEvent failed: ${e.message}")
            }
        }
        
        // Regelsynchronisationsablauf
        fun syncRule(rule: Rule) {
            try {
                val data = bundleOf(
                    "id" to rule.id,
                    "packageName" to rule.packageName,
                    "target" to rule.target,
                    "action" to rule.action.ordinal,
                    "type" to rule.type.ordinal
                )
                
                val serialized = serializeBundle(data)
                SystemProperties.set("sys.nowakelock.rule.${rule.id}", serialized)
                
            } catch (e: Exception) {
                XposedBridge.log("XProvider syncRule failed: ${e.message}")
            }
        }
    }
}
```

### Datensynchronisationsmechanismus
```kotlin
class DataSynchronizer(
    private val appInfoDao: AppInfoDao,
    private val infoEventDao: InfoEventDao
) {
    
    // Regelmäßige Synchronisation der Anwendungsinformationen
    suspend fun syncAppInfo() {
        try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(0)
            
            val appInfoList = installedApps.map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    uid = appInfo.uid,
                    label = appInfo.loadLabel(packageManager).toString(),
                    system = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    enabled = appInfo.enabled,
                    processName = appInfo.processName ?: appInfo.packageName,
                    userId = UserHandle.getUserId(appInfo.uid)
                )
            }
            
            appInfoDao.insertOrUpdateAll(appInfoList)
            
        } catch (e: Exception) {
            Log.e("DataSync", "Failed to sync app info", e)
        }
    }
    
    // Ereignisdaten aus Xposed-Modul lesen
    suspend fun syncEventData() {
        try {
            // Ereignisdaten in Systemeigenschaften lesen
            val eventData = SystemProperties.get("sys.nowakelock.events", "")
            if (eventData.isNotEmpty()) {
                val events = deserializeEvents(eventData)
                infoEventDao.insertAll(events)
                
                // Gelesene Daten löschen
                SystemProperties.set("sys.nowakelock.events", "")
            }
        } catch (e: Exception) {
            Log.e("DataSync", "Failed to sync event data", e)
        }
    }
}
```

## Echtzeitaktualisierungsmechanismus

### Flow-Kettenreaktion
```kotlin
class DataFlowManager {
    
    // Zentraler Datenfluss-Koordinator
    fun setupDataFlow(): Flow<AppDataUpdate> {
        return merge(
            // 1. Systemereignisfluss
            systemEventFlow(),
            // 2. Benutzeraktionsfluss
            userActionFlow(),
            // 3. Geplanter Synchronisationsfluss
            scheduledSyncFlow()
        ).scan(AppDataUpdate()) { accumulator, update ->
            accumulator.merge(update)
        }.distinctUntilChanged()
    }
    
    private fun systemEventFlow(): Flow<SystemEvent> {
        return callbackFlow {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    // Änderungen der Systemeigenschaften überwachen
                    trySend(SystemEvent.PropertyChanged(uri))
                }
            }
            
            // Listener registrieren
            context.contentResolver.registerContentObserver(
                Settings.Global.CONTENT_URI,
                true,
                observer
            )
            
            awaitClose {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
    }
}
```

### Datendeduplizierung und Leistungsoptimierung
```kotlin
// Datendeduplizierungsstrategie
fun <T> Flow<List<T>>.distinctItems(): Flow<List<T>> {
    return distinctUntilChanged { old, new ->
        old.size == new.size && old.zip(new).all { (a, b) -> a == b }
    }
}

// Stapelaktualisierungsoptimierung
class BatchUpdateManager {
    
    private val pendingUpdates = mutableListOf<DataUpdate>()
    private val updateJob = Job()
    
    fun scheduleUpdate(update: DataUpdate) {
        synchronized(pendingUpdates) {
            pendingUpdates.add(update)
        }
        
        // Verzögerte Stapelausführung
        CoroutineScope(updateJob).launch {
            delay(100) // 100ms Stapelfenster
            executeBatchUpdate()
        }
    }
    
    private suspend fun executeBatchUpdate() {
        val updates = synchronized(pendingUpdates) {
            val copy = pendingUpdates.toList()
            pendingUpdates.clear()
            copy
        }
        
        if (updates.isNotEmpty()) {
            // Updates zusammenführen und ausführen
            val mergedUpdate = updates.reduce { acc, update -> acc.merge(update) }
            executeUpdate(mergedUpdate)
        }
    }
}
```

## Fehlerbehandlung und Wiederherstellung

### Datenfluss-Fehlerbehandlung
```kotlin
// Repository-Schicht-Fehlerbehandlung
override fun getApps(userId: Int): Flow<List<AppDas>> {
    return flow {
        emit(getAppsFromDatabase(userId))
    }.catch { exception ->
        when (exception) {
            is SQLiteException -> {
                Log.e("DARepository", "Database error", exception)
                emit(emptyList()) // Leere Liste als Degradierung zurückgeben
            }
            is SecurityException -> {
                Log.e("DARepository", "Permission denied", exception)
                emit(getCachedApps(userId)) // Cache-Daten verwenden
            }
            else -> {
                Log.e("DARepository", "Unexpected error", exception)
                throw exception // Unbekannte Fehler erneut werfen
            }
        }
    }.retry(3) { exception ->
        exception is IOException || exception is SQLiteException
    }
}

// ViewModel-Schicht-Fehlerbehandlung
private fun handleError(error: Throwable) {
    val errorMessage = when (error) {
        is NetworkException -> "Netzwerkverbindung fehlgeschlagen, bitte überprüfen Sie die Netzwerkeinstellungen"
        is DatabaseException -> "Datenladen fehlgeschlagen, bitte erneut versuchen"
        is PermissionException -> "Unzureichende Berechtigungen, bitte überprüfen Sie die Anwendungsberechtigungen"
        else -> "Unbekannter Fehler: ${error.message}"
    }
    
    _uiState.update { 
        it.copy(
            error = errorMessage,
            isLoading = false
        ) 
    }
}
```

### Datenkonsistenzgewährleistung
```kotlin
class DataConsistencyManager {
    
    // Datenvalidierung
    suspend fun validateDataConsistency() {
        val inconsistencies = mutableListOf<DataInconsistency>()
        
        // 1. Verwaiste Ereignisse prüfen
        val orphanEvents = infoEventDao.getOrphanEvents()
        if (orphanEvents.isNotEmpty()) {
            inconsistencies.add(DataInconsistency.OrphanEvents(orphanEvents))
        }
        
        // 2. Doppelte Regeln prüfen
        val duplicateRules = ruleDao.getDuplicateRules()
        if (duplicateRules.isNotEmpty()) {
            inconsistencies.add(DataInconsistency.DuplicateRules(duplicateRules))
        }
        
        // 3. Dateninkonsistenzen reparieren
        inconsistencies.forEach { inconsistency ->
            fixInconsistency(inconsistency)
        }
    }
    
    private suspend fun fixInconsistency(inconsistency: DataInconsistency) {
        when (inconsistency) {
            is DataInconsistency.OrphanEvents -> {
                // Verwaiste Ereignisse löschen oder Standard-Anwendungsinformationen erstellen
                infoEventDao.deleteOrphanEvents()
            }
            is DataInconsistency.DuplicateRules -> {
                // Doppelte Regeln zusammenführen
                mergeDuplicateRules(inconsistency.rules)
            }
        }
    }
}
```

## Leistungsüberwachung

### Datenfluss-Leistungsmetriken
```kotlin
class DataFlowMetrics {
    
    fun trackFlowPerformance() {
        repository.getApps(userId)
            .onStart { startTime = System.currentTimeMillis() }
            .onEach { apps ->
                val duration = System.currentTimeMillis() - startTime
                Log.d("Performance", "Apps loaded in ${duration}ms, count: ${apps.size}")
                
                if (duration > 1000) {
                    Log.w("Performance", "Slow data loading detected")
                }
            }
            .catch { exception ->
                Log.e("Performance", "Data flow error", exception)
            }
            .flowOn(Dispatchers.IO)
            .collect()
    }
}
```

!!! info "Datenfluss-Eigenschaften"
    Das Datenfluss-Design von NoWakeLock nutzt die reaktiven Eigenschaften von Kotlin Flow voll aus und gewährleistet die Echtzeit- und Konsistenzeigenschaften der Daten.

!!! tip "Leistungsoptimierung"
    Die Verwendung von `distinctUntilChanged()` und Stapelaktualisierungsmechanismen kann unnötige UI-Updates erheblich reduzieren und die Anwendungsleistung verbessern.