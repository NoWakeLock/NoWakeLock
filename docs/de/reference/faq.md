# Häufig gestellte Fragen

Sammlung der häufigsten Benutzerfragen und Lösungen.

## Installationsbezogen

### F: Modul funktioniert nach Installation nicht?
**A**: Überprüfen Sie folgende Punkte:
1. Bestätigen Sie, dass das Xposed-Framework normal läuft
2. Aktivieren Sie das NoWakeLock-Modul im Framework-Manager
3. Starten Sie das Gerät neu und überprüfen Sie den Modulstatus
4. Bestätigen Sie, dass der Bereich das `android` System-Framework enthält

### F: Welche Xposed-Frameworks werden unterstützt?
**A**: Unterstützte Frameworks:
- **LSPosed** - Empfohlen, beste Kompatibilität
- **EdXposed** - Teilweise Geräteunterstützung
- **TaiChi** - Nicht empfohlen, mögliche Kompatibilitätsprobleme

### F: Modulprüfung zeigt rot ❌?
**A**: Behandlung nach spezifischem Element:
- **Framework nicht aktiviert** → Überprüfen Sie die Xposed-Installation
- **Modul nicht geladen** → Bestätigen Sie, dass das Modul aktiviert ist und neu starten
- **Hook fehlgeschlagen** → Möglicherweise ist die Systemversion nicht kompatibel

## Funktionsnutzung

### F: Regel zeigt nach Einstellung keine Wirkung?
**A**: Mögliche Ursachen:
1. **Regel nicht aktiviert** - Prüfen Sie, ob die Regel aktiviert ist
2. **Anwendung neu starten** - Einige Regeln erfordern einen Anwendungsneustart
3. **System-Cache** - Warten Sie einige Minuten, bis das System neue Regeln anwendet
4. **Berechtigungsprobleme** - Bestätigen Sie, dass NoWakeLock notwendige Berechtigungen hat

### F: Eine Anwendung kann keine Push-Nachrichten empfangen?
**A**: Lösungsschritte:
1. WakeLock-Liste dieser Anwendung anzeigen
2. Push-bezogene WakeLocks finden (enthalten normalerweise Push, GCM, FCM)
3. Diese WakeLocks auf "Erlauben"-Modus ändern
4. Anwendung neu starten und Push-Funktionen testen

### F: Anwendung wird sehr träge oder Funktionen sind abnormal?
**A**: Sofortige Behandlung:
1. Alle Regeln dieser Anwendung vorübergehend deaktivieren
2. Regeln einzeln aktivieren, um die Problemregel zu finden
3. Parameter der Problemregel anpassen oder in Begrenzungsmodus wechseln
4. Abfangen kritischer Systemdienste vermeiden

### F: Wie bestimme ich, ob die Optimierung wirksam ist?
**A**: Beobachtbare Indikatoren:
- **Kurzfristig (24 Stunden)**: Abfangstatistiken anzeigen, Anwendungsfunktionen normal
- **Mittelfristig (eine Woche)**: Akkulaufzeit deutlich verbessert
- **Langfristig**: Gesamtflüssigkeit des Systems verbessert

## Kompatibilitätsprobleme

### F: Android-Versionskompatibilität?
**A**: Unterstützungsstatus:
- **Android 7.0-16** - AOSP Vollständige Unterstützung

### F: Spezifische Markengeräteprobleme?
**A**: Bekannte Probleme:
- **MIUI** - MIUI-Optimierung deaktivieren, Hintergrundausführung erlauben
- **ColorOS** - Möglicherweise Autostart in Berechtigungsverwaltung erlauben
- **EMUI** - Empfohlen, NoWakeLock zu geschützten Anwendungen hinzuzufügen
- **OneUI** - ⚠️ **Derzeit nicht unterstützt** - Da OneUI den Android-Quellcode verändert hat, funktionieren Hook-Positionen trotz verschiedener Lösungsversuche nicht

### F: Nach Systemupdate unwirksam?
**A**: Behandlungsschritte:
1. Prüfen Sie, ob das Xposed-Framework noch funktioniert
2. Bestätigen Sie, dass das NoWakeLock-Modul noch im aktivierten Zustand ist
3. Bei Bedarf Modul neu installieren
4. Bereichseinstellungen neu konfigurieren

## Leistungsprobleme

