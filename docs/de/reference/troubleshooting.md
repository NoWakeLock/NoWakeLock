# Fehlerbehebung

Systematischer Problemdiagnose- und Lösungsleitfaden.

## Diagnoseablauf

### Erster Schritt: Grundprüfung

#### Modulstatus-Validierung
1. NoWakeLock öffnen → "Modulprüfung"
2. Bestätigen Sie, dass alle Elemente ✅ anzeigen
3. Bei ❌ Elementen entsprechend den Hinweisen behandeln

【Screenshot erforderlich: Beispiel für fehlgeschlagene Modulprüfung】

#### Xposed-Framework-Prüfung
```bash
# LSPosed-Status prüfen
adb shell am start -n org.lsposed.manager/.ui.activity.MainActivity

# Modulliste anzeigen
adb shell pm list packages | grep nowakelock
```

#### Grundberechtigungsprüfung
- Speicherberechtigung
- Berechtigung zur Abfrage aller Anwendungen (Android 11+)
- Barrierefreiheitsdienst-Berechtigung (falls erforderlich)

### Zweiter Schritt: Funktionstest

#### WakeLock-Test
1. Einfache WakeLock-Begrenzungsregel setzen
2. Entsprechende Anwendung öffnen, um WakeLock auszulösen
3. Prüfen Sie, ob die Statistikseite Abfangaufzeichnungen hat

#### Regelwirksamkeitstest
1. Testregel erstellen: Spezifischen WakeLock abfangen
2. Verhaltensänderungen der Zielanwendung beobachten
3. Ereignisprotokolle zur Bestätigung der Regelausführung anzeigen

## Häufige Problemkategorien

### Installationsprobleme

#### Modul kann nicht geladen werden
**Symptome**: Modulprüfung zeigt "Modul nicht geladen"

**Diagnoseschritte**:
1. Bestätigen Sie, dass das Xposed-Framework normal läuft
2. Prüfen Sie, ob das Modul im Manager aktiviert ist
3. Überprüfen Sie, ob die Anwendungssignatur korrekt ist

**Lösungen**:
```bash
# Modul neu installieren
adb uninstall com.js.nowakelock
adb install nowakelock.apk

# Framework-Cache löschen
# In LSPosed: "Einstellungen" → "Cache löschen"
```

#### Hook-Funktion unwirksam
**Symptome**: Modul geladen, aber Hook funktioniert nicht

**Mögliche Ursachen**:
- Systemversion nicht kompatibel
- Bereichskonfiguration fehlerhaft
- SELinux-Richtlinienbeschränkungen

**Lösungen**:
1. Bestätigen Sie, dass der Bereich `android` enthält
2. SELinux-Status prüfen:
   ```bash
   adb shell getenforce
   # Falls Enforcing, kann Hook-Funktion beeinträchtigt werden
   ```
3. Xposed-Protokolle anzeigen:
   ```bash
   adb logcat | grep -E "(Xposed|nowakelock)"
   ```

### Funktionsprobleme

#### Regeln werden nicht wirksam
**Symptome**: Regel gesetzt, aber keine Abfangwirkung

**Checkliste**:
- [ ] Ist die Regel aktiviert
- [ ] Sind die Übereinstimmungsbedingungen korrekt
- [ ] Wurde die Zielanwendung neu gestartet
- [ ] Gibt es konfliktuelle Regeln

**Debug-Methoden**:
1. Einfaches exaktes Matching zum Testen verwenden
2. Regelpriorität überprüfen
3. Matching-Protokolle anzeigen

#### Anwendungsfunktionen abnormal
**Symptome**: Nach Regeleinstellung funktioniert Anwendung nicht normal

**Sofortige Behandlung**:
1. Relevante Regeln deaktivieren
2. Problematische Anwendung neu starten
3. Regeln schrittweise wiederherstellen

**Grundlegende Lösung**:
1. Kritische Komponenten analysieren, von denen die Anwendung abhängt
2. Regelbereich oder Parameter anpassen
3. "Begrenzen" statt "Abfangen" verwenden

#### Statistikdaten abnormal
**Symptome**: Statistikdaten zeigen Anomalien oder werden nicht aktualisiert

**Zu prüfende Punkte**:
1. Datenbankstatus
   ```bash
   adb shell ls -la /data/data/com.js.nowakelock/databases/
   ```
