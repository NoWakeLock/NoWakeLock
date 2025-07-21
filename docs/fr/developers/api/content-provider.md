# API ContentProvider

NoWakeLock fournit une interface ContentProvider pour l'échange de données entre l'application et le module Xposed, prenant en charge l'enregistrement d'événements, la gestion des règles et les requêtes de statut.

## Aperçu du ContentProvider

### Autorisation et structure URI
```kotlin
const val AUTHORITY = "com.js.nowakelock.provider"
const val BASE_URI = "content://$AUTHORITY"

// Points de terminaison URI principaux
val EVENTS_URI = Uri.parse("$BASE_URI/events")           // Enregistrement d'événements
val RULES_URI = Uri.parse("$BASE_URI/rules")             // Gestion des règles
val APPS_URI = Uri.parse("$BASE_URI/apps")               // Informations d'application
val STATS_URI = Uri.parse("$BASE_URI/stats")             // Données statistiques
val STATUS_URI = Uri.parse("$BASE_URI/status")           // État du système
val CONFIG_URI = Uri.parse("$BASE_URI/config")           // Gestion de la configuration
```

### Déclaration des permissions
```xml
<!-- Permission de lecture -->
<permission
    android:name="com.js.nowakelock.permission.READ_DATA"
    android:protectionLevel="signature" />

<!-- Permission d'écriture -->
<permission
    android:name="com.js.nowakelock.permission.WRITE_DATA"
    android:protectionLevel="signature" />

<!-- Déclaration du provider -->
<provider
    android:name=".provider.NoWakeLockProvider"
    android:authorities="com.js.nowakelock.provider"
    android:exported="false"
    android:readPermission="com.js.nowakelock.permission.READ_DATA"
    android:writePermission="com.js.nowakelock.permission.WRITE_DATA" />
```

## API d'enregistrement d'événements

### Insertion d'événements
```kotlin
// Types d'événements
object EventTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// Insérer un événement WakeLock
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

// Mettre à jour l'heure de fin d'événement
fun updateEventEndTime(
    context: Context,
    instanceId: String,
    endTime: Long = System.currentTimeMillis()
): Int {
    val values = ContentValues().apply {
        put("end_time", endTime)
        put("duration", endTime - startTime) // Calculer la durée
    }
    
    val uri = Uri.withAppendedPath(EVENTS_URI, instanceId)
    return context.contentResolver.update(uri, values, null, null)
}
```

### Requête d'événements
```kotlin
// Requête des enregistrements d'événements d'application
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

// Requête d'événements actifs
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

// Requête de statistiques d'événements (session actuelle)
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

## API de gestion des règles

### Opérations CRUD des règles
```kotlin
// Types de règles
object RuleTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// Types d'actions
object ActionTypes {
    const val ALLOW = 0
    const val LIMIT = 1
    const val BLOCK = 2
}

// Types de correspondance
object MatchTypes {
    const val EXACT = 0
    const val CONTAINS = 1
    const val REGEX = 2
    const val WILDCARD = 3
}

// Insérer une règle
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

// Mettre à jour une règle
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

// Supprimer une règle
fun deleteRule(context: Context, ruleId: String): Int {
    val uri = Uri.withAppendedPath(RULES_URI, ruleId)
    return context.contentResolver.delete(uri, null, null)
}

// Suppression en lot des règles d'application
fun deleteAppRules(context: Context, packageName: String): Int {
    return context.contentResolver.delete(
        RULES_URI,
        "package_name = ?",
        arrayOf(packageName)
    )
}
```

### Requête de règles
```kotlin
// Requête des règles d'application
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

// Requête des règles globales
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

// Requête de correspondance de règles
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

## API d'informations d'application

### Gestion des données d'application
```kotlin
// Insérer ou mettre à jour les informations d'application
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
    
    // Utiliser la stratégie REPLACE
    val uri = Uri.parse("$BASE_URI/apps/replace")
    return context.contentResolver.insert(uri, values)
}

// Requête d'informations d'application
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

// Requête de toutes les applications d'un utilisateur
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

// Rechercher des applications
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

## API de données statistiques

### Requête de statistiques de session actuelle
```kotlin
// Statistiques de session actuelle d'application
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

// Statistiques globales actuelles
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

// Classement des applications actives (session actuelle)
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

!!! warning "Cycle de vie des données important"
    Le stockage de données de NoWakeLock adopte une conception au niveau de la session :
    
    - **Données effacées après redémarrage** : BootResetManager nettoie toutes les données statistiques après détection du redémarrage de l'appareil
    - **Nettoyage d'initialisation XProvider** : InfoDatabase appelle clearAllTables() lors de la création
    - **Aucune donnée historique entre les redémarrages** : Les API de statistiques ne retournent que les données de la session actuelle
    
    Cette conception garantit la nature en temps réel des données statistiques et les performances du système.

## API d'état du système