### F: NoWakeLock selbst verbraucht Strom?
**A**: Unter normalen Umständen verbraucht NoWakeLock sehr wenig Strom:
- **CPU-Nutzung** < 1%
- **Speicherverbrauch** < 50MB
- **Hintergrundaktivitäten** minimiert

Falls Sie abnormalen Stromverbrauch feststellen, überprüfen Sie die Konfiguration oder kontaktieren Sie den Support.

### F: Gerät wird langsamer oder ruckelt?
**A**: Mögliche Ursachen:
1. **Zu viele Regeln** - Unnötige Regeln reduzieren
2. **Komplexe Regex** - Reguläre Ausdrücke vereinfachen
3. **Häufiges Abfangen** - Abfangstrategie anpassen
4. **Unzureichender Speicher** - Gerät neu starten, um Speicher freizugeben

### F: Anwendungsstart wird langsamer?
**A**: Prüfen Sie, ob:
1. Für den Anwendungsstart notwendige Services abgefangen wurden
2. Initialisierungsbezogene WakeLocks übermäßig begrenzt wurden
3. Regeln den Anwendungsstartprozess beeinflussen

## Datenprobleme

### F: Statistikdaten ungenau?
**A**: Mögliche Situationen:
1. **Datenverzögerung** - Statistiken werden alle 5 Minuten aktualisiert
2. **Geräteneustart** - Einige Echtzeitdaten werden nach Neustart zurückgesetzt
3. **Zeitzonenproblem** - Gerätezeitzoneneinstellungen überprüfen
4. **Speicherprobleme** - Datenbank-Cache bereinigen

### F: Verlaufsdaten verloren?
**A**: Datenaufbewahrungsrichtlinie:
- **Echtzeitdaten** - Nach Geräteneustart gelöscht
- **Statistikdaten** - 30 Tage aufbewahrt
- **Konfigurationsdaten** - Dauerhaft gespeichert (außer manuell gelöscht)

### F: Exportierte Konfiguration kann nicht importiert werden?
**A**: Zu überprüfende Punkte:
1. Ist das Dateiformat korrekt (JSON)
2. Ist die Version kompatibel
3. Ist die Datei beschädigt
4. Sind die Berechtigungen ausreichend

## Mehrbenutzerprobleme

### F: Wie in Mehrbenutzumgebung verwenden?
**A**: Zu beachtende Punkte:
1. Jeder Benutzer muss separat konfiguriert werden
2. Systemanwendungen teilen Einstellungen zwischen allen Benutzern
3. Nach Benutzerwechsel beeinflussen sich Konfigurationen nicht gegenseitig
4. Einige Funktionen erfordern Hauptbenutzerberechtigungen

### F: Kann nicht in Arbeitsprofil verwendet werden?
**A**: Arbeitsprofilbeschränkungen:
1. Xposed-Framework muss separat im Arbeitsprofil installiert werden
2. Einige Unternehmensrichtlinien können Xposed-Module verbieten
3. Administrator bezüglich relevanter Richtlinien kontaktieren

## Sicherheitsprobleme

### F: Ist NoWakeLock sicher?
**A**: Sicherheitsmaßnahmen:
- **Open-Source-Code** - Aller Code ist öffentlich einsehbar
- **Keine Netzwerkberechtigungen** - Lädt keine Daten hoch
- **Lokale Speicherung** - Alle Daten werden nur lokal auf dem Gerät gespeichert
- **Minimale Berechtigungen** - Fordert nur notwendige Systemberechtigungen an

### F: Wird die Systemstabilität beeinträchtigt?
**A**: Sicherheitsüberlegungen:
1. Verwendet standardmäßige Hook-Mechanismen des Systems
2. Minimaler Einfluss auf Systemaufrufe
3. Automatische Degradationsbehandlung bei Ausnahmesituationen
4. Ändert keine Systemkerneldateien

### F: Werden Privatsphärendaten preisgegeben?
**A**: Datenschutz:
- **Keine Datensammlung** - Sammelt keine persönlichen Informationen
- **Keine Netzwerkkommunikation** - Kommuniziert nicht mit externen Servern
- **Lokale Verarbeitung** - Alle Analysen erfolgen lokal auf dem Gerät
- **Benutzerkontrolle** - Benutzer hat vollständige Kontrolle über alle Daten

## Fehlerbehebung

