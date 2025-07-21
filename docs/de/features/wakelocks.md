# WakeLock-Verwaltung

WakeLock (Wach-Sperre) verhindert, dass das Gerät in den Ruhezustand wechselt und ist ein Schlüsselmechanismus im Android-System, der die Akkulebensdauer beeinflusst.

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

## WakeLock-Typen

### Haupttypen

| Typ | Konstantenwert | Beschreibung | Akkuauswirkung |
|-----|----------------|--------------|----------------|
| PARTIAL_WAKE_LOCK | 1 | CPU läuft weiter | Hoch |
| SCREEN_DIM_WAKE_LOCK | 6 | Bildschirm gedimmt aber nicht aus (veraltet) | Mittel |
| SCREEN_BRIGHT_WAKE_LOCK | 10 | Bildschirm behält Helligkeit (veraltet) | Hoch |
| FULL_WAKE_LOCK | 26 | CPU + Bildschirm voll an (veraltet) | Sehr hoch |
| PROXIMITY_SCREEN_OFF_WAKE_LOCK | 32 | Näherungssensor-Kontrolle | Niedrig |

!!! warning "Typerklärung"
    Außer PARTIAL_WAKE_LOCK und PROXIMITY_SCREEN_OFF_WAKE_LOCK sind andere Typen seit Android API 17 veraltet. Moderne Anwendungen verwenden hauptsächlich PARTIAL_WAKE_LOCK.

### Spezielle Kennungen

**System-WakeLocks**:
- `PowerManagerService.WakeLocks`
- `AlarmManager`
- `AudioMix`

**Netzwerkbezogen**:
- `WifiManager`
- `ConnectivityService`

**Standortdienste**:
- `LocationManagerService`
- `GpsLocationProvider`

## Oberflächenbeschreibung

### WakeLock-Liste

【Screenshot erforderlich: WakeLock-Listenseite】

**Listeninformationen**:
- **Name** - WakeLock-Kennzeichnung
- **Anwendung** - Quellpaketname
- **Typ** - WakeLock-Typ-Symbol
- **Status** - Aktueller Statusindikator
- **Statistiken** - Erwerbsanzahl und kumulierte Dauer

### Statusindikatoren

| Status | Symbol | Beschreibung |
|--------|--------|--------------|
| Erlauben | 🟢 | Normal laufend, keine Einschränkungen |
| Begrenzen | 🟡 | Timeout gesetzt |
| Abfangen | 🔴 | Erwerb vollständig blockiert |
| Aktiv | ⚡ | Derzeit gehalten |

### Filtern und Sortieren

**Filteroptionen**:
- Alle
- Erlauben
- Begrenzen  
- Abfangen
- Derzeit aktiv

**Sortierweisen**:
- Nach Name
- Nach Anwendung
- Nach Erwerbsanzahl
- Nach kumulierter Dauer
- Nach letzter Aktivitätszeit

## Konfigurationsoptionen

### Behandlungsmodi

#### Erlauben-Modus
- Keine Einschränkungen
- WakeLock normale Erwerbs- und Freigabe
- Standardmodus, geeignet für die meisten Situationen

#### Begrenzen-Modus
- Maximale Haltezeit festlegen
- Nach Timeout erzwungene Freigabe
- Nur verwenden, wenn durch BBS bestätigt wurde, dass ein WakeLock zu lange andauert

!!! warning "Timeout-Einstellungsprinzip"
    Die Timeout-Zeit muss basierend auf tatsächlichen BBS-Analysedaten bestimmt werden, nicht mit voreingestellten Werten. Beobachten Sie die normale Dauer dieses WakeLocks und setzen Sie dann ein Timeout etwas größer als der normale Wert.

#### Abfangen-Modus
- WakeLock-Erwerb vollständig blockieren
- Anwendung kann diesen WakeLock nicht halten
- Nur verwenden, wenn bestätigt ist, dass der WakeLock völlig unnötig ist und die Batterie stark beeinflusst

## Verwendungsmethode

### Grundlegender Arbeitsablauf

