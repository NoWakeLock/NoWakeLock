# Installationsanleitung

!!! danger "⚠️ Rettungsmodus - Wichtigster Punkt!"
    **Falls das Gerät nach der Installation abnormal startet, einfriert oder endlos neu startet**:
    
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
    
    **Präventivmaßnahmen**: Konfigurieren Sie beim ersten Gebrauch vorsichtig und testen Sie die Regelwirkung schrittweise.

## Voraussetzungen

### Systemanforderungen
- Android 7.0 (API 24) oder höher

!!! error "Einschränkungen der Gerätekompatibilität"
    **Samsung-Geräte mit OneUI werden derzeit nicht unterstützt**
    
    Da OneUI den Android-Quellcode verändert hat, funktionieren die Hook-Positionen trotz verschiedener Lösungsversuche nicht. Wir arbeiten an einer Lösung, aber derzeit kann es auf Samsung OneUI-Geräten nicht ordnungsgemäß funktionieren.
    
    Android-Geräte anderer Hersteller funktionieren normalerweise ordnungsgemäß.

### Xposed-Framework
Installieren Sie eines der folgenden Frameworks:

| Framework | Anwendbare Version | Empfehlungsgrad |
|-----------|-------------------|------------------|
| LSPosed | Android 8.1+ | ⭐⭐⭐⭐⭐ |
| EdXposed | Android 8.0-11 | ⭐⭐⭐ |

!!! info "Framework-Auswahl"
    LSPosed wird empfohlen für bessere Kompatibilität und Stabilität.

## NoWakeLock herunterladen

### Offizielle Kanäle

[![GitHub](https://img.shields.io/badge/GitHub-Releases-blue)](https://github.com/NoWakeLock/NoWakeLock/releases)
[![IzzyOnDroid](https://img.shields.io/badge/IzzyOnDroid-F-Droid-green)](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock)

**Download-Methoden**:
- **GitHub Releases** - APK-Datei direkt herunterladen
- **IzzyOnDroid** - Installation über F-Droid nach Hinzufügen der IzzyOnDroid-Quelle
- **F-Droid Offiziell** - Geplant

!!! tip "F-Droid-Quelleneinrichtung"
    Für Installation über IzzyOnDroid:
    1. Quelle in F-Droid-App hinzufügen: `https://apt.izzysoft.de/fdroid/repo`
    2. Nach NoWakeLock suchen und installieren

### Versionsauswahl

- **Stabile Version** - Von GitHub Releases oder IzzyOnDroid herunterladen
- **Beta-Version** - Aus dev-Branch erstellt

!!! warning "Nur offizielle Versionen unterstützt"
    Support wird nur für über offizielle Kanäle heruntergeladene Versionen bereitgestellt.

## Installationsschritte

### 1. APK herunterladen
Laden Sie die neueste Version der APK-Datei von den offiziellen Kanälen herunter.

### 2. Anwendung installieren
```bash
# Installation mit ADB (optional)
adb install nowakelock-v3.x.x.apk
```

Oder installieren Sie die APK-Datei direkt auf dem Gerät.

【Screenshot erforderlich: Installationsschnittstelle】

### 3. Modul aktivieren
1. Öffnen Sie den Xposed-Manager (LSPosed/EdXposed)
2. Gehen Sie zur "Module"-Seite
3. Aktivieren Sie NoWakeLock
4. Starten Sie das Gerät neu

【Screenshot erforderlich: LSPosed-Modulliste】

### 4. Anwendungsbereich konfigurieren
Richten Sie den Modulbereich in LSPosed ein:

**Erforderlicher Bereich**:
- `android` (System-Framework)

!!! tip "Bereichserklärung"
    NoWakeLock benötigt nur den `android` System-Framework-Bereich für ordnungsgemäße Funktion.

【Screenshot erforderlich: Bereichskonfiguration】

## Installation überprüfen

### Modulstatus prüfen
1. Öffnen Sie die NoWakeLock-Anwendung
2. Gehen Sie zur "Modulprüfung"-Seite
3. Bestätigen Sie, dass alle Elemente grüne ✅ anzeigen

【Screenshot erforderlich: Modulprüfungsseite】

### Überprüfungselemente

| Prüfelement | Beschreibung |
|-------------|-------------|
| Xposed-Framework aktiviert | Framework läuft normal |
| Modul geladen | NoWakeLock-Modul wird erkannt |
| Hook funktioniert normal | Systemaufruf-Abfangjagd erfolgreich |
| Konfiguration erfolgreich gelesen | Anwendung kann Konfiguration lesen |

### Funktionalität testen
1. Prüfen Sie, ob die "Apps"-Seite installierte Anwendungen anzeigt
2. Überprüfen Sie, ob die "WakeLocks"-Seite Daten hat
3. Versuchen Sie, eine einfache Regel zu setzen

## Häufige Probleme

### Modul nicht aktiviert
**Symptome**: Modulprüfung zeigt ❌  
**Lösungen**:
1. Bestätigen Sie, dass das Xposed-Framework normal läuft
2. Prüfen Sie, ob das Modul aktiviert ist
3. Starten Sie das Gerät neu und prüfen Sie erneut

### Hook funktioniert nicht
**Symptome**: Keine WakeLock/Alarm-Daten  
**Lösungen**:
1. Bestätigen Sie, dass der Bereich `android` enthält
2. Prüfen Sie die SELinux-Richtlinie
3. Überprüfen Sie die Xposed-Protokolle

### Anwendung stürzt ab
**Symptome**: Anwendung stürzt sofort beim Öffnen ab  
**Lösungen**:
1. Prüfen Sie die Android-Versionskompatibilität
2. Löschen Sie Anwendungsdaten
3. Installieren Sie das Modul neu

## Modul deinstallieren

### Vollständige Deinstallationsschritte
1. Deaktivieren Sie das Modul im Xposed-Manager
2. Starten Sie das Gerät neu
3. Deinstallieren Sie die NoWakeLock-Anwendung

## Nächste Schritte

Nach abgeschlossener Installation:

1. [Schnellstart](quick-start.md) - 5-Minuten-Einrichtungskonfiguration
2. [Modulprüfung](module-check.md) - Detaillierte Überprüfung des Modulstatus
3. [WakeLock-Verwaltung](../features/wakelocks.md) - Beginnen Sie mit der WakeLock-Verwaltung