2. Speicherplatz
   ```bash
   adb shell df /data
   ```
3. Anwendungsberechtigungen

**Reparaturmethoden**:
```bash
# Datenbank löschen (Achtung: Verlust von Verlaufsdaten)
adb shell pm clear com.js.nowakelock
```

### Leistungsprobleme

#### Systemruckeln
**Symptome**: System reagiert nach NoWakeLock-Installation langsamer

**Leistungsanalyse**:
```bash
# CPU-Auslastung
adb shell top | grep nowakelock

# Speicherverbrauch
adb shell dumpsys meminfo com.js.nowakelock
```

**Optimierungslösungen**:
1. Anzahl der Regeln reduzieren
2. Reguläre Ausdrücke vereinfachen
3. Statistikfrequenz anpassen

#### Erhöhter Stromverbrauch
**Symptome**: Modul selbst verbraucht Strom

**Diagnosemethoden**:
1. Hintergrundaktivitäten prüfen
   ```bash
   adb shell dumpsys battery
   ```
2. WakeLock-Nutzung analysieren
   ```bash
   adb shell dumpsys power | grep nowakelock
   ```

**Lösungen**:
- Prüfen Sie auf abnormale Schleifenaufgaben
- Datenbankabfragefrequenz optimieren
- Bestätigen Sie, dass keine Speicherlecks vorliegen

### Kompatibilitätsprobleme

#### Spezifische Anwendungskonflikte
**Symptome**: Bestimmte Anwendungen kollidieren mit NoWakeLock

**Identifikationsmethoden**:
1. Systemprotokollanalyse
2. Anwendungsabsturzberichte
3. ANR (Application Not Responding)-Protokolle

**Behandlungsstrategien**:
```yaml
Temporäre Lösung:
  - Anwendung zur Whitelist hinzufügen
  - Relevante Regeln deaktivieren

Langfristige Lösung:
  - Konfliktursachen analysieren
  - Hook-Strategie anpassen
  - Kompatibilitätscode aktualisieren
```

#### Systemversionskompatibilität
**Symptome**: Funktionsanomalien auf neuen Android-Versionen

**Anpassungsprüfung**:
1. API-Änderungsanalyse
2. Berechtigungsmodelländerungen
3. Sicherheitsrichtlinien-Updates

**Downgrade-Lösungen**:
- Inkompatible Funktionen deaktivieren
- Alternative Implementierungen verwenden
- Auf Versionsupdate warten

## Protokollanalyse

### Protokolle sammeln

#### Systemprotokolle
```bash
# Vollständige Protokolle
adb logcat -v time > full_log.txt

# NoWakeLock-bezogen
adb logcat | grep -i nowakelock > nowakelock_log.txt

# Xposed-bezogen
adb logcat | grep -i xposed > xposed_log.txt
```

#### Anwendungsprotokolle
```bash
# Spezifische Prozessprotokolle
adb logcat --pid=$(adb shell pidof com.js.nowakelock)

# Absturzprotokolle
adb logcat | grep -E "(FATAL|AndroidRuntime)"
```

### Protokollanalyse

#### Identifikation kritischer Fehler
```
E/Xposed: Hook failed
E/NoWakeLock: Database error
W/ActivityManager: Unable to start service
```

#### Identifikation von Leistungsproblemen
```
W/Choreographer: Skipped frames
I/Timeline: Timeline: Activity_idle
W/InputDispatcher: Application is not responding
```

### Protokollbereinigung
```bash
# Protokolle löschen
adb logcat -c

# Protokollebene setzen
adb shell setprop log.tag.NoWakeLock VERBOSE
```

## Datenwiederherstellung

### Konfigurationssicherung
```bash
# Konfiguration sichern
adb backup -f backup.ab com.js.nowakelock

# Datenbank extrahieren
adb shell cp /data/data/com.js.nowakelock/databases/app_database /sdcard/
adb pull /sdcard/app_database ./
```

### Konfigurationswiderherstellung
```bash
# Sicherung wiederherstellen
adb restore backup.ab

# Datenbank manuell wiederherstellen
adb push ./app_database /sdcard/
adb shell cp /sdcard/app_database /data/data/com.js.nowakelock/databases/
```

### Reset-Optionen

#### Soft-Reset (Konfiguration beibehalten)
1. Anwendungseinstellungen → Cache löschen
2. Anwendung neu starten

