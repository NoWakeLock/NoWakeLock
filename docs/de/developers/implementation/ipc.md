# Inter-Process Communication (IPC)

Das IPC-System von NoWakeLock ist für den Datenaustausch zwischen dem Xposed-Modul und dem Anwendungsprozess verantwortlich und gewährleistet die Zuverlässigkeit von Konfigurationssynchronisation, Ereignisübertragung und Statusabfragen.

## IPC-Architektur-Übersicht

### Kommunikationsmodell
```mermaid
graph TB
    A[NoWakeLock Anwendungsprozess] --> B[XProvider]
    B --> C[SystemProperties]
    B --> D[ContentProvider]
    B --> E[Gemeinsame Dateien]
    
    F[Xposed-Modul] --> G[System-Prozess]
    G --> C
    G --> H[Binder IPC]
    G --> I[Memory Mapping]
    
    C --> J[Konfigurationssynchronisation]
    D --> K[Ereignisübertragung]
    E --> L[Statusabfrage]
    
    M[Hook-Ereignis] --> F
    F --> N[Regelabgleich]
    N --> O[Aktionsausführung]
    O --> P[Ergebnisrückmeldung]
    P --> B
```

### Kernkomponenten
```kotlin
// IPC-Manager-Schnittstelle
interface IPCManager {
    fun sendEvent(event: IPCEvent): Boolean
    fun queryConfiguration(query: ConfigQuery): ConfigResult?
    fun updateRule(rule: Rule): Boolean
    fun getSystemStatus(): SystemStatus
}

// Hauptimplementierungsklasse
class XProviderIPCManager : IPCManager {
    
    private val systemPropertiesChannel = SystemPropertiesChannel()
    private val contentProviderChannel = ContentProviderChannel()
    private val fileSystemChannel = FileSystemChannel()
    
    override fun sendEvent(event: IPCEvent): Boolean {
        return when (event.priority) {
            Priority.CRITICAL -> systemPropertiesChannel.send(event)
            Priority.HIGH -> contentProviderChannel.send(event)
            Priority.NORMAL -> fileSystemChannel.send(event)
            Priority.LOW -> fileSystemChannel.sendBatched(event)
        }
    }
}
```

## SystemProperties-Kanal

### Echtzeit-Kommunikation mit hoher Priorität
```kotlin
class SystemPropertiesChannel {
    
    companion object {
        private const val PROP_PREFIX = "sys.nowakelock."
        private const val MAX_PROP_SIZE = 92 // Android-Systembegrenzung
        private const val EVENT_PROP = "${PROP_PREFIX}event"
        private const val CONFIG_PROP = "${PROP_PREFIX}config"
        private const val STATUS_PROP = "${PROP_PREFIX}status"
        private const val SYNC_PROP = "${PROP_PREFIX}sync"
    }
    
    // Ereignis an Systemeigenschaften senden
    fun sendEvent(event: IPCEvent): Boolean {
        return try {
            val serialized = serializeEvent(event)
            if (serialized.length <= MAX_PROP_SIZE) {
                SystemProperties.set(EVENT_PROP, serialized)
                true
            } else {
                // Große Daten mit segmentierter Übertragung
                sendChunkedData(EVENT_PROP, serialized)
            }
        } catch (e: Exception) {
            XposedBridge.log("Failed to send event via SystemProperties: ${e.message}")
            false
        }
    }
    
    // Segmentierte Übertragung großer Daten
    private fun sendChunkedData(property: String, data: String): Boolean {
        val chunks = data.chunked(MAX_PROP_SIZE - 10) // Raum für Segmentierungsidentifikatoren reservieren
        val chunkCount = chunks.size
        
        try {
            // Segmentanzahl festlegen
            SystemProperties.set("${property}.chunks", chunkCount.toString())
            
            // Segmente senden
            chunks.forEachIndexed { index, chunk ->
                SystemProperties.set("${property}.$index", chunk)
            }
            
            // Vollständigkeitsmarkierung setzen
            SystemProperties.set("${property}.complete", System.currentTimeMillis().toString())
            return true
        } catch (e: Exception) {
            XposedBridge.log("Failed to send chunked data: ${e.message}")
            return false
        }
    }
    
    // Ereignis empfangen
    fun receiveEvent(): IPCEvent? {
        return try {
            val data = SystemProperties.get(EVENT_PROP, "")
            if (data.isNotEmpty()) {
                // Gelesene Daten löschen
                SystemProperties.set(EVENT_PROP, "")
                deserializeEvent(data)
            } else {
                // Segmentierte Daten prüfen
                receiveChunkedData(EVENT_PROP)
            }
        } catch (e: Exception) {
            XposedBridge.log("Failed to receive event: ${e.message}")
            null
        }
    }
    
    private fun receiveChunkedData(property: String): IPCEvent? {
        val chunkCountStr = SystemProperties.get("${property}.chunks", "")
        if (chunkCountStr.isEmpty()) return null
        
        val chunkCount = chunkCountStr.toIntOrNull() ?: return null
        val chunks = mutableListOf<String>()
        
        // Alle Segmente sammeln
        for (i in 0 until chunkCount) {
            val chunk = SystemProperties.get("${property}.$i", "")
            if (chunk.isEmpty()) return null // Segment fehlt
            chunks.add(chunk)
        }
        
        // Vollständigkeitsmarkierung prüfen
        val completeTime = SystemProperties.get("${property}.complete", "")
        if (completeTime.isEmpty()) return null
        
        // Segmentierte Daten bereinigen
        SystemProperties.set("${property}.chunks", "")
        for (i in 0 until chunkCount) {
            SystemProperties.set("${property}.$i", "")
        }
        SystemProperties.set("${property}.complete", "")
        
        // Daten zusammensetzen
        val fullData = chunks.joinToString("")
        return deserializeEvent(fullData)
    }
}
```

