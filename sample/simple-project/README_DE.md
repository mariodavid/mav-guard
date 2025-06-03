# Einfaches Maven-Projekt

Dieses Projekt ist ein einfaches Maven-Beispiel für MavGuard.

## Struktur

- Einzelnes Modul mit direkter `pom.xml`
- Mehrere Abhängigkeiten mit unterschiedlichen Versionen
- Verwendung von Maven-Properties für Versionen

## Dependency-Checks mit MavGuard

So führen Sie einen Dependency-Check mit MavGuard durch:

### Schritt 1: Stellen Sie sicher, dass MavGuard gebaut wurde

Navigieren Sie zum Root-Verzeichnis des MavGuard-Projekts und führen Sie den Maven-Build aus:

```bash
cd ../../
mvn clean install
```

### Schritt 2: Basic Dependency-Checks

```bash
# Navigieren Sie zurück zum simple-project
cd sample/simple-project

# Abhängigkeiten extrahieren und anzeigen
java -jar mav-guard-cli.jar xml extract-dependencies pom.xml

# POM parsen und grundlegende Informationen anzeigen
java -jar mav-guard-cli.jar xml parse-pom pom.xml
```

### Schritt 3: Abhängigkeiten auf Updates prüfen

```bash
# Prüfen, ob es neuere Versionen der Abhängigkeiten gibt
java -jar mav-guard-cli.jar dependencies check-updates pom.xml
```

### Beispielausgabe

Bei der Ausführung des Befehls `xml extract-dependencies` sollten Sie eine Ausgabe ähnlich der folgenden sehen:

```
Dependencies found in POM file:
- org.springframework:spring-core:5.3.27
- org.springframework:spring-context:5.3.27
- com.fasterxml.jackson.core:jackson-databind:2.14.2
- org.apache.commons:commons-lang3:3.12.0
- org.junit.jupiter:junit-jupiter:5.9.2 (scope: test)
- org.mockito:mockito-core:5.3.1 (scope: test)
```

Bei `dependencies check-updates` sollten Sie eine Ausgabe ähnlich der folgenden sehen:

```
Checking for updates for dependencies in com.example:simple-project:1.0.0
-----------------------------------------------------
org.springframework:spring-core              5.3.27 ->   7.0.0-M4
org.springframework:spring-context           5.3.27 ->   7.0.0-M4
com.fasterxml.jackson.core:jackson-databind     2.14.2 ->     2.19.0
org.apache.commons:commons-lang3             3.12.0 -> 3.17.0.redhat-00001
org.junit.jupiter:junit-jupiter               5.9.2 ->  5.13.0-M3
org.mockito:mockito-core                      5.3.1 ->     5.17.0
```