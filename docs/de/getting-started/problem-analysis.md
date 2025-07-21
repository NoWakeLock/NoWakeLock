# Problemanalyse-Leitfaden

Bevor Sie Regeln konfigurieren, müssen Sie zunächst bestätigen, dass das Gerät tatsächlich Akkuprobleme hat. Dieser Leitfaden erklärt, wie Sie Tools zur Analyse und Identifizierung von Problemen verwenden.

!!! warning "Wichtiges Prinzip"
    Erst nachdem durch Datenanalyse bestätigt wurde, dass ein bestimmter WakeLock/Alarm/Service tatsächlich Standby-Akkuverbrauchsprobleme verursacht, sollten Einschränkungen in Betracht gezogen werden. Konfigurieren Sie nicht basierend auf Annahmen oder Vorurteilen.

## Datenanalyse-Tools

### Empfohlene Analyse-Tools

1. **BetterBatteryStats (BBS)** - Erste Wahl, bietet die umfassendste Analyse
2. **NoWakeLock integrierte Statistiken** - Alternative wenn BBS nicht verfügbar ist
3. **Kombination verwenden** - Für genauere Diagnoseergebnisse

## Diagnose mit BetterBatteryStats (BBS)

### Diagnoseschritte

1. **BBS installieren** und notwendige Berechtigungen erteilen
2. **Überwachungszeitraum festlegen** - Empfohlen wird die Überwachung eines vollständigen Standby-Zyklus
3. **Schlüsseldaten analysieren**:
   - Kernel Wakelocks (System-Level-Wakelocks)
   - Partial Wakelocks (Anwendungs-Level-Wakelocks)
   - Weckfrequenz und -dauer jeder Anwendung

### Problemidentifikationsstandards

- Überwachung abnormaler Weckaktivitäten während des Gerätestillstands
- Identifizierung von WakeLocks mit übermäßig langer Dauer oder zu hoher Frequenz
- Bestätigung, dass diese Aktivitäten tatsächlich Akkuverbrauchsprobleme verursachen

## Verwendung der integrierten Datenanalyse von NoWakeLock

**Wenn BBS nicht verwendet werden kann**, können Sie die Statistikdaten von NoWakeLock selbst verwenden:

### Datenanzeige-Methoden

1. **WakeLock-Liste** - Statistiken der WakeLock-Aktivitäten jeder Anwendung anzeigen
2. **Nach Dauer sortieren** - WakeLocks mit abnormal langer Dauer identifizieren
3. **Nach Häufigkeit sortieren** - WakeLocks mit abnormal hoher Erwerbsfrequenz entdecken
4. **Aktiver Status** - Derzeit gehaltene WakeLocks überwachen

### Standards für Anomalie-Identifikation

- WakeLocks, die während des Gerätestillstands noch als aktiv angezeigt werden
- Einzelne WakeLocks mit kumulativer Dauer, die andere ähnliche Anwendungen weit übertrifft
- Häufige WakeLock-Erwerbe wenn Anwendung nicht verwendet wird
- Abnormale WakeLock-Aktivitäten in nächtlichen Stunden

## Fehlerbehebungsablauf

### Erster Schritt: Problemexistenz bestätigen

1. **Analyse-Tools für Überwachung verwenden** eines vollständigen Nutzungszyklus (mindestens 24 Stunden)
   - Priorisieren Sie BBS für umfassende Überwachung
   - Verwenden Sie NoWakeLock integrierte Statistiken wenn BBS nicht verfügbar ist
2. **Standby-Zeitraum vergleichen** WakeLock-Aktivitäten und Akkuverbrauch
3. **Anomalien bestätigen** - Suchen Sie nach WakeLocks, die auch bei Nichtnutzung des Geräts aktiv bleiben

### Zweiter Schritt: Problemquelle präzise lokalisieren

1. **Spezifische Anwendungen und Dienste identifizieren**, die abnormale WakeLocks verursachen
2. **Muster analysieren** - Prüfen Sie, ob es Probleme zu bestimmten Zeiten oder unter bestimmten Bedingungen gibt
3. **Auswirkungen bewerten** - Bestätigen Sie den tatsächlichen Einfluss dieses WakeLocks auf die Akkulebensdauer

### Dritter Schritt: Minimale Intervention

1. **Anwendungseinstellungen priorisieren** - Prüfen Sie, ob es relevante Optionen in der Anwendung gibt
2. **Begrenzungseffekte testen** - Verwenden Sie Begrenzungsmodus statt vollständiges Abfangen
3. **Ergebnisse überwachen** - Überwachen Sie weiterhin die Effekte nach der Konfiguration

!!! tip "Diagnosepunkte"
    - Das Problem muss tatsächlich existieren, nicht theoretisch
    - Die Intervention muss gezielt sein, nicht präventiv
    - Nach jeder Konfiguration muss überprüft werden, ob die Anwendungsfunktionen normal sind

## Effektvalidierung

### Überwachung nach Konfiguration

**Notwendige Schritte**:
1. **Datenüberwachung fortsetzen** der Effekte nach Konfiguration
   - Priorisieren Sie BBS für umfassende Bewertung
   - NoWakeLock integrierte Statistiken können auch als Referenz verwendet werden
2. **Anwendungsfunktionen überprüfen** - Bestätigen Sie, dass alle Anwendungsfunktionen noch normal sind
3. **Akkuverbesserung bewerten** - Vergleichen Sie den tatsächlichen Akkuverbrauch vor und nach der Konfiguration

**Rollback-Vorbereitung**:
- Wenn Anwendungsfunktionen beeinträchtigt sind, brechen Sie die Konfiguration sofort ab
- Wenn die Akkuverbesserung nicht offensichtlich ist, bewerten Sie erneut, ob Einschränkungen notwendig sind

## Nächste Schritte

Nach Abschluss der Problemanalyse:

1. [WakeLock-Verwaltung](../features/wakelocks.md) - Konfigurieren Sie spezifische WakeLock-Regeln
2. [Schnellstart](quick-start.md) - 5-Minuten-Schnellkonfigurationsleitfaden
3. [Häufig gestellte Fragen](../reference/faq.md) - Lösungen bei Problemen