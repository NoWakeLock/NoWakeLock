# Entwicklerdokumentation

Tiefer Einblick in die technische Implementierung von NoWakeLock, um zum Projekt beizutragen.

## Dokumentationsnavigation

### 🏗️ Architekturdesign
- [Systemübersicht](../architecture/overview.md) - Gesamtarchitektur und Designprinzipien
- [Xposed Hooks](../architecture/xposed-hooks.md) - Hook-System-Implementierung
- [Datenflussdesign](architecture/data-flow.md) - Datenfluss im System
- [Datenbankdesign](architecture/database.md) - Datenspeicher-Architektur

### ⚙️ Implementierungsdetails
- [Hook-Details](implementation/hook-details.md) - Spezifische Hook-Implementierung
- [Zählersystem](implementation/counter-system.md) - Statistikberechnungsmechanismus
- [Interprozess-Kommunikation](implementation/ipc.md) - Modul-Anwendung-Kommunikation

### 📚 API-Referenz
- [ContentProvider](api/content-provider.md) - Datenzugriffsschnittstelle
- [Interne API](api/internal-api.md) - Modulinterne Schnittstellen

## Schnellstart

### Umgebungsanforderungen
- **Android Studio** - Arctic Fox oder neuere Version
- **JDK** - 17 oder neuere Version
- **Android SDK** - API 24-35
- **Git** - Versionskontrolltool

### Quellcode abrufen
```bash
git clone https://github.com/NoWakeLock/NoWakeLock.git
cd NoWakeLock
git checkout dev
```

### Projekt erstellen
```bash
# Abhängigkeiten installieren
./gradlew clean

# Debug-Version erstellen
./gradlew assembleDebug

# Tests ausführen
./gradlew test
```

## Technologie-Stack

### Kerntechnologien
- **Kotlin** - Hauptprogrammiersprache
- **Jetpack Compose** - Modernes UI-Framework
- **Room** - Datenbank-Abstraktionsschicht
- **Coroutines** - Asynchrone Programmierung
- **Flow** - Reaktiver Datenfluss

### Xposed-Integration
- **LSPosed API** - Haupt-Hook-Framework
- **EdXposed-Kompatibilität** - Rückwärtskompatible Unterstützung
- **Reflection-Mechanismus** - Versionsübergreifende API-Anpassung

### Dependency Injection
- **Koin** - Leichtgewichtiges DI-Framework
- **ViewModel** - UI-Zustandsverwaltung
- **Repository-Muster** - Datenzugriffs-Abstraktion

## Kernmodule

### XposedHook-Modul
```
xposedhook/
├── XposedModule.kt      # Moduleingangspunkt
├── hook/               # Hook-Implementierung
│   ├── WakelockHook.kt
│   ├── AlarmHook.kt
│   └── ServiceHook.kt
└── model/              # Datenmodelle
    └── XpNSP.kt
```

### Datenschicht
```
data/
├── db/                 # Datenbank
│   ├── AppDatabase.kt
│   ├── InfoDatabase.kt
│   └── entity/
├── repository/         # Daten-Repository
└── counter/           # Zählersystem
```

### UI-Schicht
```
ui/
├── screens/           # Seitenkomponenten
├── components/        # Allgemeine Komponenten
├── theme/            # Themenstile
└── navigation/       # Navigationslogik
```

## Entwicklungsablauf

### Feature-Entwicklung
1. **Anforderungsanalyse** - Funktionsanforderungen und technische Lösungen klären
2. **Branch-Erstellung** - Feature-Branch vom `dev`-Branch erstellen
3. **Code-Implementierung** - Funktionen gemäß Codierungsstandards implementieren
4. **Unit-Tests** - Testfälle schreiben und ausführen
5. **Integrationstests** - Auf echten Geräten testen
6. **Code-Review** - Pull Request einreichen
7. **Merge und Release** - In `dev`-Branch mergen

### Bug-Behebung
1. **Problemreproduktion** - Reproduktionsschritte des Bugs bestätigen
2. **Root-Cause-Analyse** - Grundursache des Problems analysieren
3. **Fix-Implementierung** - Minimalen Reparaturcode schreiben
4. **Regressionstests** - Sicherstellen, dass Fix andere Funktionen nicht beeinträchtigt
5. **Release-Deployment** - Release-Zeitpunkt basierend auf Schweregrad wählen

## Code-Architektur

### MVVM-Architektur
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

### Repository-Muster
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

### Hook-Implementierungsmuster
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
                param.result = null // Aufruf abfangen
                return@findAndHookMethod
            }
            // Ursprünglichen Aufruf fortsetzen
        }
    }
}
```

## Teststrategie

### Unit-Tests
- **ViewModel-Tests** - Geschäftslogik-Tests
- **Repository-Tests** - Datenschicht-Tests
- **Utility-Tests** - Hilfsfunktions-Tests

### Integrationstests
- **Datenbank-Tests** - Room-Datenbankoperationen
- **Hook-Tests** - Xposed-Hook-Funktionalität
- **UI-Tests** - Compose-Interface-Tests

### Gerätetests
- **Kompatibilitätstests** - Multi-Version Android-Geräte
- **Leistungstests** - Speicher-, CPU-, Stromverbrauch
- **Stabilitätstests** - Langzeit-Lauftests

## Release-Prozess

### Versionsverwaltung
- **Hauptversion** - Große Funktionsupdates
- **Nebenversion** - Neue Funktionen hinzufügen
- **Revisionsversion** - Bug-Fixes

### Branch-Strategie
- **master** - Stabile Version
- **dev** - Entwicklungsversion
- **feature/*** - Feature-Branches
- **hotfix/*** - Notfall-Fixes

### CI/CD-Ablauf
```yaml
# GitHub Actions Workflow
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

## Debug-Techniken

### Xposed-Debugging
```kotlin
// XposedBridge.log für Debug-Ausgabe verwenden
XposedBridge.log("NoWakeLock: Hook executed")

// Bedingte Kompilierung für Debug-Code
if (BuildConfig.DEBUG) {
    XposedBridge.log("Debug info: ${param.args}")
}
```

### Protokollanalyse
```bash
# NoWakeLock-Protokolle filtern
adb logcat | grep -i nowakelock

# Leistungsmetriken überwachen
adb shell dumpsys meminfo com.js.nowakelock
adb shell top | grep nowakelock
```

## Community-Beiträge

### Beteiligungsweisen
- **Code-Beiträge** - Feature-Entwicklung und Bug-Fixes
- **Dokumentationsbeiträge** - Dokumentation und Tutorials verbessern
- **Test-Beiträge** - Geräte-Kompatibilitätstests
- **Übersetzungsbeiträge** - Mehrsprachige Unterstützung

### Kommunikationskanäle
- **GitHub Issues** - Problemberichte und Feature-Anfragen
- **GitHub Discussions** - Technische Diskussionen und Ideenaustausch
- **Telegram** - Echtzeit-Kommunikation und schneller Support
- **Discord** - Tiefgehende technische Diskussionen

### Code-Review
Alle Beiträge müssen durch Code-Review:
- Code-Qualitätsprüfung
- Sicherheitsbewertung
- Leistungsauswirkungsanalyse
- Kompatibilitätsverifikation

!!! info "Entwicklervereinbarung"
    Das Beitragen von Code bedeutet Zustimmung zur Open-Source-Lizenz des Projekts (GPL v3.0) und zur Contributor-Vereinbarung.

!!! tip "Anfängerfreundlich"
    Das Projekt begrüßt neue Beitragende. Wir haben als `good-first-issue` markierte einfache Aufgaben für Einsteiger.