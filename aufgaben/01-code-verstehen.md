# Übung 1 – Code verstehen mit Claude Code

Diese Übung zeigt dir, wie du Claude Code als Partner einsetzt, um dich schnell und strukturiert in eine unbekannte Codebasis einzuarbeiten – hier: *mav-guard*. Statt "alles auf einmal" lernst du, gezielte Fragen zu stellen, den Kontext klein zu halten und Antworten in nützliche Artefakte (Diagramme & Dokumentation) zu überführen.

**Nach dieser Übung kannst du:**
- die Projektstruktur und Hauptverantwortlichkeiten in wenigen Minuten herausarbeiten,
- ein leichtgewichtiges Architekturdiagramm in **Mermaid** erzeugen und anpassen,
- die bestehende Teststrategie aus dem Code ableiten, als Markdown dokumentieren und mit Claude diskutieren,
- mit Befehlen wie `/clear` und Datei‑Referenzen (`@pfad/zur/datei`) den Kontext bewusst steuern und so Qualität/Kosten im Griff behalten.

---

## 1.1 Allgemeines Codeverständnis & Architekturüberblick

**Ziel:** Verstehe, wie Claude den Code "scannt" und welche Informationen es liefern kann,
wenn du das Scope klar eingrenzt.

**Aufgabe:**
- Frag Claude z. B.:

```text  
Erkläre mir auf hoher Ebene, was das Projekt "mav-guard" macht.  
Welche Module gibt es, welche Hauptverantwortlichkeiten haben sie?  
Erstelle eine kurze Stichpunktliste.  
```

- Optional: Lass dir eine Tabelle "Modul → Zweck" ausgeben.

**Erwartetes Ergebnis:** Ein kurzer Text mit den wichtigsten Modulen, deren Aufgaben und wie sie zusammenspielen.

**Zeitaufwand:** 10 Minuten

---

## 1.2 Architekturdiagramm erzeugen lassen (Mermaid)

**Ziel:** Sieh, dass Claude nicht nur Text, sondern auch einfache Diagramme generieren kann.

**Aufgabe:**
- Bau auf deinem Ergebnis aus 1.1 auf.
- Frag Claude z. B.:

```text  
Erstelle mir ein einfaches Mermaid-Diagramm (flowchart oder classDiagram),  
das die wichtigsten Module und ihre Abhängigkeiten in mav-guard zeigt.  
```

- Kopiere das generierte Mermaid-Snippet in einen Editor (z. B. https://mermaid.live/) und render es.

**Erwartetes Ergebnis:** Ein Mermaid-Diagramm, das den groben Aufbau von mav-guard visualisiert.

**Zeitaufwand:** 5 Minuten

---

## 1.3 Spezielle Teilbereiche untersuchen (Teststrategie)

**Ziel:** Finde gezielt heraus, wie Tests im Projekt organisiert sind und dokumentiere die Erkenntnisse.

**Aufgabe:**
- Frag Claude z. B.:

```text  
Analysiere nur den Ordner "src/test" in allen Modulen. Welche Testarten gibt es (Unit-, Integration-, End-to-End)?  Wie wird ein End-to-End-Szenario getestet? Erstelle eine kurze Testdokumentation.  
```

- Lass dir das Ergebnis von Claude als Markdown-Text geben.
- Lass Claude eine Datei `docs/test-strategy.md` im Repo erzeugen. Wenn Claude die Datei nicht automatisch in dem Verzeichnis mit dem Namen abgelegt hat (was wahrscheinlich ist, wenn du es nicht explizit in dem Initial-Prompt geschrieben hast) - bitte Claude die Datei dorthin zu verschieben & korrekt zu benennen.
- Schau, ob die dokumentierte Test Strategy den Kern der End-to-End Test Szenarien beschreibt.
- Kannst du nach dem Lesen des fertigen Dokuments folgende Fragen beantworten. Wenn nicht, stell Claude Follow-Up Fragen und lass diese Ergebnisse in das Ergebnis Dokument einarbeiten: 
  1. Nutzt das Projekt für Integrationstests lediglich simulierte Spring Kontexte, oder lässt es auch die Anwendung als "black-box" auf echten Testprojekten arbeiten?
  2. Wie sicher bist du dir, dass in dem Projekt insgesamt eine gute / schlechte QA Strategie existiert?
  3. Wie groß ist ungefähr der Anteil der Unit- / Integrations- / End-to-End-Tests?

**Erwartetes Ergebnis:** Eine kurze Teststrategie-Dokumentation (in Markdown), die Testarten und End-to-End-Szenarien beschreibt.

**Zeitaufwand:** 10 Minuten

## 1.4 Teststrategie mit Claude diskutieren und Optimierungspotenziale finden

**Ziel:** Auf Basis deiner erstellten Teststrategie Claude Code aktiv nach Verbesserungspotenzialen fragen und Vorschläge sammeln.

**Aufgabe:**
- Leere den Context: `/clear` um mit einer leeren Conversation zu starten
- Frag Claude z. B.:

```text
Lies die Datei @docs/test-strategy.md und analysiere sie.  
Welche Optimierungspotenziale siehst du bei der Umsetzung der Teststrategie?  
Fehlt etwas Grundsätzliches?  
Gibt es bestimmte Teilbereiche im Code, die nicht abgedeckt sind?  
Gibt es bestimmte Fälle, die nicht berücksichtigt wurden?  
Was könnte man noch verbessern, um eine bessere Qualitätssicherung zu erzeugen (z. B. Einsatz von Coverage, statischer Analyse, zusätzliche Checks)?  
```

- Verwende `@` um Dateien zu referenzieren
- Lass Claude seine Verbesserungsvorschläge in eine neue Markdown-Datei
  `docs/test-improvements.md` schreiben (über eine Follow-up-Nachricht). Dort sollen 2–3 konkrete Klassen angegeben werden, bei denen sich Unit-Test-Coverage verbessern lässt. Stelle sicher, dass `DependencyConflictResolver.java` enthalten ist. Wenn nicht, frag noch einmal bei Claude nach, warum diese nicht enthalten ist.

**Erwartetes Ergebnis:** Eine Liste konkreter Verbesserungsvorschläge für Teststrategie und Qualitätssicherung inkl. Referenz auf `DependencyConflictResolver.java`.

**Zeitaufwand:** 10 Minuten
