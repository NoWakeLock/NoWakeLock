# NoWakeLock

NoWakeLock ist ein Android Xposed-Modul zur Verwaltung von WakeLock-, Alarm- und Service-Verhalten auf Ihrem Gerät und hilft dabei, die Akkulaufzeit zu optimieren.

!!! warning "Wichtiger Haftungsausschluss und Nutzungsempfehlung"
    **Nutzung auf eigene Gefahr - der Entwickler übernimmt keine Verantwortung für Geräteschäden.**
    
    **Wichtig**: Wenn Ihr Gerät keine Akkuprobleme hat, wird die Verwendung dieser Software nicht empfohlen. Das Hintergrundmanagement von Android 11+ wurde bereits erheblich optimiert. Die Verwendung wird nur empfohlen, wenn durch Tools wie BetterBatteryStats außergewöhnliche Akkuverbrauchsprobleme bestätigt wurden.
    
    NoWakeLock ist ein Lösungstool für spezifische Probleme, nicht eine allgemeine Optimierungssoftware.

!!! danger "⚠️ Wichtig: Rettungsmodus"
    **Falls das Gerät nach dem Start einfriert, endlos neu startet oder Systemanomalien auftreten**:
    
    **Situation 1: LSPosed-Framework-Problem (Problem tritt nach Installation auf, bevor Konfiguration erfolgt)**:
    1. Halten Sie die Ein-/Aus-Taste 10 Sekunden lang gedrückt für einen Neustart
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

## Kernfunktionen

- **WakeLock-Verwaltung** - Überwachung und Kontrolle von Anwendungs-Wakelocks
- **Alarm-Kontrolle** - Verwaltung von System-Alarmaufgaben
- **Service-Verwaltung** - Kontrolle des Starts von Hintergrunddiensten
- **Anwendungsverwaltung** - Anzeigen und Konfigurieren aller Komponenten nach Anwendung
- **Regelsystem** - Flexible Konfiguration mit Unterstützung für reguläre Ausdrücke

## Schnellstart

1. [Installationsanleitung](getting-started/installation.md) - Installation des NoWakeLock-Moduls
2. [Problemanalyse](getting-started/problem-analysis.md) - Analyse von Akkuverbrauchsproblemen (vor Verwendung lesen)
3. [Schnellstart](getting-started/quick-start.md) - 5-Minuten-Grundkonfiguration
4. [Modulprüfung](getting-started/module-check.md) - Überprüfung des Modulstatus

## Hauptfunktionen

### 📱 Anwendungsverwaltung
- [Anwendungsverwaltung](features/app-management.md) - Anzeigen und Konfigurieren nach Anwendung

### ⚡ Systemkontrolle
- [WakeLock-Verwaltung](features/wakelocks.md) - Sperrmechanismus zur Verhinderung des Geräteschlafmodus
- [Alarm-Verwaltung](features/alarms.md) - Kontrolle von System-Alarmaufgaben
- [Service-Verwaltung](features/services.md) - Kontrolle von Hintergrunddiensten

### 🔧 Konfigurationstools
- [Regeln und Regex](features/rules-regex.md) - Flexible Matching-Regeln
- [Anwendungsverwaltung](features/app-management.md) - Einheitliche Verwaltung nach Anwendung

## Benutzerhandbuch

Arbeiten Sie über die fünf Registerkarten der Anwendungshauptschnittstelle:
- **Apps** - Anwendungsliste und Gesamtverwaltung
- **Wakelocks** - WakeLock-Überwachung und -Kontrolle
- **Alarms** - Verwaltung geplanter Aufgaben
- **Services** - Kontrolle von Hintergrunddiensten
- **Settings** - Globale Einstellungen und Konfiguration

## Hilfe erhalten

- [Häufig gestellte Fragen](reference/faq.md) - Antworten auf die häufigsten Probleme
- [Fehlerbehebung](reference/troubleshooting.md) - Problemdiagnose und -lösung
- [Glossar](reference/glossary.md) - Erklärung technischer Begriffe

## Kompatibilität

- **Android-Version**: 7.0 (API 24) bis 16.0 (API 35)
- **Xposed-Framework**: LSPosed (empfohlen), EdXposed
- **Architektur-Unterstützung**: ARM64, ARM32
- **Aktuelle Version**: 3.0.3 (stabile Version)

!!! error "Einschränkungen der Gerätekompatibilität"
    **Samsung-Geräte mit OneUI werden derzeit nicht unterstützt**
    
    Da OneUI den Android-Quellcode verändert hat, funktionieren die Hook-Positionen trotz verschiedener Lösungsversuche nicht. Andere Android-Geräte von anderen Herstellern funktionieren normalerweise ordnungsgemäß.

## Community und Support

- **Telegram**: [@nowakelock](https://t.me/nowakelock)
- **Discord**: [NoWakelock](https://discord.gg/kewmG5AShQ)
- **GitHub**: [NoWakeLock/NoWakeLock](https://github.com/NoWakeLock/NoWakeLock)

## Entwickler

Interessiert an der technischen Implementierung oder an der Mitwirkung am Code?

- [Entwicklerdokumentation](developers/) - Technische Architektur und Implementierungsdetails
- [Entwicklungsumgebung](developers/) - Wie Sie an der Entwicklung teilnehmen können

---

!!! warning "Nutzungshinweis"
    NoWakeLock erfordert das Xposed-Framework. Sichern Sie wichtige Daten vor der Verwendung. Der Entwickler übernimmt keine Verantwortung für Geräteprobleme.

!!! info "Lizenz"
    Dieses Projekt ist unter der [GNU General Public License v3.0](https://github.com/NoWakeLock/NoWakeLock/blob/master/LICENSE) als Open Source verfügbar.