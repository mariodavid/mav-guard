# MavGuard Beispielprojekte

In diesem Verzeichnis finden Sie Beispielprojekte zur Demonstration der MavGuard-Funktionalitäten.

## Verfügbare Beispiele

### Simple-Project

Ein einfaches Maven-Projekt mit einer einzelnen POM-Datei. Dieses Projekt eignet sich gut für:
- Testen der grundlegenden Dependency-Extraktion
- Demonstration der Property-Auflösung

### Multi-Module-Project

Ein umfangreicheres Maven-Projekt mit mehreren verschachtelten Modulen. Dieses Projekt zeigt:
- Wie MavGuard mit Multi-Module-Projekten umgeht
- Verschiedene Abhängigkeitsversionen in unterschiedlichen Modulen
- Verschachtelte Module (bis zu zwei Ebenen)
- Abhängigkeiten zwischen Modulen

### Spring-Boot-Parent-Example

Ein Beispielprojekt mit Spring Boot als Parent-POM, das speziell für das Testen der Parent-Version-Prüfung konzipiert ist:
- Demonstriert die Prüfung auf verfügbare Updates für die Parent-POM
- Zeigt, wie MavGuard ältere Spring Boot Versionen identifiziert
- Enthält eine ausführliche README mit Testanweisungen

## Verwendung

Um die Beispiele mit MavGuard zu analysieren, navigieren Sie zunächst in das entsprechende Projektverzeichnis. Anschließend können Sie die MavGuard-Befehle ausführen, wie in den jeweiligen README-Dateien beschrieben.