### F: Kann überhaupt nicht verwendet werden, was tun?
**A**: Reset-Schritte:
1. Modul in Xposed deaktivieren
2. Gerät neu starten
3. NoWakeLock-Anwendungsdaten löschen
4. Modul erneut aktivieren und konfigurieren

### F: Bestimmte Anwendungen stürzen häufig ab?
**A**: Notfallbehandlung:
1. Alle Regeln dieser Anwendung sofort deaktivieren
2. Fehlerinformationen in Systemprotokollen überprüfen
3. Regeln schrittweise wiederherstellen und beobachten
4. Bei Bedarf Anwendung zur Whitelist hinzufügen

### F: Wie Debug-Informationen sammeln?
**A**: Informationssammlung:
```bash
# Geräteinformationen
adb shell getprop ro.build.version.release
adb shell getprop ro.product.model

# Anwendungsprotokolle
adb logcat | grep -i nowakelock

# Xposed-Protokolle
adb logcat | grep -i xposed
```

## Grundkonzepte

### F: Was sind WakeLock/Alarm/Service? Wie einstellen für beste Ergebnisse?

**A**: Kernkonzepterklärung:

**WakeLock (Wach-Sperre)**:
- Mechanismus zur Verhinderung des Geräteschlafs
- Typen: PARTIAL (CPU läuft), SCREEN (Bildschirm an) etc.
- Offizielle Dokumentation: [Android WakeLock-Leitfaden](https://developer.android.com/training/scheduling/wakelock)

**Alarm (Geplante Aufgaben)**:
- System-Timer, löst Aufgaben zu bestimmten Zeiten aus
- Typen: RTC, ELAPSED_REALTIME etc.
- Offizielle Dokumentation: [Android Alarms-Leitfaden](https://developer.android.com/training/scheduling/alarms)

**Service (Dienste)**:
- Im Hintergrund laufende Anwendungskomponenten
- Typen: Vordergrunddienste, Hintergrunddienste, gebundene Dienste
- Offizielle Dokumentation: [Android Services-Leitfaden](https://developer.android.com/guide/components/services)

**Empfohlene Lernressourcen**:
- XDA-Leitfaden: ["Anfänger WakeLock Komplettleitfaden"](https://forum.xda-developers.com)
- [Amplify](https://forum.xda-developers.com/t/mod-xposed-amplify-battery-extender-control-alarms-services-and-wakelocks.2853874/) - Bietet WakeLock/Alarm/Service-Informationsreferenzliste
- [WakeBlock](https://github.com/MrLast98/WakeBlock) - Bietet auch WakeLock-Informationen

**Hinweis**: Leider gibt es keine perfekte universelle Referenz, da die Unterschiede zwischen Geräten enorm sind. Anpassungen müssen je nach spezifischem Gerät und Anwendung vorgenommen werden.

## Schwerwiegende Problembehandlung

### F: Fehlerhafte Bedienung führt dazu, dass Gerät nicht startet, was tun?

**A**: Notfall-Wiederherstellungsschritte:

**Situation 1: Startprobleme durch NoWakeLock-Modul**:

**Methode 1: Hardware-Taste Sicherheitsmodus (empfohlen)**:
```bash
1. Halten Sie die Ein-/Aus-Taste 10 Sekunden lang für Neustart gedrückt
2. Sobald der Bildschirm schwarz wird, drücken Sie sofort wiederholt eine beliebige Hardware-Taste (Lautstärke- oder Ein-/Aus-Taste)
3. Nach 2 kurzen Vibrationen drücken Sie schnell 4 Mal dieselbe Taste
4. Nach der 4. Tastenbetätigung sollten Sie eine lange Vibration spüren - Xposed ist deaktiviert
5. Nach normalem Start deaktivieren Sie das NoWakeLock-Modul in LSPosed
```

**Methode 2: Recovery-Dateisystem-Methode**:
```bash
1. TWRP Recovery aufrufen
2. Advanced → File Manager anklicken
3. Zu /data/adb/lspd/ navigieren
4. config-Ordner löschen
5. Zum System neu starten
```

**Methode 3: Gerätespezifische Methode (wie Pixel)**:
```bash
Beim Starten nach dem Erscheinen des Google-Logos sofort wie verrückt die Leiser-Taste drücken
Bis das Gerät vibriert und bestätigt, dass der Sicherheitsmodus aufgerufen wurde
```

**Situation 2: Fälschliches Abfangen wichtiger Systemkomponenten**:
```bash
1. Recovery → Dateimanager aufrufen
2. Zu /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock navigieren
   # xxx-xxx-xxx ist eine lange zufällige Zeichenfolge, die auf jedem Gerät unterschiedlich sein kann
3. Gesamten Ordner löschen
4. Gerät neu starten
5. Nach Systemstart fehlerhaften Vorgang wiederherstellen, bei unklarem spezifischem Problem NoWakeLock-Daten direkt löschen
```

**Bei ungewisser Ursache**:
- Nach Systemstart NoWakeLock-Anwendungsdaten direkt löschen
- Bei Neukonfiguration Abfangen kritischer Systemkomponenten vermeiden

!!! danger "Wichtige Warnung"
    Vor Änderung kritischer Systemkomponenten unbedingt Konfiguration sichern. Empfohlen wird, mit Drittanbieter-Anwendungen zu beginnen und schrittweise Systemdienste anzupassen.

## Datenschutz und Sicherheit

### F: Sammelt NoWakeLock Privatsphärendaten?

**A**: Datenschutzverpflichtung:
- **Vollständig lokal** - Alle Daten werden nur lokal auf dem Gerät gespeichert
- **Null Daten-Upload** - Lädt keine Daten auf Server hoch
- **Keine Privatsphärensammlung** - Sammelt oder speichert keine persönlichen Informationen
- **Open-Source-Transparenz** - Quellcode ist vollständig öffentlich einsehbar

**Mögliche zukünftige Funktionen**:
- Optionale Cloud-Konfigurationssynchronisation könnte hinzugefügt werden
- Benutzer haben vollständige Kontrolle über Aktivierung
- Datenschutzprinzipien werden weiterhin eingehalten

## Mitwirkung

### F: Neue Funktionen benötigt oder Bug gefunden, was tun?

**A**: Beteiligungsweisen:
- **GitHub Issues** - [Problem oder Funktionsanfrage einreichen](https://github.com/NoWakeLock/NoWakeLock/issues)
- **Detaillierte Beschreibung** - Vollständige Problembeschreibung und Reproduktionsschritte bereitstellen
- **Aktives Feedback** - Entwickler werden sich bemühen, alle Rückmeldungen zu bearbeiten

### F: Wie bei Übersetzungsaktualisierung helfen?

**A**: Übersetzungsbeitrag:
- **Pull Request** - Übersetzungs-PR direkt einreichen
- **Mehrsprachige Unterstützung** - Übersetzungsbeiträge in verschiedenen Sprachen willkommen
- **Community-Zusammenarbeit** - Übersetzungsdetails können in Community-Gruppen diskutiert werden

## Hilfe erhalten

### F: Wo Probleme melden?
**A**: Support-Kanäle:
- **GitHub Issues** - Detaillierte technische Probleme und Funktionsanfragen
- **Telegram-Gruppe** - [@nowakelock](https://t.me/nowakelock) Schnelle Anfragen und Diskussionen
- **Discord-Community** - [NoWakelock](https://discord.gg/kewmG5AShQ) Vertiefte technische Kommunikation

### F: Wie effektive Problemberichte bereitstellen?
**A**: Zu enthaltende Informationen:
1. **Geräteinformationen** - Marke, Modell, Android-Version
2. **Framework-Informationen** - Xposed-Framework-Typ und -Version
3. **Problembeschreibung** - Spezifische Problemphänomene
4. **Reproduktionsschritte** - Wie das Problem reproduziert wird
5. **Protokollinformationen** - Relevante Fehlerprotokolle
6. **Screenshots** - Screenshots der Problemoberfläche

### F: Wie lange dauert eine Antwort?
**A**: Antwortzeiten:
- **Community-Gruppen** - Normalerweise innerhalb weniger Stunden
- **GitHub Issues** - 1-3 Werktage
- **Dringende Probleme** - Werden priorisiert behandelt

!!! tip "Nutzungsempfehlung"
    Bei Problemen wird empfohlen, zuerst Dokumentation und FAQ zu konsultieren. Falls das Problem weiterhin besteht, stellen Sie bitte detaillierte Informationen bereit, um bessere Hilfe zu ermöglichen.

!!! warning "Wichtige Erinnerung"
    Vor Gebrauch bitte wichtige Daten sichern. Falsche Konfiguration kann den normalen Gerätebetrieb beeinträchtigen, bitte vorsichtig vorgehen.