# Multi-Module Maven-Projekt

Dieses Projekt ist ein Beispiel für ein Maven Multi-Module-Projekt für MavGuard.

## Struktur

```
multi-module-project/
├── pom.xml                  (Root POM)
├── moduleA/                 (Modul A)
│   ├── pom.xml
│   └── src/
├── moduleB/                 (Modul B)
│   ├── pom.xml
│   └── src/
└── moduleC/                 (Modul C mit Untermodul)
    ├── pom.xml
    ├── src/
    └── submoduleD/          (Untermodul D)
        ├── pom.xml
        └── src/
```

## Besonderheiten

- Hierarchische Modulstruktur mit verschachtelten Modulen
- Unterschiedliche Eigenschaftswerte (Properties) auf verschiedenen Ebenen:
  - `moduleB` verwendet eine andere Jackson-Version als die Parent-POM
  - `moduleC` verwendet eine andere JUnit-Version als die Parent-POM
  - `submoduleD` verwendet eine andere Mockito-Version als die Parent-POM
- Abhängigkeiten zwischen Modulen (`moduleB` hängt von `moduleA` ab usw.)

## Dependency-Checks mit MavGuard

So führen Sie Dependency-Checks für ein Multi-Module-Projekt durch:

### Schritt 1: MavGuard bauen (falls noch nicht geschehen)

```bash
cd ../../
mvn clean install
cd sample/multi-module-project
```

### Schritt 2: Multi-Module-Analyse ausführen

Dies ist der wichtigste Befehl für die Analyse von Multi-Module-Projekten:

```bash
# Grundlegende Analyse des Multi-Module-Projekts
java -jar mav-guard-cli.jar xml analyze-multi-module pom.xml
```

Die Ausgabe zeigt eine Zusammenfassung der erkannten Module und Abhängigkeiten sowie mögliche Versionsinkonsistenzen.

### Schritt 3: Detaillierte Abhängigkeitsanalyse

Für mehr Details zu den Abhängigkeiten in allen Modulen:

```bash
# Detaillierte Abhängigkeitsanalyse mit Modulnutzung
java -jar mav-guard-cli.jar xml analyze-multi-module pom.xml --detailed-usage
```

### Schritt 4: Überprüfung auf Versionsinkonsistenzen

Dieser Befehl gibt einen Fehlercode zurück, wenn Versionsinkonsistenzen gefunden werden:

```bash
# Überprüfung auf Versionsinkonsistenzen (gibt Fehlercode zurück wenn gefunden)
java -jar mav-guard-cli.jar xml analyze-multi-module pom.xml --check-inconsistencies
```

Dieses Sample-Projekt enthält absichtlich Versionsinkonsistenzen zur Demonstration:
- Jackson: 2.13.4 (Root) vs. 2.14.0 (moduleB)
- JUnit: 5.8.2 (Root) vs. 5.9.1 (moduleC)
- Mockito: 4.8.0 (Root) vs. 5.2.0 (submoduleD)

### Schritt 5: Extraktion von Abhängigkeiten aus einem einzelnen Modul

Wenn Sie nur ein einzelnes Modul analysieren möchten:

```bash
# Abhängigkeiten aus moduleB extrahieren
java -jar mav-guard-cli.jar xml extract-dependencies moduleB/pom.xml
```

### Beispielausgabe

Bei der Ausführung des Befehls `analyze-multi-module` sollten Sie etwa folgende Ausgabe sehen:

```
Multi-module project with X modules:
- com.example:multi-module-project:1.0.0-SNAPSHOT
- com.example:moduleA:1.0.0-SNAPSHOT
- com.example:moduleB:1.0.0-SNAPSHOT
- com.example:moduleC:1.0.0-SNAPSHOT
- com.example:submoduleD:1.0.0-SNAPSHOT

WARNING: Found inconsistent dependency versions:
Dependency com.fasterxml.jackson.core:jackson-databind has inconsistent versions:
  - Version 2.13.4 used in modules: multi-module-project, moduleA
  - Version 2.14.0 used in modules: moduleB

...
```