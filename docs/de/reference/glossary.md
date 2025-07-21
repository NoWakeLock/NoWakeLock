# Glossar

Erklärung technischer Begriffe und Konzepte im Zusammenhang mit NoWakeLock.

## Kernkonzepte

### WakeLock (Wach-Sperre)
Mechanismus zur Verhinderung des Wechsels von Android-Geräten in den Ruhezustand. Anwendungen halten WakeLocks, um die CPU am Laufen zu halten oder den Bildschirm anzulassen.

**Typen**:
- **PARTIAL_WAKE_LOCK** - Hält CPU am Laufen, Bildschirm kann ausgeschaltet werden
- **SCREEN_DIM_WAKE_LOCK** - Hält Bildschirm an, erlaubt aber Abdunklung
- **SCREEN_BRIGHT_WAKE_LOCK** - Hält Bildschirm vollständig hell
- **FULL_WAKE_LOCK** - Hält sowohl CPU als auch Bildschirm am Laufen

### Alarm (Geplante Aufgaben)
Timer-Service des Android-Systems, der es Anwendungen ermöglicht, Aufgaben zu bestimmten Zeiten oder in Intervallen auszuführen.

**Typen**:
- **RTC** - Auf echter Zeit basierender Timer
- **RTC_WAKEUP** - Auf echter Zeit basierend und weckt Gerät
- **ELAPSED_REALTIME** - Auf Gerätestartzeit basierend
- **ELAPSED_REALTIME_WAKEUP** - Auf Startzeit basierend und weckt Gerät

### Service (Dienst)
Im Hintergrund laufende Android-Anwendungskomponente, die keine Benutzeroberfläche bereitstellt.

**Typen**:
- **Vordergrunddienst** - Führt benutzersichtbare Aufgaben aus, zeigt dauerhafte Benachrichtigung
- **Hintergrunddienst** - Führt für Benutzer nicht direkt wahrnehmbare Aufgaben aus
- **Gebundener Dienst** - Bietet Client-Server-Schnittstelle

## Android-System

### Doze Mode (Ruhezustand)
In Android 6.0+ eingeführter Energiesparmechanismus, Gerät wechselt bei Stillstand in tiefen Schlafzustand.

### App Standby (App-Bereitschaft)
Energiespareinschränkungen des Systems für längere Zeit nicht verwendete Anwendungen.

### Background Execution Limits (Hintergrundausführungsbeschränkungen)
Android 8.0+ Beschränkungen für Hintergrunddienste und Broadcast-Empfänger.

### SELinux (Sicherheits-erweiterte Linux)
Mandatory Access Control Sicherheitsmechanismus des Android-Systems.

## Xposed-Framework

### Xposed Framework
Framework, das es ermöglicht, System- und Anwendungsverhalten zu ändern, ohne APKs zu modifizieren.

### Hook (Haken)
Technik zum Abfangen und Modifizieren von Funktionsaufrufen.

### LSPosed
Moderne Xposed-Implementierung basierend auf Riru, unterstützt Android 8.1+.

### EdXposed
Xposed-Implementierung basierend auf YAHFA und SandHook.

### Zygote
Elternprozess aller Anwendungsprozesse im Android-System.

## NoWakeLock-Begriffe

### Abfangmodi
- **Erlauben** - Keine Einschränkungen, normale Ausführung
- **Begrenzen** - Zeit- oder Frequenzeinschränkungen setzen
- **Abfangen** - Operation vollständig blockieren

### Regelsystem
Auf Musterübereinstimmung basierender Konfigurationsmechanismus, unterstützt reguläre Ausdrücke.

### Komponenten
Sammelbezeichnung für WakeLock, Alarm, Service.

### Anwendungsbereich
Anwendungsbereich, in dem das Xposed-Modul wirksam wird.

### DA
Abkürzung für Detection/Action, bezieht sich auf von NoWakeLock erkannte WakeLock-, Alarm-, Service-Aktivitäten.

## Leistungsindikatoren

### Erwerbsanzahl
Gesamtzahl der WakeLock-Erwerbe.

### Kumulierte Dauer
Gesamtzeit, die WakeLock gehalten wurde.

### Auslösefrequenz
Durchschnittliches Auslöseintervall von Alarmen.

### Startanzahl
Gesamtzahl der Service-Starts.

### Abfangrate
Prozentsatz der abgefangenen Operationen zu Gesamtoperationen.

## Technische Begriffe

### API Level
API-Stufennummer entsprechend der Android-Version.

### Package Name
Eindeutige Kennung einer Anwendung, wie `com.example.app`.

### UID (Benutzerkennung)
Vom System jeder Anwendung zugewiesene eindeutige numerische Kennung.

### PID (Prozesskennung)
Vom System jedem Prozess zugewiesene eindeutige numerische Kennung.

### ContentProvider
Eine der vier Hauptkomponenten von Android, verwendet für anwendungsübergreifende Datenfreigabe.