### Interface de requête d'état
```kotlin
// Requête d'état du module
fun queryModuleStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/module")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Requête d'état des Hooks
fun queryHookStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/hooks")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Requête de métriques de performance
fun queryPerformanceMetrics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/performance")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Requête de statistiques de règles
fun queryRuleStatistics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/rules")
    return context.contentResolver.query(uri, null, null, null, null)
}
```

## API de gestion de configuration

### Interface de synchronisation de configuration
```kotlin
// Synchroniser la configuration vers le module Xposed
fun syncConfiguration(context: Context): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync")
    val values = ContentValues().apply {
        put("sync_time", System.currentTimeMillis())
        put("sync_type", "full")
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// Synchronisation incrémentale des règles
fun syncRule(context: Context, ruleId: String): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync_rule")
    val values = ContentValues().apply {
        put("rule_id", ruleId)
        put("sync_time", System.currentTimeMillis())
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// Requête d'état de synchronisation
fun querySyncStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/config/sync_status")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Réinitialiser la configuration
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

## API d'opérations en lot

### Traitement de données en lot
```kotlin
// Insertion en lot d'événements
fun bulkInsertEvents(context: Context, events: List<ContentValues>): Int {
    val uri = Uri.parse("$BASE_URI/events/bulk")
    return context.contentResolver.bulkInsert(uri, events.toTypedArray())
}

// Mise à jour en lot de règles
fun bulkUpdateRules(context: Context, updates: List<Pair<String, ContentValues>>): Int {
    val uri = Uri.parse("$BASE_URI/rules/bulk_update")
    
    // Utiliser ContentProviderOperation pour les opérations en lot
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

// Suppression en lot d'événements expirés
fun cleanupOldEvents(context: Context, cutoffTime: Long): Int {
    val uri = Uri.parse("$BASE_URI/events/cleanup")
    return context.contentResolver.delete(
        uri,
        "start_time < ?",
        arrayOf(cutoffTime.toString())
    )
}
```

## Modèle d'observateur

### Écoute des changements de données
```kotlin
// Enregistrer un écouteur de changements d'événements
class EventObserver(private val context: Context) : ContentObserver(Handler(Looper.getMainLooper())) {
    
    fun startObserving() {
        context.contentResolver.registerContentObserver(
            EVENTS_URI,
            true, // Écouter les URI enfants
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
                // Gérer les changements d'événements
                onEventChanged(uri)
            }
            uri.toString().startsWith(RULES_URI.toString()) -> {
                // Gérer les changements de règles
                onRuleChanged(uri)
            }
            uri.toString().startsWith(APPS_URI.toString()) -> {
                // Gérer les changements d'applications
                onAppChanged(uri)
            }
        }
    }
    
    private fun onEventChanged(uri: Uri) {
        // Implémenter la logique de gestion des changements d'événements
    }
    
    private fun onRuleChanged(uri: Uri) {
        // Implémenter la logique de gestion des changements de règles
        // Peut déclencher la synchronisation de configuration
        syncRule(context, uri.lastPathSegment ?: "")
    }
    
    private fun onAppChanged(uri: Uri) {
        // Implémenter la logique de gestion des changements d'applications
    }
}
```

## Gestion d'erreurs

### Gestion des situations exceptionnelles
```kotlin
object ContentProviderErrors {
    const val SUCCESS = 0
    const val PERMISSION_DENIED = 1
    const val INVALID_ARGUMENTS = 2
    const val DATABASE_ERROR = 3
    const val SYNC_FAILED = 4
    const val UNKNOWN_ERROR = 5
}

// Encapsuler les opérations de requête
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
        Result.failure(SecurityException("Permission refusée : ${e.message}"))
    } catch (e: IllegalArgumentException) {
        Result.failure(IllegalArgumentException("Arguments invalides : ${e.message}"))
    } catch (e: Exception) {
        Result.failure(Exception("Échec de la requête : ${e.message}"))
    }
}

// Encapsuler les opérations d'insertion
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

!!! info "Instructions d'utilisation de l'API"
    L'API ContentProvider fournit une interface complète d'accès aux données, prenant en charge l'enregistrement d'événements, la gestion des règles, les requêtes d'informations d'applications et les statistiques de session actuelle.

!!! warning "Exigences de permissions"
    L'utilisation de l'API ContentProvider nécessite les déclarations de permissions correspondantes, en s'assurant que l'appelant possède les permissions de lecture et d'écriture.

!!! tip "Recommandations de performance et gestion des données"
    - Pour les opérations de données volumineuses, il est recommandé d'utiliser les API en lot et le traitement asynchrone pour éviter de bloquer le thread principal
    - Utiliser ContentObserver pour écouter les changements de données afin de réaliser des mises à jour en temps réel
    - Notez que les données ne sont valides que pendant la session actuelle, toutes les données statistiques sont effacées après le redémarrage de l'appareil