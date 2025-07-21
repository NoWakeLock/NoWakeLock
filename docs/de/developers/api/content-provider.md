# ContentProvider API

NoWakeLock stellt eine ContentProvider-Schnittstelle für den Datenaustausch zwischen Anwendung und Xposed-Modul bereit, die Ereignisaufzeichnung, Regelverwaltung und Statusabfragen unterstützt.

## ContentProvider Übersicht

### Autorisierung und URI-Struktur
```kotlin
const val AUTHORITY = "com.js.nowakelock.provider"
const val BASE_URI = "content://$AUTHORITY"

// Haupt-URI-Endpunkte
val EVENTS_URI = Uri.parse("$BASE_URI/events")           // Ereignisaufzeichnung
val RULES_URI = Uri.parse("$BASE_URI/rules")             // Regelverwaltung
val APPS_URI = Uri.parse("$BASE_URI/apps")               // Anwendungsinformationen
val STATS_URI = Uri.parse("$BASE_URI/stats")             // Statistikdaten
val STATUS_URI = Uri.parse("$BASE_URI/status")           // Systemstatus
val CONFIG_URI = Uri.parse("$BASE_URI/config")           // Konfigurationsverwaltung
```

### Berechtigungsdeklaration
```xml
<!-- Leseberechtigung -->
<permission
    android:name="com.js.nowakelock.permission.READ_DATA"
    android:protectionLevel="signature" />

<!-- Schreibberechtigung -->
<permission
    android:name="com.js.nowakelock.permission.WRITE_DATA"
    android:protectionLevel="signature" />

<!-- Provider-Deklaration -->
<provider
    android:name=".provider.NoWakeLockProvider"
    android:authorities="com.js.nowakelock.provider"
    android:exported="false"
    android:readPermission="com.js.nowakelock.permission.READ_DATA"
    android:writePermission="com.js.nowakelock.permission.WRITE_DATA" />
```

## Ereignisaufzeichnungs-API

### Ereignis einfügen
```kotlin
// Ereignistypen
object EventTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// WakeLock-Ereignis einfügen
fun insertWakelockEvent(
    context: Context,
    packageName: String,
    tag: String,
    flags: Int,
    uid: Int,
    isBlocked: Boolean,
    instanceId: String = UUID.randomUUID().toString()
): Uri? {
    val values = ContentValues().apply {
        put("type", EventTypes.WAKELOCK)
        put("package_name", packageName)
        put("name", tag)
        put("flags", flags)
        put("uid", uid)
        put("user_id", UserHandle.getUserId(uid))
        put("start_time", System.currentTimeMillis())
        put("is_blocked", if (isBlocked) 1 else 0)
        put("instance_id", instanceId)
    }
    
    return context.contentResolver.insert(EVENTS_URI, values)
}

// Ereignis-Endzeit aktualisieren
fun updateEventEndTime(
    context: Context,
    instanceId: String,
    endTime: Long = System.currentTimeMillis()
): Int {
    val values = ContentValues().apply {
        put("end_time", endTime)
        put("duration", endTime - startTime) // Dauer berechnen
    }
    
    val uri = Uri.withAppendedPath(EVENTS_URI, instanceId)
    return context.contentResolver.update(uri, values, null, null)
}
```