#### Hard-Reset (alle Daten löschen)
```bash
adb shell pm clear com.js.nowakelock
```

#### Vollständiger Reset (Neuinstallation)
```bash
adb uninstall com.js.nowakelock
# Neu installieren und konfigurieren
```

## Erweiterte Fehlerbehebung

### Hook-Debugging

#### Detaillierte Protokolle aktivieren
In den Anwendungseinstellungen "Debug-Modus" aktivieren, gibt detaillierte Hook-Informationen aus.

#### Hook-Testwerkzeuge
```kotlin
// Spezifischen Hook-Punkt testen
fun testWakeLockHook() {
    // WakeLock-Erwerb manuell auslösen
    // Beobachten, ob Hook aufgerufen wird
}
```

### Leistungsanalyse

#### CPU-Analyse
```bash
# Leistungsüberwachung
adb shell am start -n com.android.shell/.BugreportStorageProvider

# Thread-Analyse
adb shell ps -T | grep nowakelock
```

#### Speicheranalyse
```bash
# Speicherdetails
adb shell dumpsys meminfo com.js.nowakelock

# Speicherleck-Erkennung
adb shell am dumpheap com.js.nowakelock /sdcard/heap.hprof
```

### Datenbank-Debugging

#### Datenbankprüfung
```sql
-- Datenbank verbinden
sqlite3 app_database

-- Tabellenstruktur prüfen
.schema

-- Daten anzeigen
SELECT * FROM app_info LIMIT 10;
SELECT * FROM wakelock_info LIMIT 10;
```

#### Datenkonsistenzprüfung
```sql
-- Verwaiste Datensätze prüfen
SELECT * FROM events WHERE app_id NOT IN (SELECT id FROM apps);

-- Statistikdaten validieren
SELECT package_name, COUNT(*) FROM events GROUP BY package_name;
```

## Präventivmaßnahmen

### Regelmäßige Wartung

#### Wöchentliche Prüfung
- Modulstatus-Validierung
- Regelwirkungsbewertung
- Leistungsindikator-Überwachung

#### Monatliche Wartung
- Verlaufsdaten bereinigen
- Regelkonfiguration aktualisieren
- Wichtige Einstellungen sichern

### Überwachungseinstellungen

#### Leistungsüberwachung
Leistungsschwellenwerte setzen, automatische Warnungen bei Überschreitung:
- CPU-Auslastung > 5%
- Speicherverbrauch > 100MB
- Datenbankgröße > 500MB

#### Funktionsüberwachung
Schlüsselfunktionen regelmäßig testen:
- Regelübereinstimmungsgenauigkeit
- Vollständigkeit der Statistikdaten
- Normalität der Anwendungsfunktionen

## Professioneller Support

### Community-Support
- **Telegram**: [@nowakelock](https://t.me/nowakelock)
- **Discord**: [NoWakelock Community](https://discord.gg/kewmG5AShQ)
- **GitHub**: [Issues](https://github.com/NoWakeLock/NoWakeLock/issues)

### Problemberichtsvorlage
```markdown
## Umgebungsinformationen
- Gerät: [Marke Modell]
- Android-Version: [Versionsnummer]
- Xposed-Framework: [LSPosed/EdXposed Version]
- NoWakeLock-Version: [Versionsnummer]

## Problembeschreibung
[Detaillierte Beschreibung der Problemphänomene]

## Reproduktionsschritte
1. [Schritt eins]
2. [Schritt zwei]
3. [Problem tritt auf]

## Erwartetes Ergebnis
[Erwartetes normales Verhalten]

## Tatsächliches Ergebnis
[Tatsächlich auftretendes abnormales Verhalten]

## Relevante Protokolle
```log
[Relevante Protokolle einfügen]
```

## Weitere Informationen
[Andere relevante Informationen]
```

!!! warning "Datensicherheit"
    Bei der Fehlerbehebung unbedingt wichtige Konfigurationen zuerst sichern. Bestimmte Operationen können zu Datenverlust führen.

!!! tip "Debug-Empfehlung"
    Bei komplexen Problemen wird schrittweise Untersuchung empfohlen, beginnend mit der einfachsten Konfiguration und schrittweiser Erhöhung der Komplexität, um die Problemquelle leichter zu lokalisieren.