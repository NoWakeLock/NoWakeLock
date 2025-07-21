# Service-Verwaltung

Service (Hintergrunddienste) sind Android-Anwendungskomponenten, die lang laufende Aufgaben im Hintergrund ausführen. Zu viele Hintergrunddienste verbrauchen Systemressourcen und Strom.

!!! danger "⚠️ Rettungsmodus - Wichtige Erinnerung!"
    **Falls falsche WakeLock-Konfiguration dazu führt, dass das Gerät nicht startet**:
    
    **Situation 1: LSPosed-Framework-Problem (Problem tritt nach Installation auf, bevor Konfiguration erfolgt)**:
    1. Halten Sie die Ein-/Aus-Taste 10 Sekunden lang für einen Neustart gedrückt
    2. Sobald der Bildschirm schwarz wird, drücken Sie sofort wiederholt eine beliebige Hardware-Taste
    3. Nach 2 kurzen Vibrationen drücken Sie schnell 4 Mal dieselbe Taste
    4. Nach der 4. Tastenbetätigung sollten Sie eine lange Vibration spüren - LSPosed ist deaktiviert
    5. Nach normalem Start deaktivieren Sie das NoWakeLock-Modul in LSPosed
    
    **Situation 2: Fehlkonfigurationsproblem (Recovery-Zugriff möglich)**:
    1. Gehen Sie zu Recovery → Dateimanager
    2. Navigieren Sie zu /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock
       (xxx-xxx-xxx ist eine lange zufällige Zeichenfolge, die auf jedem Gerät unterschiedlich sein kann)
    3. Löschen Sie den gesamten Ordner
    4. Starten Sie das Gerät neu
    
    **Bei ungewisser Ursache**: Löschen Sie direkt die NoWakeLock-Anwendungsdaten und vermeiden Sie beim Neukonfigurieren das Abfangen kritischer Systemkomponenten.

## Funktionsübersicht

### Service-Zweck
- Hintergrunddatenverarbeitung
- Netzwerkkommunikation und Downloads
- Musik-Wiedergabe und andere Mediendienste
- Systemüberwachung und -wartung

### Verwaltungsziele
- Überwachung von Service-Start- und -Bindungsverhalten
- Kontrolle unnötiger Hintergrunddienste
- Optimierung der Service-Startfrequenz
- Reduzierung des Ressourcenverbrauchs

## Oberflächenbeschreibung

### Service-Liste

【Screenshot erforderlich: Service-Listenseite】

**Listeninformationen**:
- **Service-Name** - Service-Klassenname
- **Anwendung** - Quellpaketname
- **Typ** - Service-Typ-Symbol
- **Status** - Laufstatus und Abfangeinstellungen
- **Statistiken** - Startanzahl und Laufzeit

### Statusanzeige

| Status | Symbol | Beschreibung |
|--------|--------|--------------|
| Erlauben | 🟢 | Normal starten und laufen |
| Begrenzen | 🟡 | Startfrequenz begrenzen |
| Abfangen | 🔴 | Start blockieren |
| Läuft | ▶️ | Derzeit laufend |
| Gestoppt | ⏹️ | Service gestoppt |

### Service-Typen

**Vordergrunddienste**:
- Zeigen dauerhafte Benachrichtigungen an
- Benutzersichtbare Dienste
- Wie Musik-Wiedergabe, Navigation

**Hintergrunddienste**:
- Keine Benutzeroberfläche
- Führen Aufgaben im Stillen aus
- Wie Datensynchronisation, Bereinigung

**Gebundene Dienste**:
- Mit anderen Komponenten verbunden
- Bieten Schnittstellenaufrufe
- Lebenszyklus mit Bindern verknüpft

## Konfigurationsoptionen

### Behandlungsmodi

#### Erlauben-Modus
- Service normal starten und laufen
- Keine Einschränkungen
- Geeignet für wichtige Funktionsdienste

#### Begrenzen-Modus
- Startfrequenz kontrollieren
- Anzahl gleichzeitig laufender Services begrenzen
- Automatisches Stoppen lang laufender Services

#### Abfangen-Modus
- Service-Start vollständig blockieren
- Einschließlich expliziter und impliziter Starts
- Kann Kernfunktionen der Anwendung beeinträchtigen

### Erweiterte Optionen