### Ereignisserialisierung
```kotlin
object EventSerializer {
    
    // Kompaktes Serialisierungsformat
    fun serializeEvent(event: IPCEvent): String {
        return when (event.type) {
            IPCEventType.WAKELOCK_EVENT -> serializeWakelockEvent(event as WakelockIPCEvent)
            IPCEventType.ALARM_EVENT -> serializeAlarmEvent(event as AlarmIPCEvent)
            IPCEventType.SERVICE_EVENT -> serializeServiceEvent(event as ServiceIPCEvent)
            IPCEventType.RULE_UPDATE -> serializeRuleUpdate(event as RuleUpdateEvent)
            IPCEventType.CONFIG_SYNC -> serializeConfigSync(event as ConfigSyncEvent)
        }
    }
    
    private fun serializeWakelockEvent(event: WakelockIPCEvent): String {
        // Kompaktes Format verwenden: Typ|Zeitstempel|Paketname|Tag|Aktion|Flags|...
        return buildString {
            append("WL") // Typidentifikator
            append("|${event.timestamp}")
            append("|${event.packageName}")
            append("|${event.tag}")
            append("|${event.action.ordinal}")
            append("|${event.flags}")
            append("|${event.uid}")
            append("|${if (event.isBlocked) 1 else 0}")
            if (event.instanceId.isNotEmpty()) {
                append("|${event.instanceId}")
            }
        }
    }
    
    fun deserializeEvent(data: String): IPCEvent? {
        val parts = data.split("|")
        if (parts.size < 3) return null
        
        return when (parts[0]) {
            "WL" -> deserializeWakelockEvent(parts)
            "AL" -> deserializeAlarmEvent(parts)
            "SV" -> deserializeServiceEvent(parts)
            "RU" -> deserializeRuleUpdate(parts)
            "CS" -> deserializeConfigSync(parts)
            else -> null
        }
    }
    
    private fun deserializeWakelockEvent(parts: List<String>): WakelockIPCEvent? {
        if (parts.size < 8) return null
        
        return try {
            WakelockIPCEvent(
                timestamp = parts[1].toLong(),
                packageName = parts[2],
                tag = parts[3],
                action = ActionType.values()[parts[4].toInt()],
                flags = parts[5].toInt(),
                uid = parts[6].toInt(),
                isBlocked = parts[7] == "1",
                instanceId = if (parts.size > 8) parts[8] else ""
            )
        } catch (e: Exception) {
            XposedBridge.log("Failed to deserialize WakeLock event: ${e.message}")
            null
        }
    }
}
```

## ContentProvider-Kanal

