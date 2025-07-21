# Alarm-Verwaltung

Alarm (geplante Aufgaben) ist der Timer-Mechanismus des Android-Systems, der verwendet wird, um Operationen zu bestimmten Zeiten auszulösen. Häufige Alarme beeinträchtigen die Gerätelebensdauer.

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

### Alarm-Zweck
- Zeitgesteuerte Aufgabenausführung
- Periodische Operationsauslösung
- System-Level-Timer
- Anwendungs-Keep-Alive-Mechanismus

### Verwaltungsziele
- Überwachung von Alarm-Einstellung und -Auslösung
- Identifizierung übermäßig häufiger geplanter Aufgaben
- Kontrolle der Alarm-Auslösefrequenz
- Reduzierung unnötiger Weckaktivitäten

## Oberflächenbeschreibung

### Alarm-Liste

【Screenshot erforderlich: Alarm-Listenseite】

**Listeninformationen**:
- **Label** - Alarm-Kennzeichnung
- **Anwendung** - Quellpaketname
- **Typ** - Alarm-Typ-Symbol
- **Status** - Abfangstatus
- **Statistiken** - Auslöseanzahl und Zeitinformationen

### Statusanzeige

| Status | Symbol | Beschreibung |
|--------|--------|--------------|
| Erlauben | 🟢 | Normal auslösen |
| Begrenzen | 🟡 | Auslösefrequenz reduzieren |
| Abfangen | 🔴 | Auslösung blockieren |
| Ausstehend | ⏰ | Eingestellt und wartet auf Auslösung |

## Alarm-Typen

### Klassifizierung nach Auslösebedingungen

| Typ | Beschreibung | Typische Verwendung |
|-----|--------------|-------------------|
| RTC | Absolute Zeitauslösung | Wecker, Erinnerungen |
| RTC_WAKEUP | Absolute Zeit weckt Gerät | Wichtige Benachrichtigungen |
| ELAPSED_REALTIME | Relative Zeitauslösung | Periodische Prüfungen |
| ELAPSED_REALTIME_WAKEUP | Relative Zeit-Weckung | Hintergrundaufgaben |

### Klassifizierung nach Wiederholungsmuster

**Einmalige Alarme**:
- Werden nach einmaliger Ausführung automatisch abgebrochen
- Verwendet für Aufgaben zu bestimmten Zeiten

**Wiederholende Alarme**:
- Wiederholen sich in festen Intervallen
- Häufig für Synchronisations- und Aktualisierungsaufgaben

**Präzise Alarme**:
- Genaue Zeitauslösung
- Höherer Systemressourcenverbrauch

## Konfigurationsoptionen

### Behandlungsmodi

#### Erlauben-Modus
- Alarm normale Einstellung und Auslösung
- Keine Intervention
- Geeignet für wichtige Systemfunktionen

#### Begrenzen-Modus
- Auslösefrequenz reduzieren
- Nahe Auslösezeiten zusammenführen
- Nicht-dringende Alarme verzögern

#### Abfangen-Modus
- Alarm-Einstellung vollständig blockieren
- Anwendung kann diesen Alarm-Typ nicht erstellen
- Kann Anwendungsfunktionen schwerwiegend beeinträchtigen

### Erweiterte Optionen

**Intelligente Zusammenführung**:
- Alarme mit ähnlichen Zeiten zusammenführen
- Reduzierung der Geräteweckungen

**Batch-Verarbeitungsmodus**:
- Nicht-dringende Alarme verzögern
- Zusammen mit anderen Aufgaben ausführen

## Verwendungsmethode

### Alarm-Liste anzeigen

1. Klicken Sie auf die "Alarms"-Registerkarte unten
2. Aktuelle aktive Alarme anzeigen
3. Filter verwenden, um spezifische Status anzuzeigen

### Alarm-Regeln konfigurieren

1. Klicken Sie auf das Ziel-Alarm-Element
2. Behandlungsmodus wählen
3. Spezifische Parameter setzen:
   - Mindestintervallzeit
   - Verzögerungszeit
   - Batch-Verarbeitungsoptionen

【Screenshot erforderlich: Alarm-Konfigurationsseite】

### Massenverwaltung

**Masseneinstellung nach Anwendung**:
1. Spezifische Anwendungsalarme filtern
2. Massenoperation wählen
3. Einheitliche Regeln anwenden

**Masseneinstellung nach Typ**:
- Alle WAKEUP-Typen begrenzen
- Alle wiederkehrenden Alarme reduzieren
- Systemalarme vorsichtig behandeln

## Praktische Anwendung

### Problemidentifikation

#### Charakteristika abnormaler Alarme

**Hochfrequente Auslösung**:
- Wiederholende Alarme mit Intervallen unter 1 Minute
- Häufige Auslösung in nächtlichen Stunden
- Läuft noch wenn Gerät ruhig ist

## Technische Implementierung

### Hook-Mechanismus

Abfangen von Schlüsselmethoden des AlarmManagerService:
```kotlin
// System-Alarm-Einstellungsaufruf
setImpl(
    int type,
    long triggerAtTime,
    long windowLength,
    long interval,
    PendingIntent operation,
    IAlarmListener directReceiver,
    String listenerTag,
    WorkSource workSource,
    AlarmManager.AlarmClockInfo alarmClock,
    int callingUid,
    String callingPackage
)

// Alarm-Auslösebehandlung
triggerAlarmsLocked(ArrayList<Alarm> triggerList)
```

### Datenverarbeitung

**Echtzeitverarbeitung**:
- Regelprüfung bei Alarm-Einstellung
- Frequenzkontrolle bei Auslösung
- Dynamische Anpassung der Auslösezeit

**Verlaufsdatensätze**:
- Datenbank speichert Auslöseverlauf
- Statistische Analyse und Trendberechnung
- Automatische Bereinigung abgelaufener Daten

### Kompatibilität

**Versionsunterstützung**:
- Android 7.0+ vollständige Unterstützung
- API-Anpassung verschiedener Versionen
- Downgrade-Kompatibilitätsstrategie

**Leistungsoptimierung**:
- Minimierung des Hook-Aufwands
- Effizienter Regel-Matching-Algorithmus
- Asynchrone Verarbeitung von Statistikdaten

## Verwandte Funktionen

- [Anwendungsverwaltung](app-management.md) - Alle Alarme einer Anwendung anzeigen
- [WakeLock-Verwaltung](wakelocks.md) - Kombiniert mit WakeLock-Optimierung
- [Regelsystem](rules-regex.md) - Massenkonfiguration mit regulären Ausdrücken

!!! tip "Optimierungsempfehlung"
    Alarm-Optimierung zeigt deutliche Effekte, erfordert aber ein Gleichgewicht mit der Funktionalität. Es wird empfohlen, mit nicht-kritischen Anwendungen zu beginnen und schrittweise wichtige Anwendungseinstellungen anzupassen.

!!! warning "Hinweise"
    Übermäßige Einschränkung von System-Alarmen kann normale Gerätefunktionen beeinträchtigen, wie automatische Zeitsynchronisation, Systemupdate-Prüfungen etc.