# Systemübersicht

NoWakeLock verwendet moderne Android-Entwicklungsarchitektur und kombiniert die systemweiten Hook-Fähigkeiten des Xposed-Frameworks mit dem deklarativen UI-Design von Jetpack Compose.

## Gesamtarchitektur

### Architekturdiagramm

```
┌─────────────────────────────────────────────────────────┐
│                    Android System                        │
├─────────────────────────────────────────────────────────┤
│  PowerManagerService │ AlarmManagerService │ ActiveServices │
│         ↓ Hook             ↓ Hook               ↓ Hook     │
├─────────────────────────────────────────────────────────┤
│                 Xposed-Modulschicht                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │WakelockHook │ │ AlarmHook   │ │ ServiceHook │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
├─────────────────────────────────────────────────────────┤
│                   Anwendungsschicht                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │ Presentation│ │  Domain     │ │    Data     │        │
│  │   (UI)      │ │ (Business)  │ │ (Storage)   │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
└─────────────────────────────────────────────────────────┘
```

## Technologie-Stack

### Kerntechnologien
- **Kotlin** 1.9.25 - Hauptprogrammiersprache
- **Jetpack Compose** 2025.04.01 - Deklaratives UI-Framework
- **Room** 2.7.1 - Datenbank-ORM-Framework
- **Coroutines** - Asynchrone Programmierung
- **Flow** - Reaktiver Datenfluss

### Xposed-Integration
- **Xposed API** 82 - Systemweites Hook-Framework
- **LSPosed** - Haupt-Ziel-Framework
- **EdXposed** - Rückwärtskompatible Unterstützung
- **Reflection-Mechanismus** - Versionsübergreifende API-Anpassung

### Dependency Injection
- **Koin** 4.0.4 - Leichtgewichtiges DI-Framework
- **Modulare Konfiguration** - Funktionsgruppierte Injection
- **ViewModel-Verwaltung** - Automatische Lebenszyklus-Bindung

## Architektur-Schichtung

### 1. Xposed-Modulschicht
```kotlin
// Einstiegspunkt
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

**Verantwortlichkeiten**:
- Systemdienst-Hook-Abfangung
- Interprozess-Datenkommunikation
- Versionskompatibilitätsbehandlung

### 2. Datenschicht (Data Layer)

#### Dual-Datenbank-Architektur
```kotlin
// Haupt-Business-Datenbank
@Database(
    entities = [AppInfo::class, WakelockRule::class],
    version = 13
)
abstract class AppDatabase : RoomDatabase()

// Ereignisaufzeichnungs-Datenbank  
@Database(
    entities = [InfoEvent::class],
    version = 12
)
abstract class InfoDatabase : RoomDatabase()
```

**Verantwortlichkeiten**:
- Anwendungsinformationsverwaltung
- Ereignisdatenaufzeichnung
- Regelkonfigurationsspeicherung
- Benutzereinstellungen

#### Repository-Muster
```kotlin
interface DARepository {
    fun getApps(userId: Int): Flow<List<AppDas>>
    suspend fun updateRule(rule: Rule)
}

class DARepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val xProvider: XProvider
) : DARepository {
    // Datenzugriffslogik implementieren
}
```

**Eigenschaften**:
- Einheitliche Datenzugriffsschnittstelle
- Lokale Datenbank + Xposed-Datenquelle
- Reaktiver Datenfluss

### 3. Geschäftsschicht (Domain Layer)

#### ViewModel-Architektur
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

**Eigenschaften**:
- MVVM-Architekturmuster
- StateFlow-Zustandsverwaltung
- Reaktive Datenbindung

### 4. Präsentationsschicht (Presentation Layer)

#### Compose UI-Architektur
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

**Eigenschaften**:
- Deklarative UI-Komponenten
- Typsichere Navigation
- Material Design 3

## Kernfunktionen

### 1. Mehrbenutzunterstützung
```kotlin
class UserManager {
    fun getCurrentUsers(): List<User> {
        return UserManagerService.getUsers()
    }
    
    fun switchUser(userId: Int) {
        // Benutzerkontext wechseln
    }
}
```

### 2. Versionskompatibilität
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

### 3. Leistungsoptimierung
- **Parameter-Position-Cache** - Reflection-Overhead reduzieren
- **Flow distinctUntilChanged** - Wiederholte Updates vermeiden
- **Datenbankindizes** - Abfrageleistung optimieren
- **Lazy Loading** - Daten nach Bedarf laden

## Datenflussdesign

### Ereignisverarbeitungsablauf
```
Systemaufruf → Hook-Abfangung → Regelübereinstimmung → Aktion ausführen → Ereignis aufzeichnen → UI aktualisieren
    ↓         ↓         ↓         ↓         ↓        ↓
PowerManager → WakelockHook → RuleEngine → Block/Allow → InfoEvent → Flow-Update
```

### Zustandsverwaltung
```kotlin
data class DAsUiState(
    val apps: List<AppDas> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterOption: FilterOption = FilterOption.ALL,
    val sortOption: SortOption = SortOption.NAME
)
```

## Kommunikationsmechanismus

### 1. Xposed ↔ Anwendungskommunikation
- **XProvider** - ContentProvider-Interprozess-Kommunikation
- **SharedPreferences** - Konfigurationsdatenfreigabe
- **Dateisystem** - Temporärer Datenaustausch

### 2. Komponenten-interne Kommunikation
- **Repository-Muster** - Datenschicht-Abstraktion
- **StateFlow** - Reaktive Zustandsfreigabe
- **Navigation** - Parameterübertragung zwischen Seiten

## Erweiterungspunkte

### 1. Neue Hook-Typen hinzufügen
```kotlin
// Neue Hook-Klasse erstellen
object NewFeatureHook {
    fun hook(lpparam: LoadPackageParam) {
        // Hook-Implementierung
    }
}

// In XposedModule registrieren
NewFeatureHook.hook(lpparam)
```

### 2. Neue UI-Seiten hinzufügen
```kotlin
// Neue Route hinzufügen
@Serializable
data class NewFeature(val param: String = "")

// Navigation und Seite hinzufügen
composable<NewFeature> { NewFeatureScreen() }
```

### 3. Neue Datenquellen integrieren
```kotlin
// Repository-Schnittstelle erweitern
interface ExtendedRepository : DARepository {
    fun getNewData(): Flow<List<NewData>>
}
```

## Designprinzipien

### 1. Single Responsibility
Jede Klasse und jedes Modul hat klare Verantwortungsgrenzen, was Wartung und Tests erleichtert.

### 2. Dependency Inversion
Hochrangige Module hängen nicht von niedrigrangigen Modulen ab, beide hängen von abstrakten Schnittstellen ab.

### 3. Open-Closed-Prinzip
Offen für Erweiterungen, geschlossen für Modifikationen, erleichtert das Hinzufügen neuer Funktionen.

### 4. Reaktives Design
Verwendung von Flow und StateFlow für reaktiven Datenfluss, gewährleistet UI-Daten-Synchronisation.

!!! info "Architekturvorteile"
    Dieses geschichtete Architekturdesign ermöglicht es NoWakeLock, sowohl komplexe systemweite Operationen zu handhaben als auch gute Codeorganisation und Wartbarkeit zu bewahren.

!!! warning "Hinweise"
    Änderungen an systemweiten Hooks erfordern sorgfältige Behandlung der Versionskompatibilität. Umfassende Tests auf mehreren Android-Versionen werden empfohlen.