### Datenübertragung mittlerer Priorität
```kotlin
class ContentProviderChannel {
    
    companion object {
        private const val AUTHORITY = "com.js.nowakelock.provider"
        private const val BASE_URI = "content://$AUTHORITY"
        private val EVENTS_URI = Uri.parse("$BASE_URI/events")
        private val RULES_URI = Uri.parse("$BASE_URI/rules")
        private val APPS_URI = Uri.parse("$BASE_URI/apps")
        private val STATS_URI = Uri.parse("$BASE_URI/stats")
    }
    
    fun sendEvent(event: IPCEvent): Boolean {
        return try {
            val contentValues = eventToContentValues(event)
            val uri = when (event.type) {
                IPCEventType.WAKELOCK_EVENT,
                IPCEventType.ALARM_EVENT,
                IPCEventType.SERVICE_EVENT -> EVENTS_URI
                IPCEventType.RULE_UPDATE -> RULES_URI
                else -> return false
            }
            
            // ContentResolver zum Einfügen von Daten verwenden
            val context = getSystemContext()
            val result = context.contentResolver.insert(uri, contentValues)
            result != null
        } catch (e: Exception) {
            XposedBridge.log("ContentProvider send failed: ${e.message}")
            false
        }
    }
    
    fun queryRules(packageName: String? = null): List<Rule> {
        return try {
            val context = getSystemContext()
            val selection = packageName?.let { "package_name = ?" }
            val selectionArgs = packageName?.let { arrayOf(it) }
            
            val cursor = context.contentResolver.query(
                RULES_URI,
                null,
                selection,
                selectionArgs,
                "priority DESC"
            )
            
            cursor?.use { 
                cursorToRules(it)
            } ?: emptyList()
        } catch (e: Exception) {
            XposedBridge.log("Failed to query rules: ${e.message}")
            emptyList()
        }
    }
    
    fun updateRule(rule: Rule): Boolean {
        return try {
            val context = getSystemContext()
            val contentValues = ruleToContentValues(rule)
            val uri = Uri.withAppendedPath(RULES_URI, rule.id)
            
            val count = context.contentResolver.update(
                uri,
                contentValues,
                null,
                null
            )
            count > 0
        } catch (e: Exception) {
            XposedBridge.log("Failed to update rule: ${e.message}")
            false
        }
    }
    
    private fun eventToContentValues(event: IPCEvent): ContentValues {
        return ContentValues().apply {
            put("type", event.type.ordinal)
            put("timestamp", event.timestamp)
            put("priority", event.priority.ordinal)
            
            when (event) {
                is WakelockIPCEvent -> {
                    put("package_name", event.packageName)
                    put("name", event.tag)
                    put("action", event.action.ordinal)
                    put("flags", event.flags)
                    put("uid", event.uid)
                    put("is_blocked", event.isBlocked)
                    put("instance_id", event.instanceId)
                }
                is AlarmIPCEvent -> {
                    put("package_name", event.packageName)
                    put("name", event.tag)
                    put("alarm_type", event.alarmType)
                    put("trigger_time", event.triggerTime)
                    put("is_blocked", event.isBlocked)
                }
                // Andere Ereignistypen...
            }
        }
    }
}

// ContentProvider-Implementierung
class NoWakeLockProvider : ContentProvider() {
    
    private lateinit var database: AppDatabase
    private lateinit var infoDatabase: InfoDatabase
    
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "events", EVENTS_CODE)
        addURI(AUTHORITY, "events/#", EVENT_CODE)
        addURI(AUTHORITY, "rules", RULES_CODE)
        addURI(AUTHORITY, "rules/#", RULE_CODE)
        addURI(AUTHORITY, "apps", APPS_CODE)
        addURI(AUTHORITY, "stats/*", STATS_CODE)
    }
    
    override fun onCreate(): Boolean {
        context?.let { ctx ->
            database = AppDatabase.getInstance(ctx)
            infoDatabase = InfoDatabase.getInstance(ctx)
        }
        return true
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (uriMatcher.match(uri)) {
            EVENTS_CODE -> {
                values?.let { 
                    insertEvent(it)
                    Uri.withAppendedPath(uri, values.getAsString("instance_id"))
                }
            }
            RULES_CODE -> {
                values?.let {
                    insertRule(it)
                    Uri.withAppendedPath(uri, values.getAsString("id"))
                }
            }
            else -> null
        }
    }
    
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            RULES_CODE -> queryRules(selection, selectionArgs, sortOrder)
            APPS_CODE -> queryApps(selection, selectionArgs, sortOrder)
            STATS_CODE -> queryStats(uri.lastPathSegment!!, selection, selectionArgs)
            else -> null
        }
    }
    
    private fun insertEvent(values: ContentValues) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val event = contentValuesToEvent(values)
                infoDatabase.infoEventDao().insert(event)
            } catch (e: Exception) {
                Log.e("NoWakeLockProvider", "Failed to insert event", e)
            }
        }
    }
}
```

