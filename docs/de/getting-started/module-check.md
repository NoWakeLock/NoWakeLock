# Modulprüfung

Die Modulprüfungsseite hilft Ihnen dabei zu überprüfen, ob NoWakeLock korrekt installiert ist und läuft.

## Prüfelemente

### Grundprüfungen

| Prüfelement | Beschreibung | Fehlerursachen |
|-------------|--------------|----------------|
| **Xposed-Framework aktiviert** | Erkennt den Xposed-Framework-Laufstatus | Framework nicht installiert oder nicht aktiviert |
| **Modul geladen** | Bestätigt, dass NoWakeLock vom Framework erkannt wird | Modul nicht aktiviert oder Installation fehlgeschlagen |
| **Modul aktiviert** | Überprüft, dass das Modul im Zielprozess läuft | Bereichskonfiguration fehlerhaft |

### Funktionsprüfungen

| Prüfelement | Beschreibung | Fehlerursachen |
|-------------|--------------|----------------|
| **Hook funktioniert normal** | Überprüft Systemaufruf-Abfangfunktion | Systemversion nicht kompatibel |
| **Konfiguration erfolgreich gelesen** | Bestätigt, dass Einstellungen gelesen werden können | Berechtigungsprobleme oder Speicheranomalien |
| **Datenbank normal** | Prüft Datenbank-Lese-/Schreibfunktion | Unzureichender Speicherplatz oder Berechtigungsprobleme |

【Screenshot erforderlich: Modulprüfungsseite - Alle erfolgreich】

## Statusbeschreibungen

### ✅ Normaler Status
Alle Prüfelemente zeigen grüne Symbole, was bedeutet, dass das Modul normal funktioniert.

### ❌ Abnormaler Status
Rote Symbole zeigen Probleme an, die behandelt werden müssen.

### ⚠️ Warnstatus
Gelbe Symbole zeigen an, dass Funktionen teilweise eingeschränkt sind, aber grundsätzlich verwendbar.

## Fehlerbehebung

### Xposed-Framework-Probleme

**Symptome**: "Xposed-Framework aktiviert" zeigt ❌

**Lösungsschritte**:
1. Bestätigen Sie, dass LSPosed oder EdXposed installiert ist
2. Prüfen Sie, ob der Framework-Manager "Aktiviert" anzeigt
3. Starten Sie das Gerät neu
4. Überprüfen Sie die Framework-Versionskompatibilität

### Modulladungsprobleme

**Symptome**: "Modul geladen" zeigt ❌

**Lösungsschritte**:
1. Öffnen Sie den Xposed-Manager
2. Gehen Sie zur "Module"-Seite
3. Bestätigen Sie, dass NoWakeLock aktiviert ist
4. Starten Sie das Gerät neu
5. Prüfen Sie erneut

### Bereichskonfigurationsprobleme

**Symptome**: "Modul aktiviert" zeigt ❌

**Lösungsschritte**:
1. Öffnen Sie den LSPosed-Manager
2. Klicken Sie auf das NoWakeLock-Modul
3. Gehen Sie zu den "Bereich"-Einstellungen
4. Bestätigen Sie die Auswahl:
   - `android` (System-Framework)
   - `com.js.nowakelock` (Anwendung selbst)
5. Starten Sie das Gerät neu

【Screenshot erforderlich: LSPosed-Bereichskonfiguration】

### Hook-Funktionsprobleme

**Symptome**: "Hook funktioniert normal" zeigt ❌

**Mögliche Ursachen**:
- Android-Version nicht unterstützt
- Systemanpassung führt zu Schnittstellenänderungen
- SELinux-Richtlinienbeschränkungen

**Lösungen**:
1. Prüfen Sie die Gerätekompatibilität
2. Xposed-Protokolle anzeigen:
   ```bash
   adb logcat | grep -i nowakelock
   ```
3. Versuchen Sie eine Neuinstallation des Moduls

### Konfigurationsleseprobleme

**Symptome**: "Konfiguration erfolgreich gelesen" zeigt ❌

**Lösungsschritte**:
1. Prüfen Sie Speicherberechtigungen
2. Anwendungsdaten löschen:
   ```bash
   # Achtung: Dies löscht alle Konfigurationen
   adb shell pm clear com.js.nowakelock
   ```
3. Öffnen Sie die Anwendung erneut

### Datenbankprobleme

**Symptome**: "Datenbank normal" zeigt ❌

**Lösungsschritte**:
1. Prüfen Sie den Speicherplatz
2. Überprüfen Sie Anwendungsberechtigungen
3. Datenbank zurücksetzen:
   - Einstellungen → Daten löschen
   - Neu konfigurieren

## Erweiterte Prüfung

### Systemprotokolle anzeigen
```bash
# NoWakeLock-bezogene Protokolle anzeigen
adb logcat | grep -i nowakelock

# Xposed-Protokolle anzeigen
adb logcat | grep -i xposed
```

### Hook-Effekte überprüfen
1. Öffnen Sie eine beliebige Anwendung
2. Wechseln Sie zur WakeLock-Seite
3. Prüfen Sie, ob neue WakeLock-Einträge vorhanden sind

### Regelfunktionen testen
1. Setzen Sie eine Testregel
2. Lösen Sie entsprechendes Systemverhalten aus
3. Prüfen Sie, ob Statistikdaten aktualisiert werden

## Leistungsüberwachung

### Ressourcenverwendung
Die Modulprüfungsseite zeigt auch:
- CPU-Auslastung
- Speicherverbrauch
- Speicherplatzverwendung

### Leistungsindikatoren

| Indikator | Normaler Bereich | Beschreibung |
|-----------|------------------|--------------|
| CPU-Nutzung | < 5% | Hook-Verarbeitungsaufwand |
| Speicherverbrauch | < 50MB | Cache- und Datenverbrauch |
| Speicherverwendung | < 100MB | Datenbank- und Protokollgröße |

## Regelmäßige Prüfung

### Empfohlene Häufigkeit
- **Erstinstallation**: Täglich prüfen
- **Stabiler Betrieb**: Wöchentlich prüfen
- **Nach Systemupdate**: Sofort prüfen

### Automatische Prüfung
Die Anwendung führt beim Start automatische Grundprüfungen durch und zeigt bei Anomalien Benachrichtigungen an.

### Prüfaufzeichnungen
Die Anwendung behält Prüfaufzeichnungen der letzten 30 Tage, die in den Einstellungen eingesehen werden können.

## Support kontaktieren

Falls Prüfungen weiterhin fehlschlagen:

1. **Informationen sammeln**:
   - Gerätemodell und Android-Version
   - Xposed-Framework-Typ und -Version
   - Screenshot der Prüfungsseite

2. **Protokolle abrufen**:
   ```bash
   adb logcat -v time > nowakelock_log.txt
   ```

3. **Hilfe suchen**:
   - [GitHub Issues](https://github.com/NoWakeLock/NoWakeLock/issues)
   - [Telegram-Gruppe](https://t.me/nowakelock)
   - [Discord-Community](https://discord.gg/kewmG5AShQ)

!!! warning "Wichtige Erinnerung"
    Bei fehlgeschlagener Modulprüfung funktionieren die NoWakeLock-Funktionen möglicherweise nicht ordnungsgemäß. Bitte lösen Sie alle Probleme, bevor Sie Konfigurationen vornehmen.