### IPC (Interprozess-Kommunikation)
Datenaustauschsmechanismus zwischen verschiedenen Prozessen.

### JNI (Java Native Interface)
Schnittstelle für Java-Code zum Aufrufen von nativem C/C++-Code.

## Datenbankbegriffe

### Room
Offizielle SQLite-Abstraktionsschicht-Framework von Google.

### DAO (Data Access Object)
Schnittstelle, die Datenbankoperationen kapselt.

### Entity (Entität)
Objektzuordnung von Datenbanktabellen.

### Migration (Migration)
Behandlungsmechanismus für Datenbankversions-Upgrades.

## Entwicklungsbegriffe

### Kotlin
Moderne JVM-Programmiersprache, bevorzugte Sprache für Android-Entwicklung.

### Jetpack Compose
Modernes deklaratives UI-Toolkit für Android.

### Coroutines (Koroutinen)
Asynchroner Programmiermechanismus von Kotlin.

### Flow
Reaktives Datenfluss-Framework von Kotlin.

### ViewModel
Android-Architekturkomponente zur Verwaltung UI-bezogener Daten.

### LiveData
Beobachtbare Datenhalterklasse mit Lebenszyklusbewusstsein.

### Koin
Leichtgewichtiges Dependency-Injection-Framework.

## Reguläre Ausdrücke

### Metazeichen
Zeichen mit besonderer Bedeutung, wie `.`, `*`, `+`, `?` etc.

### Zeichenklassen
In eckigen Klammern eingeschlossene Zeichensätze, wie `[abc]`.

### Quantifizierer
Symbole zur Angabe der Übereinstimmungsanzahl, wie `{n}`, `{n,m}` etc.

### Gruppierung
Mit runden Klammern erstellte Unterausdrücke, wie `(abc)+`.

### Anker
Symbole zur Angabe der Übereinstimmungsposition, wie `^` (Anfang), `$` (Ende).

## Konfigurationsbegriffe

### Vererbung
Mechanismus, durch den Unterkonfigurationen automatisch Einstellungen von übergeordneten Konfigurationen erhalten.

### Priorität
Ausführungsreihenfolge bei Konflikten zwischen mehreren Regeln.

### Vorlage
Vordefinierte Konfigurationskombinationen, die wiederverwendet werden können.

### Whitelist
Liste von Anwendungen oder Komponenten, die nicht von Regeln eingeschränkt werden.

### Blacklist
Liste von Anwendungen oder Komponenten, die strikt eingeschränkt oder abgefangen werden.

## Systemdienste

### PowerManagerService
Systemdienst zur Verwaltung des Gerätestromzustands.

### AlarmManagerService
Dienst zur Verwaltung von System-Alarmaufgaben.

### ActivityManagerService
Dienst zur Verwaltung des Anwendungslebenszyklus.

### PackageManagerService
Dienst zur Verwaltung von Anwendungsinstallation und -berechtigungen.

### WindowManagerService
Dienst zur Verwaltung der Fensteranzeige.

## Berechtigungsbezogen

### QUERY_ALL_PACKAGES
Berechtigung zur Abfrage aller installierten Anwendungen (Android 11+).

### WAKE_LOCK
Berechtigung zum Erwerb von WakeLock.

### RECEIVE_BOOT_COMPLETED
Berechtigung zum Empfang von Boot-Broadcasts.

### WRITE_EXTERNAL_STORAGE
Berechtigung zum Schreiben in externen Speicher.

## Debug-Begriffe

### ADB (Android Debug Bridge)
Befehlszeilentool zur Verbindung von Entwicklungsmaschine und Android-Gerät.

### Logcat
Android-Systemprotokoll-Anzeigentool.

### ANR (Application Not Responding)
Fehler, wenn Hauptthread der Anwendung länger als 5 Sekunden blockiert ist.

### Crash (Absturz)
Fehler bei abnormaler Beendigung der Anwendung.

### Memory Leak (Speicherleck)
Problem, bei dem Speicher während der Programmausführung nicht normal freigegeben werden kann.

## Leistungsbegriffe

### CPU-Auslastung
Prozentuale Nutzung des Prozessors.

### Speicherverbrauch
Von der Anwendung verwendete RAM-Größe.

### Stromverbrauch
Akkuverbrauch der Anwendung.

### Netzwerkverkehr
Datenübertragungsmenge der Anwendung.

### Speicher-I/O
Speicher-Lese-/Schreibaktivitäten der Anwendung.

!!! info "Begriffsaktualização"
    Mit der Projektentwicklung werden neue Begriffe kontinuierlich zu dieser Liste hinzugefügt. Bei Fragen konsultieren Sie bitte die relevante Dokumentation oder kontaktieren Sie die Community.

!!! tip "Lernempfehlung"
    Neuen Benutzern wird empfohlen, sich zunächst mit den Kernkonzepten (WakeLock, Alarm, Service) vertraut zu machen und dann schrittweise technische Details zu verstehen.