## Dateisystem-Kanal

### Stapelübertragung niedriger Priorität
```kotlin
class FileSystemChannel {
    
    companion object {
        private val DATA_DIR = File("/data/system/nowakelock")
        private val EVENTS_DIR = File(DATA_DIR, "events")
        private val CONFIG_DIR = File(DATA_DIR, "config")
        private val TEMP_DIR = File(DATA_DIR, "temp")
        private val BATCH_FILE = File(EVENTS_DIR, "batch_events.json")
        private val CONFIG_FILE = File(CONFIG_DIR, "current_config.json")
    }
    
    init {
        ensureDirectoryStructure()
    }
    
    private fun ensureDirectoryStructure() {
        try {
            listOf(DATA_DIR, EVENTS_DIR, CONFIG_DIR, TEMP_DIR).forEach { dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                    // Berechtigungen für Zugriff durch System-Prozess setzen
                    Runtime.getRuntime().exec("chmod 755 ${dir.absolutePath}")
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("Failed to create directory structure: ${e.message}")
        }
    }
    
    fun send(event: IPCEvent): Boolean {
        return try {
            val eventFile = File(EVENTS_DIR, "event_${System.currentTimeMillis()}.json")
            val json = Gson().toJson(event)
            eventFile.writeText(json)
            
            // Dateiberechtigungen setzen
            Runtime.getRuntime().exec("chmod 644 ${eventFile.absolutePath}")
            true
        } catch (e: Exception) {
            XposedBridge.log("Failed to write event file: ${e.message}")
            false
        }
    }
    
    fun sendBatched(event: IPCEvent): Boolean {
        synchronized(BATCH_FILE) {
            return try {
                val events = if (BATCH_FILE.exists()) {
                    val existingJson = BATCH_FILE.readText()
                    val type = object : TypeToken<MutableList<IPCEvent>>() {}.type
                    Gson().fromJson<MutableList<IPCEvent>>(existingJson, type) ?: mutableListOf()
                } else {
                    mutableListOf()
                }
                
                events.add(event)
                
                // Stapelgröße begrenzen
                if (events.size > 100) {
                    events.removeAt(0) // Ältestes Ereignis entfernen
                }
                
                val json = Gson().toJson(events)
                BATCH_FILE.writeText(json)
                true
            } catch (e: Exception) {
                XposedBridge.log("Failed to write batch file: ${e.message}")
                false
            }
        }
    }
    
    fun readBatchedEvents(): List<IPCEvent> {
        synchronized(BATCH_FILE) {
            return try {
                if (!BATCH_FILE.exists()) {
                    return emptyList()
                }
                
                val json = BATCH_FILE.readText()
                val type = object : TypeToken<List<IPCEvent>>() {}.type
                val events = Gson().fromJson<List<IPCEvent>>(json, type) ?: emptyList()
                
                // Stapeldatei leeren
                BATCH_FILE.delete()
                
                events
            } catch (e: Exception) {
                XposedBridge.log("Failed to read batch file: ${e.message}")
                emptyList()
            }
        }
    }
    
    fun writeConfiguration(config: Configuration): Boolean {
        return try {
            val json = Gson().toJson(config)
            CONFIG_FILE.writeText(json)
            Runtime.getRuntime().exec("chmod 644 ${CONFIG_FILE.absolutePath}")
            true
        } catch (e: Exception) {
            XposedBridge.log("Failed to write configuration: ${e.message}")
            false
        }
    }
    
    fun readConfiguration(): Configuration? {
        return try {
            if (!CONFIG_FILE.exists()) return null
            
            val json = CONFIG_FILE.readText()
            Gson().fromJson(json, Configuration::class.java)
        } catch (e: Exception) {
            XposedBridge.log("Failed to read configuration: ${e.message}")
            null
        }
    }
}
```

## Konfigurationssynchronisationsmechanismus