### Ereignisse abfragen
```kotlin
// Ereignisse einer Anwendung abfragen
fun queryAppEvents(
    context: Context,
    packageName: String,
    eventType: Int? = null,
    startTime: Long? = null,
    endTime: Long? = null,
    limit: Int = 1000
): Cursor? {
    val selection = buildString {
        append("package_name = ?")
        if (eventType != null) append(" AND type = ?")
        if (startTime != null) append(" AND start_time >= ?")
        if (endTime != null) append(" AND start_time <= ?")
    }
    
    val selectionArgs = buildList {
        add(packageName)
        if (eventType != null) add(eventType.toString())
        if (startTime != null) add(startTime.toString())
        if (endTime != null) add(endTime.toString())
    }.toTypedArray()
    
    val sortOrder = "start_time DESC LIMIT $limit"
    
    return context.contentResolver.query(
        EVENTS_URI,
        null,
        selection,
        selectionArgs,
        sortOrder
    )
}

// Aktive Ereignisse abfragen
fun queryActiveEvents(context: Context, eventType: Int? = null): Cursor? {
    val selection = if (eventType != null) {
        "end_time IS NULL AND type = ?"
    } else {
        "end_time IS NULL"
    }
    
    val selectionArgs = eventType?.toString()?.let { arrayOf(it) }
    
    return context.contentResolver.query(
        EVENTS_URI,
        arrayOf("instance_id", "package_name", "name", "start_time", "type"),
        selection,
        selectionArgs,
        "start_time DESC"
    )
}

// Ereignisstatistiken der aktuellen Sitzung abfragen
fun queryCurrentSessionStatistics(
    context: Context,
    packageName: String? = null
): Cursor? {
    val uri = Uri.parse("$BASE_URI/events/current_session")
    val selection = if (packageName != null) {
        "package_name = ?"
    } else null
    
    val selectionArgs = packageName?.let { arrayOf(it) }
    
    return context.contentResolver.query(
        uri,
        null,
        selection,
        selectionArgs,
        "start_time DESC"
    )
}
```

## Regelverwaltungs-API

### Regel-CRUD-Operationen
```kotlin
// Regeltypen
object RuleTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// Aktionstypen
object ActionTypes {
    const val ALLOW = 0
    const val LIMIT = 1
    const val BLOCK = 2
}

// Übereinstimmungstypen
object MatchTypes {
    const val EXACT = 0
    const val CONTAINS = 1
    const val REGEX = 2
    const val WILDCARD = 3
}

// Regel einfügen
fun insertRule(
    context: Context,
    packageName: String,
    target: String,
    ruleType: Int,
    action: Int,
    matchType: Int = MatchTypes.EXACT,
    timeout: Long = 60000,
    priority: Int = 0
): Uri? {
    val ruleId = UUID.randomUUID().toString()
    val currentTime = System.currentTimeMillis()
    
    val values = ContentValues().apply {
        put("id", ruleId)
        put("package_name", packageName)
        put("target", target)
        put("type", ruleType)
        put("action", action)
        put("match_type", matchType)
        put("timeout", timeout)
        put("priority", priority)
        put("enabled", 1)
        put("created_time", currentTime)
        put("last_modified_time", currentTime)
    }
    
    return context.contentResolver.insert(RULES_URI, values)
}

// Regel aktualisieren
fun updateRule(
    context: Context,
    ruleId: String,
    action: Int? = null,
    timeout: Long? = null,
    enabled: Boolean? = null,
    priority: Int? = null
): Int {
    val values = ContentValues().apply {
        action?.let { put("action", it) }
        timeout?.let { put("timeout", it) }
        enabled?.let { put("enabled", if (it) 1 else 0) }
        priority?.let { put("priority", it) }
        put("last_modified_time", System.currentTimeMillis())
    }
    
    val uri = Uri.withAppendedPath(RULES_URI, ruleId)
    return context.contentResolver.update(uri, values, null, null)
}

// Regel löschen
fun deleteRule(context: Context, ruleId: String): Int {
    val uri = Uri.withAppendedPath(RULES_URI, ruleId)
    return context.contentResolver.delete(uri, null, null)
}

// Anwendungsregeln stapelweise löschen
fun deleteAppRules(context: Context, packageName: String): Int {
    return context.contentResolver.delete(
        RULES_URI,
        "package_name = ?",
        arrayOf(packageName)
    )
}
```

