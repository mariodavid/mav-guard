# 🛡️ MavGuard – Your Maven Dependency Watchdog

**MavGuard** ist ein leichtgewichtiges Tool zur kontinuierlichen Überwachung und Aktualisierung von Maven-Dependencies in Java-Projekten.  
Es prüft regelmäßig alle `pom.xml`-Dateien in Deinem Code-Repository und identifiziert:

- veraltete Versionen und mögliche Updates
- Security Advisories für bekannte Libraries
- inkonsistente Versionsnutzung über Module hinweg
- SNAPSHOT-Abhängigkeiten in produktiven Kontexten

## ✨ Features

- Integration in CI/CD-Pipelines (z.B. über GitHub Actions oder Jenkins)
- Konfigurierbare Regeln (z.B. keine Major-Upgrades automatisch)
- Ausgabe als JSON, Markdown oder Pull-Request-Kommentare
- Optional: automatisches Öffnen von PRs mit Upgrade-Vorschlägen
- Kompatibel mit OSS-Scannern wie OWASP Dependency-Check oder Snyk

## 🧑‍💻 Use Case

Perfekt für Teams, die Abhängigkeiten unter Kontrolle halten wollen,  
ohne manuell `mvn versions:display-dependency-updates` zu fahren oder auf Dependabot zu warten.