### Konfigurationsmanager
```kotlin
class ConfigurationSynchronizer(
    private val ipcManager: IPCManager,
    private val database: AppDatabase
) {
    
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val configCache = mutableMapOf<String, CachedConfig>()
    
    fun startSynchronization() {
        // Regelmäßige Synchronisationsaufgabe starten
        syncScope.launch {
            while (isActive) {
                try {
                    performFullSync()
                    delay(60_000) // Jede Minute synchronisieren
                } catch (e: Exception) {
                    XposedBridge.log("Sync failed: ${e.message}")
                    delay(10_000) // Bei Fehlern 10 Sekunden warten und wiederholen
                }
            }
        }
        
        // Konfigurationsänderungen überwachen
        syncScope.launch {
            database.wakelockRuleDao().getAllRules()
                .distinctUntilChanged()
                .collect { rules ->
                    syncRulesToXposed(rules)
                }
        }
    }
    
    private suspend fun performFullSync() {
        // 1. Alle Regeln synchronisieren
        val allRules = database.wakelockRuleDao().getAllRules().first() +
                      database.alarmRuleDao().getAllRules().first() +
                      database.serviceRuleDao().getAllRules().first()
        
        syncRulesToXposed(allRules)
        
        // 2. Benutzereinstellungen synchronisieren
        val preferences = database.userPreferencesDao().getUserPreferences().first()
        syncPreferencesToXposed(preferences)
        
        // 3. Anwendungsinformationen synchronisieren
        val apps = database.appInfoDao().getAllApps().first()
        syncAppsToXposed(apps)
    }
    
    private fun syncRulesToXposed(rules: List<Rule>) {
        val config = Configuration(
            version = System.currentTimeMillis(),
            rules = rules,
            updateTime = System.currentTimeMillis()
        )
        
        val event = ConfigSyncEvent(
            timestamp = System.currentTimeMillis(),
            configuration = config,
            syncType = SyncType.FULL_RULES
        )
        
        ipcManager.sendEvent(event)
    }
    
    fun onRuleUpdated(rule: Rule) {
        // Inkrementelle Synchronisation einzelner Regeln
        val event = RuleUpdateEvent(
            timestamp = System.currentTimeMillis(),
            rule = rule,
            operation = RuleOperation.UPDATE
        )
        
        ipcManager.sendEvent(event)
        
        // Lokalen Cache aktualisieren
        updateConfigCache(rule)
    }
    
    fun onRuleDeleted(ruleId: String) {
        val event = RuleUpdateEvent(
            timestamp = System.currentTimeMillis(),
            ruleId = ruleId,
            operation = RuleOperation.DELETE
        )
        
        ipcManager.sendEvent(event)
        
        // Aus Cache entfernen
        configCache.remove(ruleId)
    }
    
    private fun updateConfigCache(rule: Rule) {
        configCache[rule.id] = CachedConfig(
            rule = rule,
            cacheTime = System.currentTimeMillis(),
            version = rule.lastModifiedTime
        )
    }
}

// Konfigurationsdatenklassen
data class Configuration(
    val version: Long,
    val rules: List<Rule>,
    val preferences: UserPreferences? = null,
    val apps: List<AppInfo>? = null,
    val updateTime: Long
)

data class CachedConfig(
    val rule: Rule,
    val cacheTime: Long,
    val version: Long
) {
    fun isExpired(maxAge: Long = 300_000): Boolean {
        return System.currentTimeMillis() - cacheTime > maxAge
    }
}

enum class SyncType {
    FULL_RULES, INCREMENTAL_RULE, PREFERENCES, APPS
}

enum class RuleOperation {
    CREATE, UPDATE, DELETE
}
```

## Statusabfragesystem