### Regeln abfragen
```kotlin
// Anwendungsregeln abfragen
fun queryAppRules(
    context: Context,
    packageName: String,
    ruleType: Int? = null,
    enabled: Boolean? = null
): Cursor? {
    val selection = buildString {
        append("package_name = ?")
        if (ruleType != null) append(" AND type = ?")
        if (enabled != null) append(" AND enabled = ?")
    }
    
    val selectionArgs = buildList {
        add(packageName)
        if (ruleType != null) add(ruleType.toString())
        if (enabled != null) add(if (enabled) "1" else "0")
    }.toTypedArray()
    
    return context.contentResolver.query(
        RULES_URI,
        null,
        selection,
        selectionArgs,
        "priority DESC, last_modified_time DESC"
    )
}

// Globale Regeln abfragen
fun queryGlobalRules(
    context: Context,
    ruleType: Int? = null
): Cursor? {
    val selection = buildString {
        append("package_name = '*'")
        if (ruleType != null) append(" AND type = ?")
        append(" AND enabled = 1")
    }
    
    val selectionArgs = ruleType?.toString()?.let { arrayOf(it) }
    
    return context.contentResolver.query(
        RULES_URI,
        null,
        selection,
        selectionArgs,
        "priority DESC"
    )
}

// Regelübereinstimmung abfragen
fun queryMatchingRules(
    context: Context,
    packageName: String,
    target: String,
    ruleType: Int
): Cursor? {
    val uri = Uri.parse("$BASE_URI/rules/match")
    val selection = "package_name IN (?, '*') AND type = ? AND enabled = 1"
    val selectionArgs = arrayOf(packageName, ruleType.toString())
    
    return context.contentResolver.query(
        uri,
        null,
        selection,
        selectionArgs,
        "priority DESC"
    )
}
```

## Anwendungsinformations-API

### Anwendungsdatenverwaltung
```kotlin
// Anwendungsinformationen einfügen oder aktualisieren
fun insertOrUpdateApp(
    context: Context,
    packageName: String,
    uid: Int,
    label: String,
    isSystem: Boolean,
    enabled: Boolean,
    versionCode: Long,
    versionName: String
): Uri? {
    val userId = UserHandle.getUserId(uid)
    
    val values = ContentValues().apply {
        put("package_name", packageName)
        put("uid", uid)
        put("user_id", userId)
        put("label", label)
        put("system", if (isSystem) 1 else 0)
        put("enabled", if (enabled) 1 else 0)
        put("version_code", versionCode)
        put("version_name", versionName)
        put("last_update_time", System.currentTimeMillis())
    }
    
    // REPLACE-Strategie verwenden
    val uri = Uri.parse("$BASE_URI/apps/replace")
    return context.contentResolver.insert(uri, values)
}

// Anwendungsinformationen abfragen
fun queryAppInfo(
    context: Context,
    packageName: String,
    userId: Int = 0
): Cursor? {
    return context.contentResolver.query(
        APPS_URI,
        null,
        "package_name = ? AND user_id = ?",
        arrayOf(packageName, userId.toString()),
        null
    )
}

// Alle Anwendungen eines Benutzers abfragen
fun queryUserApps(
    context: Context,
    userId: Int,
    systemApps: Boolean? = null,
    enabledOnly: Boolean = false
): Cursor? {
    val selection = buildString {
        append("user_id = ?")
        if (systemApps != null) append(" AND system = ?")
        if (enabledOnly) append(" AND enabled = 1")
    }
    
    val selectionArgs = buildList {
        add(userId.toString())
        if (systemApps != null) add(if (systemApps) "1" else "0")
    }.toTypedArray()
    
    return context.contentResolver.query(
        APPS_URI,
        null,
        selection,
        selectionArgs,
        "label ASC"
    )
}

// Anwendungen suchen
fun searchApps(
    context: Context,
    query: String,
    userId: Int
): Cursor? {
    val uri = Uri.parse("$BASE_URI/apps/search")
    return context.contentResolver.query(
        uri,
        null,
        "user_id = ? AND (label LIKE ? OR package_name LIKE ?)",
        arrayOf(userId.toString(), "%$query%", "%$query%"),
        "label ASC"
    )
}
```

## Statistikdaten-API

