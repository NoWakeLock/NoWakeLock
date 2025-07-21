# Schnellstart

5-Minuten-Schnellkonfiguration von NoWakeLock, um die Akkuoptimierung Ihres Geräts zu starten.

## Schritt 1: Installation überprüfen

Öffnen Sie NoWakeLock und gehen Sie zur "Modulprüfung"-Seite:

- ✅ Alle Elemente zeigen Grün = Installation erfolgreich
- ❌ Beliebiges rotes Element = siehe [Installationsanleitung](installation.md)

【Screenshot erforderlich: Erfolgreiche Modulprüfungsschnittstelle】

## Schritt 2: Anwendungsliste anzeigen

1. Klicken Sie auf die "Apps"-Registerkarte unten
2. Durchsuchen Sie die Liste der installierten Anwendungen
3. Beachten Sie die Statistikzahlen auf der rechten Seite

【Screenshot erforderlich: Anwendungslistenschnittstelle】

**Listeninformationen**:
- Anwendungsname und Symbol
- WakeLock/Alarm/Service-Statistiken
- Letzte Aktivitätszeit

## Schritt 3: Akkuverbrauchsprobleme analysieren

⚠️ **Wichtig**: Bevor Sie Regeln konfigurieren, müssen Sie zunächst bestätigen, dass das Gerät tatsächlich Akkuprobleme hat.

### Problemanalyse-Tools verwenden
- Siehe [Problemanalyse-Leitfaden](problem-analysis.md) für den vollständigen Analyseprozess
- Verwenden Sie BetterBatteryStats oder die integrierten Statistiken von NoWakeLock zur Problembestätigung
- Konfigurieren Sie nur bestätigte problematische WakeLocks

### Problematische Anwendungen schnell identifizieren
1. Klicken Sie auf die "Wakelocks"-Registerkarte
2. Achten Sie auf Elemente mit hoher "Anzahl" und "Dauer"
3. Diese sind die Hauptquellen des Akkuverbrauchs

【Screenshot erforderlich: WakeLock-Liste】

## Schritt 4: Grundregeln einrichten

### Problematische WakeLocks konfigurieren
1. Klicken Sie auf abnormale WakeLock-Elemente
2. Wählen Sie die Behandlungsmethode:
   - **Begrenzen** - Timeout-Freigabe (für Anfänger empfohlen)
   - **Abfangen** - Vollständig blockieren (vorsichtig verwenden)

【Screenshot erforderlich: Regeleinrichtungsschnittstelle】

## Effekte beobachten

### Nach 24 Stunden prüfen
1. **Apps-Schnittstelle** - Abfangeffekte anzeigen
2. **Anwendungsfunktionen** - Bestätigen Sie, dass wichtige Funktionen normal sind
3. **Akkuverbrauch** - Systemakkustatistiken

### Schlüsselindikatoren
- Abfanganzahl steigt ✅
- Anwendungsbenachrichtigungen normal ✅  
- Akkulebensdauer verbessert sich ✅

【Screenshot erforderlich: Statistikdiagramme】

## Häufige Probleme

### Eine Anwendung kann keine Benachrichtigungen empfangen
**Lösung**:
1. Finden Sie die push-bezogenen WakeLocks dieser Anwendung
2. Ändern Sie in den "Erlauben"-Modus
3. Beobachten Sie einige Stunden

### Keine Wirkung nach Einstellung
**Zu prüfende Elemente**:
1. Funktioniert das Modul normal
2. Werden Regeln korrekt angewendet
3. Wurde die Anwendung neu gestartet

### Gerät wird langsam oder ruckelt
**Sofortige Behandlung**:
1. Deaktivieren Sie vorübergehend alle Regeln
2. Stellen Sie wichtige Anwendungen schrittweise wieder her
3. Vermeiden Sie das Abfangen kritischer Systemdienste

## Nächste Schritte

### Tiefere Konfiguration
- [WakeLock-Verwaltung](../features/wakelocks.md) - Detaillierte WakeLock-Kontrolle
- [Regelsystem](../features/rules-regex.md) - Reguläre Ausdrücke für Matching

### Erweiterte Funktionen
- [Alarm-Verwaltung](../features/alarms.md) - Kontrolle geplanter Aufgaben
- [Service-Verwaltung](../features/services.md) - Hintergrunddiensteverwaltung
- [Anwendungsverwaltung](../features/app-management.md) - Konfiguration nach Anwendung

### Hilfe erhalten
- [Häufig gestellte Fragen](../reference/faq.md) - Die häufigsten Probleme
- [Fehlerbehebung](../reference/troubleshooting.md) - Problemdiagnose-Leitfaden

!!! tip "Nutzungsempfehlung"
    Es wird empfohlen, zunächst mit konservativen Einstellungen zu beginnen und schrittweise anzupassen. Stellen Sie sicher, dass wichtige Anwendungsfunktionen normal funktionieren, bevor Sie aggressivere Optimierungen vornehmen.