### Statusmanager
```kotlin
class SystemStatusManager(
    private val ipcManager: IPCManager
) {
    
    fun getModuleStatus(): ModuleStatus {
        val statusQuery = StatusQuery(
            type = StatusType.MODULE_STATUS,
            timestamp = System.currentTimeMillis()
        )
        
        val result = ipcManager.queryConfiguration(statusQuery)
        return result?.let {
            parseModuleStatus(it.data)
        } ?: ModuleStatus.UNKNOWN
    }
    
    fun getHookStatus(): Map<HookType, HookStatus> {
        val statusQuery = StatusQuery(
            type = StatusType.HOOK_STATUS,
            timestamp = System.currentTimeMillis()
        )
        
        val result = ipcManager.queryConfiguration(statusQuery)
        return result?.let {
            parseHookStatus(it.data)
        } ?: emptyMap()
    }
    
    fun getPerformanceMetrics(): PerformanceMetrics {
        val statusQuery = StatusQuery(
            type = StatusType.PERFORMANCE_METRICS,
            timestamp = System.currentTimeMillis()
        )
        
        val result = ipcManager.queryConfiguration(statusQuery)
        return result?.let {
            parsePerformanceMetrics(it.data)
        } ?: PerformanceMetrics.DEFAULT
    }
    
    private fun parseModuleStatus(data: String): ModuleStatus {
        return try {
            val parts = data.split("|")
            ModuleStatus(
                isActive = parts[0] == "1",
                version = parts[1],
                loadTime = parts[2].toLong(),
                hookCount = parts[3].toInt(),
                errorCount = parts[4].toInt(),
                lastActivity = parts[5].toLong()
            )
        } catch (e: Exception) {
            ModuleStatus.UNKNOWN
        }
    }
}

// Statusdatenklassen
data class ModuleStatus(
    val isActive: Boolean,
    val version: String,
    val loadTime: Long,
    val hookCount: Int,
    val errorCount: Int,
    val lastActivity: Long
) {
    companion object {
        val UNKNOWN = ModuleStatus(
            isActive = false,
            version = "unknown",
            loadTime = 0,
            hookCount = 0,
            errorCount = 0,
            lastActivity = 0
        )
    }
}

data class HookStatus(
    val isHooked: Boolean,
    val hookTime: Long,
    val callCount: Long,
    val errorCount: Long,
    val avgDuration: Long
)

enum class HookType {
    WAKELOCK_ACQUIRE, WAKELOCK_RELEASE,
    ALARM_TRIGGER, ALARM_SET,
    SERVICE_START, SERVICE_STOP, SERVICE_BIND
}

enum class StatusType {
    MODULE_STATUS, HOOK_STATUS, PERFORMANCE_METRICS, RULE_COUNT
}
```

## Fehlerbehandlung und Wiederholungsmechanismus

### Zuverlässigkeitsgarantie
```kotlin
class ReliableIPCManager(
    private val primaryChannel: SystemPropertiesChannel,
    private val secondaryChannel: ContentProviderChannel,
    private val fallbackChannel: FileSystemChannel
) : IPCManager {
    
    private val retryPolicy = RetryPolicy(
        maxRetries = 3,
        baseDelay = 1000,
        maxDelay = 10000,
        backoffMultiplier = 2.0
    )
    
    override fun sendEvent(event: IPCEvent): Boolean {
        return withRetry(retryPolicy) {
            when (event.priority) {
                Priority.CRITICAL -> {
                    primaryChannel.send(event) || 
                    secondaryChannel.send(event)
                }
                Priority.HIGH -> {
                    secondaryChannel.send(event) ||
                    primaryChannel.send(event)
                }
                Priority.NORMAL -> {
                    fallbackChannel.send(event)
                }
                Priority.LOW -> {
                    fallbackChannel.sendBatched(event)
                }
            }
        }
    }
    
    private fun <T> withRetry(policy: RetryPolicy, operation: () -> T): T {
        var lastException: Exception? = null
        var delay = policy.baseDelay
        
        repeat(policy.maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                XposedBridge.log("IPC attempt ${attempt + 1} failed: ${e.message}")
                
                if (attempt < policy.maxRetries - 1) {
                    Thread.sleep(delay)
                    delay = minOf(delay * policy.backoffMultiplier.toLong(), policy.maxDelay)
                }
            }
        }
        
        throw lastException ?: Exception("All retry attempts failed")
    }
}

data class RetryPolicy(
    val maxRetries: Int,
    val baseDelay: Long,
    val maxDelay: Long,
    val backoffMultiplier: Double
)
```

!!! info "IPC-Designprinzipien"
    Das IPC-System von NoWakeLock verwendet ein Mehrkanal-Redundanzdesign, das zuverlässige prozessübergreifende Kommunikation in verschiedenen Systemumgebungen gewährleistet.

!!! warning "Berechtigungshinweise"
    Prozessübergreifende Kommunikation erfordert angemessene Berechtigungskonfiguration, insbesondere muss der Dateisystem-Kanal sicherstellen, dass der System-Prozess auf gemeinsame Verzeichnisse zugreifen kann.

!!! tip "Leistungsoptimierung"
    Wählen Sie basierend auf Priorität und Größe der Daten den geeigneten Kommunikationskanal: Hochfrequente kleine Daten verwenden SystemProperties, große Daten verwenden ContentProvider oder Dateisystem.