### Abfrage der aktuellen Sitzungsstatistiken
```kotlin
// Statistiken der aktuellen Anwendungssitzung
fun queryCurrentAppStatistics(
    context: Context,
    packageName: String
): Cursor? {
    val uri = Uri.parse("$BASE_URI/stats/current/$packageName")
    return context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )
}

// Aktuelle Gesamtstatistiken
fun queryCurrentOverallStatistics(
    context: Context,
    userId: Int = 0
): Cursor? {
    val uri = Uri.parse("$BASE_URI/stats/current_overall")
    return context.contentResolver.query(
        uri,
        null,
        "user_id = ?",
        arrayOf(userId.toString()),
        null
    )
}

// Aktive Anwendungsrangliste (aktuelle Sitzung)
fun queryActiveApps(
    context: Context,
    metric: String = "event_count", // event_count, active_wakelocks, blocked_count
    limit: Int = 10
): Cursor? {
    val uri = Uri.parse("$BASE_URI/stats/active_apps")
    return context.contentResolver.query(
        uri,
        null,
        "metric = ? LIMIT ?",
        arrayOf(metric, limit.toString()),
        "value DESC"
    )
}
```

!!! warning "Wichtiger Hinweis zum Datenlebenszyklus"
    Die Datenspeicherung von NoWakeLock basiert auf einem Sitzungsdesign:
    
    - **Daten werden nach Neustart gelöscht**: BootResetManager löscht nach Erkennung eines Geräte-Neustarts alle Statistikdaten
    - **XProvider-Initialisierung löscht Daten**: InfoDatabase ruft bei der Erstellung clearAllTables() auf
    - **Keine sitzungsübergreifenden historischen Daten**: Statistik-APIs geben nur Daten der aktuellen Sitzung zurück
    
    Dieses Design gewährleistet die Aktualität der Statistikdaten und die Systemleistung.

## Systemstatus-API

### Statusabfrageschnittstelle
```kotlin
// Modulstatus abfragen
fun queryModuleStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/module")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Hook-Status abfragen
fun queryHookStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/hooks")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Leistungsmetriken abfragen
fun queryPerformanceMetrics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/performance")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Regelstatistiken abfragen
fun queryRuleStatistics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/rules")
    return context.contentResolver.query(uri, null, null, null, null)
}
```

## Konfigurationsverwaltungs-API

### Konfigurationssynchronisationsschnittstelle
```kotlin
// Konfiguration mit Xposed-Modul synchronisieren
fun syncConfiguration(context: Context): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync")
    val values = ContentValues().apply {
        put("sync_time", System.currentTimeMillis())
        put("sync_type", "full")
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// Inkrementelle Regelsynchronisation
fun syncRule(context: Context, ruleId: String): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync_rule")
    val values = ContentValues().apply {
        put("rule_id", ruleId)
        put("sync_time", System.currentTimeMillis())
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// Synchronisationsstatus abfragen
fun querySyncStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/config/sync_status")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Konfiguration zurücksetzen
fun resetConfiguration(context: Context, resetType: String = "all"): Boolean {
    val uri = Uri.parse("$BASE_URI/config/reset")
    val values = ContentValues().apply {
        put("reset_type", resetType)
        put("reset_time", System.currentTimeMillis())
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}
```

## Stapelverarbeitungs-API

### Stapeldatenverarbeitung
```kotlin
// Ereignisse stapelweise einfügen
fun bulkInsertEvents(context: Context, events: List<ContentValues>): Int {
    val uri = Uri.parse("$BASE_URI/events/bulk")
    return context.contentResolver.bulkInsert(uri, events.toTypedArray())
}

// Regeln stapelweise aktualisieren
fun bulkUpdateRules(context: Context, updates: List<Pair<String, ContentValues>>): Int {
    val uri = Uri.parse("$BASE_URI/rules/bulk_update")
    
    // ContentProviderOperation für Stapelverarbeitung verwenden
    val operations = updates.map { (ruleId, values) ->
        ContentProviderOperation.newUpdate(Uri.withAppendedPath(RULES_URI, ruleId))
            .withValues(values)
            .build()
    }
    
    return try {
        val results = context.contentResolver.applyBatch(AUTHORITY, ArrayList(operations))
        results.count { it.count > 0 }
    } catch (e: Exception) {
        0
    }
}

// Abgelaufene Ereignisse stapelweise löschen
fun cleanupOldEvents(context: Context, cutoffTime: Long): Int {
    val uri = Uri.parse("$BASE_URI/events/cleanup")
    return context.contentResolver.delete(
        uri,
        "start_time < ?",
        arrayOf(cutoffTime.toString())
    )
}
```

