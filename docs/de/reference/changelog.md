# Änderungsprotokoll

Dieses Dokument dokumentiert die Versionsupdate-Geschichte und wichtigen Änderungen von NoWakeLock.

## [v3.0.3 Build 80] - 20.05.2024

### 🎉 Großes Update
- **Erste offizielle Version der 3.x-Serie**, vollständig umstrukturiert
- **Material Design 3** völlig neues Oberflächendesign
- **Android 7-15** Kompatibilitätsunterstützung
- **Mehrbenutzerunterstützung** vollständige Implementierung
- **Ereignisverfolgungssystem** umfassende Aktualisierung

### ✨ Neue Funktionen
- **Jetpack Compose** UI-Framework-Integration
- **Anwendungsstatistiken** Funktionsverbesserung
- **Globale Suche** Funktion
- **Moduldetektion** vollständige Statusprüfung
- **Benutzerverwaltung** Mehrbenutzerumgebungsunterstützung

### ⚠️ Kompatibilitätshinweise
- **Samsung-Geräte bekannte Probleme** - Einige Funktionen können eingeschränkt sein
- **Upgrade-Empfehlung**:
  1. v2.0.x-Version deinstallieren
  2. Gerät neu starten
  3. v3.x-Version installieren
  4. Gerät neu starten

### 🔄 Konfigurationsmigration
- **v2.0-Konfiguration nicht kompatibel** - Neukonfiguration erforderlich
- Empfohlen wird Sicherung wichtiger Konfigurationen vor Upgrade
- Konfigurationsimport-/-exportfunktion bereitgestellt

---

## [v3.0.2 Build 77] - 16.05.2024

### ✨ Neue Funktionen
- **Modulprüfungsfunktion** - Vollständige Modulstatus-Validierung
- **Detektionsoberflächen-Optimierung** - Klarere Statusanzeige
- **Datenbankmigrationsstrategie** - Verbesserte Versionsupgrade-Behandlung

### 🐛 Korrekturen
- Moduldetektion auf bestimmten Geräten korrigiert
- Datenbank-Upgrade-Prozess verbessert
- Benutzeroberflächen-Reaktionsgeschwindigkeit optimiert

---

## [v3.0.1 Build 75-76] - 05.05.2024

### 🔧 Verbesserungen
- **Einheitliche Hook-Strategie** - Service-, Alarm-, WakeLock-Behandlungsoptimierung
- **Leistungsoptimierung** - Reduzierung des Systemressourcenverbrauchs
- **Code-Refaktorierung** - Verbesserung der Codequalität und Wartbarkeit

### 🐛 Korrekturen
- Kompatibilitätsprobleme auf bestimmten Android-Versionen korrigiert
- Hook-Stabilität verbessert
- Speichernutzung optimiert

---

## [v2.0.5 Build 62-63] - März 2024

### ✨ Neue Funktionen
- **Thematisierte Starticons** - Unterstützung für dynamische Themen
- **Fehlerbehandlungsverbesserung** - ContentProvider-Datenanomaliebehandlung
- **Service-Hook-Update** - Unterstützung für Android API 29-40

### 🔧 Verbesserungen
- Datenabfrageleistung optimiert
- Benutzeroberflächen-Reaktion verbessert
- Fehlerprotokollierung verbessert

---

## v2.x-Serie (Historische Versionen)

### Hauptmerkmale
- Grundlegende WakeLock/Alarm/Service-Verwaltung
- Traditionelles UI-Design
- Android 7+ Grundunterstützung
- Einzelbenutzerumgebung

### Bekannte Einschränkungen
- Relativ einfaches UI-Design
- Relativ grundlegende Funktionen
- Keine Mehrbenutzerumgebungsunterstützung
- Mit v3.x-Konfiguration nicht kompatibel

---

## Versionsvergleich

### v3.x vs v2.x Hauptunterschiede

| Merkmal | v2.x | v3.x |
|---------|------|------|
| UI-Framework | Traditionelle View | Jetpack Compose |
| Design-Sprache | Material Design 2 | Material Design 3 |
| Android-Unterstützung | 7-11 | 7-15 |
| Mehrbenutzer | ❌ | ✅ |
| Moduldetektion | Grundlegend | Vollständig |
| Leistung | Allgemein | Optimiert |
| Konfigurationskompatibilität | - | Nicht kompatibel mit v2.x |

### Upgrade-Empfehlungen

#### Von v2.x upgraden
1. **Konfiguration sichern** - Aktuelle Regeleinstellungen aufzeichnen
2. **Vollständig deinstallieren** - v2.x deinstallieren und neu starten
3. **Neuinstallation** - v3.x installieren und neu konfigurieren
4. **Schrittweise Konfiguration** - Einstellungen schrittweise basierend auf Sicherung wiederherstellen

#### Neue Benutzer
- Direkt neueste v3.x-Version installieren
- Nach [Schnellstart](../getting-started/quick-start.md)-Leitfaden konfigurieren

---

## Bekannte Probleme

### v3.0.3 Bekannte Probleme
- **Samsung-Geräte** - Einige Funktionen können instabil sein
- **Android 15** - Bestimmte neue Funktionen möglicherweise nicht vollständig unterstützt
- **Speichernutzung** - Kann Leistungsauswirkungen auf Geräten mit wenig Speicher haben

### Temporäre Lösungen
- Samsung-Geräte empfohlene konservative Konfiguration
- Android 15-Benutzer sollten nachfolgende Updates beachten
- Geräte mit wenig Speicher sollten Anzahl der Regeln reduzieren

---

## Entwicklungsroadmap

### Kurzfristige Pläne (v3.1.x)
- Samsung-Geräte-Kompatibilitätsverbesserung
- Android 15 vollständige Unterstützung
- Leistungsoptimierung
- Mehr vordefinierte Konfigurationsvorlagen

### Mittelfristige Pläne (v3.2.x)
- Cloud-Konfigurationssynchronisation (optional)
- Reichhaltigere Statistikanalyse
- Plugin-System
- API-Öffnung

### Langfristige Pläne (v4.x)
- Völlig neues Architekturdesign
- Breitere Geräteunterstützung
- KI-unterstützte Konfiguration
- Enterprise-Funktionen

---

## Updates erhalten

### Offizielle Kanäle
- [GitHub Releases](https://github.com/NoWakeLock/NoWakeLock/releases) - Neueste Version
- [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock) - F-Droid-Repository

### Versionstypen
- **Stable** - Stabile Version, empfohlen für allgemeine Benutzer
- **Beta** - Testversion, neue Funktionen im Voraus erleben
- **Dev** - Entwicklungsversion, nur für Entwicklungstests

### Update-Benachrichtigungen
- In-App-Benachrichtigungen über neue Versionsveröffentlichungen
- GitHub Releases verfolgen für neueste Informationen
- Community-Gruppen beitreten für Erstinformationen

!!! info "Versionsstrategie"
    NoWakeLock folgt semantischer Versionsverwaltung. Große Updates werden im Voraus angekündigt, um Benutzern ausreichend Upgrade-Vorbereitungszeit zu gewährleisten.

!!! warning "Upgrade-Erinnerung"
    Vor großen Versionsupgrades unbedingt Konfigurationen sichern. Zwischen bestimmten Versionen können inkompatible Situationen bestehen.