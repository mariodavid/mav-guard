# 06 – Debugging

In diesem Modul lernt ihr, wie Claude Code beim Debugging hilft - von der Fehleranalyse über interaktive Lösungsfindung bis zur autonomen Implementierung. Ihr seht, wie der Feedback-Loop zwischen Mensch und AI zu robusteren Lösungen führt als eine direkte "Fix das mal"-Anweisung.

Nach diesem Modul wisst ihr:
- wie Claude Fehlermeldungen analysiert und Hypothesen bildet,
- wie der interaktive Debugging-Prozess funktioniert: erst naive Lösung, dann durch Feedback zur besseren Lösung,
- wie Claude eigenständig API-Dokumentation recherchiert und elegante Lösungen entwickelt,
- warum die Kombination aus interaktivem Design und autonomer Implementierung sehr effektiv ist.

---

## 6.1 Debugging: 404-Fehler bei Parent-Version-Checks (Interaktiver Modus)

**Ziel:** Claude soll einen 404-Fehler analysieren und dabei zeigen, wie der interaktive Debugging-Prozess funktioniert - erst eine naive Lösung, dann durch Feedback zur besseren Lösung.

**Problem:** Das `check-updates` Command zeigt einen 404-Fehler bei lokalen Parent-Projekten:
```
$ java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar check-updates sample/simple-project/pom.xml

...
Error fetching parent versions from Maven Central: 404 Not Found: "<html>..."
```

**Aufgabe:**
- Leert den Context mit `/clear`.
- Führt das fehlerhafte Command aus:
```bash
java -jar mav-guard-cli.jar check-updates sample/simple-project/pom.xml
```
- Gebt Claude die Ausgabe und beschreibt das Problem minimal:

```text
fuehre mal 'java -jar mav-guard-cli.jar check-updates sample/simple-project/pom.xml' aus. unten kommt 404. woran könnte das liegen?
```

- Claude wird vermutlich zunächst eine naive Lösung vorschlagen (z.B. SNAPSHOT-Check)
- **Wichtig:** Unterbrecht Claude, wenn er eine zu einfache Lösung macht und fragt nach (ESC):

```text
ne, kann man das denn nicht besser lösen? In dem man programmatisch checkt ob es da ist?
```

- Fordert Claude auf, nach einer API-basierten Lösung zu suchen:

```text
kannst du nicht auf den http code checken, oder eine api benutzen die das abfragen erlaubt? Google mal welche APIs es für Maven Central aber auch für Nexus gibt.
```

- **Plan Mode verwenden:** Lasst Claude einen strukturierten Plan für die Lösung erstellen
- Lasst Claude recherchieren und eine elegantere HTTP-basierte Lösung entwickeln

**Erwartetes Ergebnis:**
- Claude analysiert das Problem Schritt für Schritt
- Eine erste, naive Lösung wird vorgeschlagen
- Durch euer Feedback entwickelt Claude eine bessere API-basierte Lösung
- Claude erstellt einen strukturierten Plan für die HTTP-basierte Lösung
- Der 404-Fehler wird elegant über HTTP Status Codes abgefangen
- Ihr erlebt den interaktiven Debugging-Prozess mit Feedback-Loops im Plan Mode

**Zeitaufwand:** 15 Minuten

**Learnings:**
- Claude Code eignet sich hervorragend für interaktives Debugging
- Erste Lösungsvorschläge sind oft naiv - Feedback führt zu besseren Ansätzen
- HTTP APIs und Status Codes bieten elegantere Lösungen als String-Parsing
- Der Plan Mode hilft bei der strukturierten Lösungsfindung
- Der Feedback-Loop zwischen Mensch und AI führt zu robusteren Lösungen

## 6.2 Debugging-Lösung implementieren und testen (Autonomer Modus)

**Ziel:** Claude soll die erarbeitete HTTP-basierte Lösung eigenständig implementieren und ausführlich testen.

**Aufgabe:**
- Gebt Claude den Auftrag zur vollständigen Implementierung:

```text
ok, kannst du mal versuchen das zu fixen? Teste auch gleich ob es funktioniert und führe die Anwendung aus um zu checken ob der 404-Fehler weg ist (mit dem Kommando von oben).
```

- Claude soll eigenständig:
  - Die HTTP Status Code Lösung implementieren
  - Sowohl Maven Central als auch Nexus Repository Service anpassen
  - Das Projekt bauen
  - Die Anwendung mit dem ursprünglichen Command testen
  - Den Erfolg der Lösung verifizieren

**Erwartetes Ergebnis:**
- Claude implementiert HTTP Requests im NexusClient
- Beide Repository Services (Maven Central und Nexus) werden angepasst
- Das Projekt wird erfolgreich gebaut
- Die Anwendung läuft ohne 404-Fehler
- Claude führt eigenständig die Anwendung aus und bestätigt den Fix

**Zeitaufwand:** 10 Minuten

**Learnings:**
- Claude kann eigenständig komplexe Fixes implementieren und testen
- Die Kombination aus interaktivem Design und autonomer Implementierung ist sehr effektiv
- Claude verifiziert seine Lösung selbstständig durch Ausführung der Anwendung
- API-basierte Lösungen sind robuster als String-basierte Workarounds