## Beobachtermuster

### Datenänderungsüberwachung
```kotlin
// Ereignisänderungs-Listener registrieren
class EventObserver(private val context: Context) : ContentObserver(Handler(Looper.getMainLooper())) {
    
    fun startObserving() {
        context.contentResolver.registerContentObserver(
            EVENTS_URI,
            true, // Sub-URIs überwachen
            this
        )
    }
    
    fun stopObserving() {
        context.contentResolver.unregisterContentObserver(this)
    }
    
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri?.let { handleUriChange(it) }
    }
    
    private fun handleUriChange(uri: Uri) {
        when {
            uri.toString().startsWith(EVENTS_URI.toString()) -> {
                // Ereignisänderung verarbeiten
                onEventChanged(uri)
            }
            uri.toString().startsWith(RULES_URI.toString()) -> {
                // Regeländerung verarbeiten
                onRuleChanged(uri)
            }
            uri.toString().startsWith(APPS_URI.toString()) -> {
                // Anwendungsänderung verarbeiten
                onAppChanged(uri)
            }
        }
    }
    
    private fun onEventChanged(uri: Uri) {
        // Ereignisänderungslogik implementieren
    }
    
    private fun onRuleChanged(uri: Uri) {
        // Regeländerungslogik implementieren
        // Kann Konfigurationssynchronisation auslösen
        syncRule(context, uri.lastPathSegment ?: "")
    }
    
    private fun onAppChanged(uri: Uri) {
        // Anwendungsänderungslogik implementieren
    }
}
```

## Fehlerbehandlung

### Ausnahmebehandlung
```kotlin
object ContentProviderErrors {
    const val SUCCESS = 0
    const val PERMISSION_DENIED = 1
    const val INVALID_ARGUMENTS = 2
    const val DATABASE_ERROR = 3
    const val SYNC_FAILED = 4
    const val UNKNOWN_ERROR = 5
}

// Abfrageoperation umhüllen
fun safeQuery(
    context: Context,
    uri: Uri,
    projection: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null
): Result<Cursor?> {
    return try {
        val cursor = context.contentResolver.query(
            uri, projection, selection, selectionArgs, sortOrder
        )
        Result.success(cursor)
    } catch (e: SecurityException) {
        Result.failure(SecurityException("Berechtigung verweigert: ${e.message}"))
    } catch (e: IllegalArgumentException) {
        Result.failure(IllegalArgumentException("Ungültige Argumente: ${e.message}"))
    } catch (e: Exception) {
        Result.failure(Exception("Abfrage fehlgeschlagen: ${e.message}"))
    }
}

// Einfügeoperation umhüllen
fun safeInsert(
    context: Context,
    uri: Uri,
    values: ContentValues
): Result<Uri?> {
    return try {
        val result = context.contentResolver.insert(uri, values)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

!!! info "API-Verwendungsanleitung"
    Die ContentProvider-API bietet eine vollständige Datenzugriffsschnittstelle, die Ereignisaufzeichnung, Regelverwaltung, Anwendungsinformationsabfragen und aktuelle Sitzungsstatistiken unterstützt.

!!! warning "Berechtigungsanforderungen"
    Die Verwendung der ContentProvider-API erfordert entsprechende Berechtigungsdeklarationen, um sicherzustellen, dass der Aufrufer über Lese- und Schreibberechtigungen verfügt.

!!! tip "Leistungs- und Datenverwaltungsempfehlungen"
    - Für umfangreiche Datenoperationen empfiehlt sich die Verwendung von Stapel-APIs und asynchroner Verarbeitung, um das Blockieren des Hauptthreads zu vermeiden
    - Verwenden Sie ContentObserver zur Überwachung von Datenänderungen für Echtzeitaktualisierungen
    - Beachten Sie, dass Daten nur in der aktuellen Sitzung gültig sind; alle Statistikdaten werden nach einem Geräte-Neustart gelöscht