**Intelligente Planung**:
- Nicht-dringende Service-Starts verzögern
- Services mit ähnlichen Funktionen zusammenführen
- Anpassung basierend auf Systemlast

**Ressourcenbegrenzung**:
- CPU-Nutzungsratenbegrenzung
- Speicherverbrauchskontrolle
- Netzwerkverkehrsbegrenzung

## Verwendungsmethode

### Service-Liste anzeigen

1. Klicken Sie auf die "Services"-Registerkarte unten
2. Alle erkannten Services anzeigen
3. Filter verwenden, um spezifische Status oder Anwendungen anzuzeigen

### Service-Regeln konfigurieren

1. Klicken Sie auf das Ziel-Service-Element
2. Behandlungsmodus wählen
3. Spezifische Begrenzungsparameter setzen:
   - Start-Intervallzeit
   - Maximale Laufzeit
   - Ressourcennutzungsbegrenzungen

【Screenshot erforderlich: Service-Konfigurationsseite】

### Massenverwaltung

**Nach Anwendung filtern**:
- Alle Services einer spezifischen Anwendung anzeigen
- Batch-Einstellung von Anwendungsebene-Regeln

**Nach Typ filtern**:
- Vordergrunddienste separat verwalten
- Hintergrunddienste einheitlich begrenzen

## Praktische Anwendung

### Problemidentifikation

#### Charakteristika abnormaler Services

**Häufige Starts**:
- Start-Intervalle unter 10 Sekunden
- Wiederholte Starts desselben Services in kurzer Zeit
- Service-Starts wenn Anwendung nicht verwendet wird

**Ressourcenverbrauch**:
- Lang laufend (über 30 Minuten)
- Hohe CPU-Nutzung (> 5%)
- Großer Speicherverbrauch (> 100MB)

**Ungültige Services**:
- Start und sofortiger Stopp
- Leere Services ohne tatsächliche Funktion
- Services nur für Keep-Alive

## Technische Implementierung

### Hook-Mechanismus

Abfangen von Service-Verwaltungsmethoden des ActivityManagerService:
```kotlin
// Service-Start-Abfangung
startServiceLocked(
    IApplicationThread caller,
    Intent service,
    String resolvedType,
    int callingPid,
    int callingUid,
    boolean fgRequired,
    String callingPackage,
    int userId
)

// Service-Bindungs-Abfangung  
bindServiceLocked(
    IApplicationThread caller,
    IBinder token,
    Intent service,
    String resolvedType,
    IServiceConnection connection,
    int flags,
    String callingPackage,
    int userId
)
```

### Datenverfolgung

**Echtzeitüberwachung**:
- Aufzeichnung von Informationen beim Service-Start
- Verfolgung des Service-Laufstatus
- Berechnung des Ressourcenverbrauchs

**Verlaufsstatistiken**:
- Datenbank speichert Service-Verlauf
- Analyse von Startmustern und Trends
- Generierung von Optimierungsempfehlungen

### Kompatibilitätsbehandlung

**Versionsanpassung**:
- Anpassung an Android 8.0+ Hintergrunddienst-Beschränkungen
- Behandlung von API-Unterschieden verschiedener Versionen
- Spezielle Behandlung von Vordergrunddiensten

**Leistungsoptimierung**:
- Minimierung des Hook-Aufrufaufwands
- Effizienter Regel-Matching
- Asynchrone Verarbeitung von Statistikdaten

## Verwandte Funktionen

- [Anwendungsverwaltung](app-management.md) - Alle Services nach Anwendung anzeigen
- [WakeLock-Verwaltung](wakelocks.md) - Service-bezogene WakeLocks
- [Regelsystem](rules-regex.md) - Massenkonfiguration mit regulären Ausdrücken

!!! info "Android 8.0+ Änderungen"
    Ab Android 8.0 werden Hintergrunddienste eingeschränkt, das System stoppt automatisch die meisten Hintergrunddienste. Die Service-Verwaltung von NoWakeLock zielt hauptsächlich auf Vordergrunddienste und gebundene Dienste ab.

!!! warning "Vorsichtige Behandlung"
    Das Abfangen kritischer Services kann zu abnormalen Anwendungsfunktionen führen. Es wird empfohlen, zunächst den Begrenzen-Modus zu verwenden und erst nach Bestätigung, dass keine Auswirkungen vorliegen, das Abfangen zu erwägen.