!!! warning "Wichtig: Vor Konfiguration lesen"
    1. **Erst diagnostizieren, dann konfigurieren** - Verwenden Sie BBS zur Problembestätigung vor Konfiguration
    2. **Einzelbehandlung** - Konfigurieren Sie gezielt für spezifische Probleme, vermeiden Sie Massenoperationen
    3. **Kontinuierliche Überwachung** - Verwenden Sie nach der Konfiguration weiterhin BBS zur Effektvalidierung

### WakeLocks anzeigen und analysieren

1. Klicken Sie auf die "Wakelocks"-Registerkarte unten
2. Durchsuchen Sie die aktuelle Liste und Statistikdaten
3. Analysieren Sie abnormale Elemente in Kombination mit BBS-Daten

### Gezielte Konfiguration

1. **Problem bestätigen** - Basierend auf BBS-Analyseergebnissen
2. **Ziel-WakeLock anklicken** zur Konfigurationsseite
3. **Minimale Intervention wählen** - Bevorzugen Sie den Begrenzen-Modus
4. **Parameter setzen** - Basierend auf tatsächlich beobachteten Daten
5. **Funktionen überprüfen** - Bestätigen Sie, dass Anwendungsfunktionen normal sind

!!! danger "Massenkonfiguration verboten"
    Verwenden Sie nicht die Massenoperationsfunktion für voreingestellte Konfigurationen. Jedes WakeLock-Problem ist spezifisch und erfordert individuelle Analyse und Behandlung.

## Effektvalidierung

### Überwachung nach Konfiguration

**Notwendige Schritte**:
1. **Datenüberwachung fortsetzen** der Effekte nach Konfiguration
   - Priorisieren Sie BBS für umfassende Bewertung
   - Verwenden Sie auch NoWakeLock integrierte Statistiken als Referenz
2. **Anwendungsfunktionen überprüfen** - Bestätigen Sie, dass alle Anwendungsfunktionen noch normal sind
3. **Akkuverbesserung bewerten** - Vergleichen Sie den tatsächlichen Akkuverbrauch vor und nach der Konfiguration

**Rollback-Vorbereitung**:
- Wenn Anwendungsfunktionen beeinträchtigt sind, brechen Sie die Konfiguration sofort ab
- Wenn die Akkuverbesserung nicht offensichtlich ist, bewerten Sie erneut, ob Einschränkungen notwendig sind

## Technische Details

### Hook-Implementierung

NoWakeLock fängt Schlüsselmethoden in PowerManagerService ab:

```kotlin
// Haupt-Hook-Methoden (Parameter variieren je nach Android-Version)
acquireWakeLockInternal(...)
releaseWakeLockInternal(...)
```

**Versionskompatibilitätsbehandlung**:
- Verwendung von Parameter-Position-Cache-Mechanismus
- Unterstützung verschiedener Methodensignaturen von Android 7.0-15.0
- Automatische Erkennung und Anpassung von Parameterpositionen

### Kompatibilitätsbehandlung

**Versionsanpassung**:
- Unterstützt Android 7.0-15.0
- Dynamische Parameter-Positionserkennung
- Downgrade-Strategiebehandlung

**Leistungsoptimierung**:
- Hook-Aufrufaufwand < 1ms
- Regel-Matching verwendet Cache
- Asynchrone Verarbeitung von Statistikdaten

### Datenspeicherung

**Echtzeitdaten**:
- Derzeit aktive WakeLocks
- Im Speicher zwischengespeichert, nach Neustart gelöscht

**Sitzungsstatistiken**:
- WakeLock-Aktivitätsdatensätze der aktuellen Sitzung
- Temporäre Datenbankspeicherung, nach Geräteneustart geleert

## Verwandte Funktionen

- [Anwendungsverwaltung](app-management.md) - Alle WakeLocks nach Anwendung anzeigen
- [Regelsystem](rules-regex.md) - Massenkonfiguration mit regulären Ausdrücken

!!! warning "Nutzungsempfehlung"
    Das Ändern von WakeLocks kritischer Systemdienste kann die Gerätestabilität beeinträchtigen. Es wird empfohlen, mit Drittanbieter-Anwendungen zu beginnen und schrittweise Systemdienste anzupassen.