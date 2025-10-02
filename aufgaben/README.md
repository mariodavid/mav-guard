# Schulungsaufgaben – Claude Code & mav-guard

Dieses Verzeichnis enthält alle Aufgaben und Übungen für die Schulung "Claude Code mit mav-guard". Die Markdown-Dateien sind so aufgebaut, dass ihr Schritt für Schritt lernt, Claude Code produktiv einzusetzen.

## Überblick über die Übungen

- **01 – Code verstehen:** Einstieg in Claude Code. Projektstruktur verstehen, Architekturdiagramm (Mermaid) erzeugen, Teststrategie ableiten und dokumentieren.
- **02 – Context Management:** CLAUDE.md automatisch generieren mit `/init`, Verhaltensänderungen durch CLAUDE.md-Anpassungen beobachten, hierarchische CLAUDE.md-Dateien in Multi-Modul-Projekten nutzen.
- **03 – Claude Werkzeug Verwendung:** Erleben, wie Claude Befehle in einer Shell ausführen kann (z. B. GitHub CLI oder git selbst). Wie begrenze ich den Kontext? Wie nutze ich `/clear`? Non-Interactive Mode und Automatisierungen.
- **04 – Tests und Refactorings:** Code schreiben mit Claude Code. Autonome längerlaufende Tasks, Plan Mode für strukturierte Refactorings, Test-Feedback-Schleifen und Baby-Agent-Supervision bei komplexen Aufgaben.
- **05 – Produktionscode erzeugen:** Kleine Feature-Erweiterung umsetzen (z. B. neues CLI-Flag), Tests generieren und Commit-Notizen erstellen.
- **06 – Debugging:** Vorbereitete Fehler analysieren, Hypothesen bilden, minimalen Fix durchführen, Tests grün bekommen.

Optional ergänzend:
- **Review & Refactoring:** Claude Code als Reviewer für PRs nutzen, Architektur gezielt umstrukturieren.
- **Qualität & Tests erweitern:** Coverage, statische Analyse, zusätzliche Checks.

## Arbeitsweise

- Jede Markdown-Datei beschreibt eine einzelne Übung mit Ziel, Aufgabe und erwartetem Ergebnis.
- Die Übungen bauen aufeinander auf, können aber auch einzeln durchgeführt werden.
- Ergebnisse und erstellte Artefakte (Dokumentationen, Diagramme) legt ihr im Ordner `docs/` ab.
- Die Beispiel-Prompts in den Aufgaben solltest du als Inspiration verwenden und durchlesen, um zu verstehen, was in einen guten Prompt gehört. Tipet die Prompts dann aber am besten selbst, um sowohl das Lesen als auch das eigene Formulieren zu üben.

So habt ihr einen klaren Fahrplan, um Claude Code von Grund auf kennenzulernen – vom Lesen und Verstehen über Planen, Implementieren und Debuggen bis hin zu automatisierten Workflows.