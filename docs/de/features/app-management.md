# Anwendungsverwaltung

Die Anwendungsverwaltungsfunktion ermöglicht es Ihnen, alle WakeLocks, Alarme und Services nach Anwendungen anzuzeigen und zu konfigurieren, und bietet eine einheitliche Verwaltungsansicht auf Anwendungsebene.

## Funktionsübersicht

### Kernfunktionen
- Anzeige aller Aktivitäten kategorisiert nach Anwendungen
- Statistikinformationen auf Anwendungsebene
- Massenkonfiguration aller Komponenten einer Anwendung
- Analyse von Anwendungsverhaltensmustern

### Verwaltungsvorteile
- Einheitliche Anwendungsperspektive
- Einfache Identifikation problematischer Anwendungen
- Vereinfachter Massenkonfigurationsprozess
- Intuitive Vergleichsanalyse

## Oberflächenbeschreibung

### Anwendungsliste

【Screenshot erforderlich: Anwendungslistenseite】

**Anzeigeinformationen**:
- **Anwendungssymbol und -name**
- **Paketname** - Eindeutige Anwendungskennung
- **Statistikzusammenfassung** - Anzahl von WakeLock/Alarm/Service
- **Aktivitätsstatus** - Indikator für kürzliche Aktivitäten
- **Benutzer** - Benutzerkennung in Mehrbenutzerumgebung

### Statistikkarten

Jede Anwendung zeigt drei Arten von Statistiken:

| Typ | Indikator | Beschreibung |
|-----|-----------|--------------|
| WakeLock | Anzahl/Dauer | Erwerbsanzahl und kumulierte Haltezeit |
| Alarm | Anzahl/Frequenz | Auslöseanzahl und durchschnittliches Intervall |
| Service | Anzahl/Dauer | Startanzahl und Laufzeit |

### Filtern und Sortieren

**Filteroptionen**:
- Alle Anwendungen
- Systemanwendungen
- Benutzeranwendungen
- Anwendungen mit Aktivitäten
- Anwendungen mit konfigurierten Regeln

**Sortierweisen**:
- Nach Anwendungsname
- Nach Installationszeit
- Nach Aktivitätsfrequenz
- Nach Ressourcenverbrauch
- Nach Konfigurationsstatus

## Anwendungsdetailseite

### Zur Detailseite

1. Klicken Sie auf eine beliebige Anwendung in der Anwendungsliste
2. Zur Anwendungsdetailseite wechseln

【Screenshot erforderlich: Anwendungsdetailseite】

### Detailseiteninhalt

#### Anwendungsinformationen
- Anwendungsname, Version, Paketname
- Installationszeit, Aktualisierungszeit
- Berechtigungsliste
- Aktueller Laufstatus

#### Aktivitätsstatistiken
- **Zeitliniendiagramm** - 12-Stunden-Aktivitätstrend
- **Kategorisierte Statistiken** - Detaillierte WakeLock/Alarm/Service-Daten
- **Ressourcenverbrauch** - CPU-, Speicher-, Stromverbrauch

#### Komponentenliste
Anzeige nach Registerkarten kategorisiert:
- **WakeLocks** - Alle WakeLocks dieser Anwendung
- **Alarms** - Alle Alarme dieser Anwendung
- **Services** - Alle Services dieser Anwendung

## Konfigurationsfunktionen

### Konfiguration auf Anwendungsebene

#### Globale Einstellungen
Einheitliche Regeln für Anwendungen festlegen:
```
Erlauben-Modus:
- Alle Komponenten laufen normal
- Geeignet für wichtige Anwendungen

Begrenzen-Modus:
- Einheitliche Zeitbegrenzungen
- Geeignet für allgemeine Anwendungen

Abfangen-Modus:
- Alle Hintergrundaktivitäten blockieren
- Geeignet für problematische Anwendungen
```

#### Vererbungsmechanismus
- Komponentenebene kann Anwendungseinstellungen erben
- Unterstützt Ausnahmekonfigurationen für einzelne Komponenten
- Priorität: Komponenteneinstellungen > Anwendungseinstellungen > Globale Standards

## Mehrbenutzerunterstützung

### Benutzerwechsel
Auf Mehrbenutzegeräten:
1. Benutzerauswahl oben
2. Wechsel zur Anzeige verschiedener Benutzeranwendungen
3. Unabhängige Konfiguration für jeden Benutzer

【Screenshot erforderlich: Benutzerwechselschnittstelle】

### Benutzerisolierung
- Konfigurationen verschiedener Benutzer beeinflussen sich nicht gegenseitig
- Systemanwendungen werden zwischen allen Benutzern geteilt
- Benutzeranwendungen werden nur unter entsprechenden Benutzern angezeigt

## Import und Export

### Konfigurationssicherung

#### Konfiguration exportieren
```json
{
  "version": "3.0",
  "timestamp": "2024-01-01T00:00:00Z",
  "user_id": 0,
  "apps": [
    {
      "package_name": "com.example.app",
      "app_config": {
        "mode": "limit",
        "wakelock_timeout": 60000
      },
      "components": [
        {
          "type": "wakelock",
          "name": "ExampleWakeLock",
          "mode": "allow"
        }
      ]
    }
  ]
}
```

#### Konfiguration importieren
- Unterstützt vollständigen Konfigurationsimport
- Selektiver Import spezifischer Anwendungen
- Auswahl der Konfliktbehandlungsstrategie

## Verwandte Funktionen

- [WakeLock-Verwaltung](wakelocks.md) - Detaillierte WakeLock-Kontrolle
- [Alarm-Verwaltung](alarms.md) - Detaillierte Alarm-Kontrolle
- [Service-Verwaltung](services.md) - Detaillierte Service-Kontrolle
- [Regelsystem](rules-regex.md) - Massenkonfiguration mit regulären Ausdrücken

!!! tip "Nutzungsempfehlung"
    Die Anwendungsverwaltung ist die Eingangsfunktion von NoWakeLock. Es wird empfohlen, dass neue Benutzer hier beginnen, um die Verhaltensmuster verschiedener Anwendungen zu verstehen, bevor sie gezielte Optimierungen vornehmen.