# 04 – Produktionscode erzeugen

In diesem Modul geht es um Feature-Entwicklung mit Claude Code - von der initialen Issue-Beschreibung über die strukturierte Problemanalyse bis zur vollständigen Implementierung. Ihr seht, wie Claude aus minimalen Beschreibungen aussagekräftige GitHub Issues erstellt, komplexe Features in logische Schritte zerlegt und dabei Produktionscode mit Tests schreibt.

Nach diesem Modul wisst ihr:
- wie ihr Claude mit minimalen Prompts GitHub Issues erstellen lasst, die technische Details und Implementierungsideen enthalten,
- wie Claude komplexe Feature-Requests strukturiert analysiert und in implementierbare Schritte aufteilt,
- wie Claude Produktionscode schreibt und dabei bestehende Patterns und Architekturen respektiert,
- wie sich Claude bei Feature-Implementierung verhält: eigenständige Codebase-Analyse, Pattern-Erkennung und konsistente Umsetzung.

---

## 4.1 GitHub Issue für Milestone-Release-Filterung erstellen

**Ziel:** Claude soll aus einer minimalen Problembeschreibung eigenständig die Codebase analysieren und dann eine strukturierte GitHub Issue erstellen.

**Problem:** Das `check-updates` Command zeigt Milestone-Releases als "Latest" an:
```
DEPENDENCY                                         CURRENT                    LATEST
-------------------------------------------------------------------------------------------------
org.springframework:spring-core                    5.3.27               → 7.0.0-M8
```

Milestone-Releases (M1, M2, RC1, etc.) sollten nicht als empfohlene Updates vorgeschlagen werden.

**Aufgabe:**
- Leert den Context mit `/clear`.
- Gebt Claude nur die minimale Beschreibung:

```text
check-updates zeigt milestone releases. Das ist schlecht:
org.springframework:spring-core 5.3.27 → 7.0.0-M8

Erstell mal ein GH Issue dafür. Wer will denn bitte sein Produktionscode auf Milestone Releases umstellen?! Schau dir aber erst mal die fachlichen Details an - also welche ähnliche Fälle könnten wir auch filtern wollen... In dem GH Issue will ich aber keine fertig Lösung schon beschrieben haben, das wäre ja dann langweilig in der Umsetzung, nicht wahr? ;) 
```

- Claude soll eigenständig eine aussagekräftige Issue-Beschreibung schreiben mit:
  - Strukturierte Problem-Beschreibung
  - Erwartetes vs. aktuelles Verhalten
  - Kontext aus der Codebase (welche Komponenten sind betroffen)
- Nutze den Plan Mode, wenn du eine Feedback-Loop über die Issuebeschreibung haben möchtest

**Erwartetes Ergebnis:**
- Ein GitHub Issue wurde erstellt mit strukturierter Beschreibung
- Claude hat die Codebase analysiert und relevante Komponenten identifiziert
- Das Issue enthält Kontext darüber, wo das Problem liegt
- Claude hat die GitHub CLI eigenständig verwendet


**Learnings:**
- Der entscheidende Erfolgsfaktor bei Code-Generierung mit Claude ist der Prompt. Wenn man versucht, das ad-hoc dem Modell zu erklären, scheitert man oft, weil man nicht alle relevanten Details bereitstellt - man beherrscht ja nicht die 20-Finger-Tipp-Technik. Daher: Nutzt Claude selbst, um Issue-Beschreibungen in der richtigen Detailtiefe zu erstellen.
- Durch Integration mit Umsystemen (GitHub, Jira, Confluence) wird Claude Code auch für Non-Coding-Tasks nutzbar: strukturierte Issue-Erstellung, Dokumentation, Projektmanagement - alles direkt aus dem Terminal heraus.
- Nutzt Speech-to-Text (Windows native, Mac: MacWhisperer), damit selbst kleine Prompts euch nicht mehr davon abhalten, eure Gedanken mit dem LLM zu teilen. Ihr werdet merken, wie schnell ihr müde werdet, dem LLM alles schriftlich zu erklären. Sprache ermöglicht höheren Durchsatz - selbst wenn die Spracherkennung nicht alles korrekt versteht, kann das LLM mit diesen Unschärfen trotzdem umgehen.

## 4.2 Feature implementieren basierend auf GitHub Issue

**Ziel:** Claude soll das in 4.1 erstellte GitHub Issue als Grundlage nehmen und das Feature vollständig implementieren - inklusive Tests.

**Aufgabe:**
- Leert den Context mit `/clear`.
- Gebt Claude den minimalen Prompt:

```text
Implementiere mal das Feature aus diesem Issue: <<INSERT-GH-ISSUE-NUMBER-HERE>>
```

- Nach der Implementierung:
  - Fragt Claude nach einer Erklärung der Lösung
  - Fragt, welche Tests erstellt wurden und welche Fälle gecovered werden
  - Vergleicht die Erklärung mit dem tatsächlichen Produktionscode

**Erwartetes Ergebnis:**
- Claude analysiert das Issue und versteht die Anforderungen
- Claude findet die betroffenen Komponenten in der Codebase
- Die Milestone-Release-Filterung wurde implementiert
- Eine Implementierung wurde geschrieben, die zu den bestehenden Patterns passt
- Tests wurden für die neue Funktionalität erstellt
- Tests wurden ausgeführt und laufen erfolgreich durch
- Die Implementierung ist vollständig und funktionsfähig

## 4.3 Pull Request erstellen und mergen

**Ziel:** Claude soll einen Pull Request erstellen, GitHub Actions abwarten und bei erfolgreichem Build den PR automatisch mergen.

**Aufgabe:**
- Bittet Claude einen GitHub Pull Request zu erstellen mit strukturierter Beschreibung basierend auf der Erklärung die es euh zuvor gegeben hat.
- Claude soll die GitHub Actions abwarten und prüfen, ob das Build grün ist.
- Bei erfolgreichem Build soll Claude den PR mergen.

```text
Erstell mal einen PR für das Feature und warte die Actions ab. Wenn alles grün ist, merge den PR.
```

**Erwartetes Ergebnis:**
- Ein GitHub Pull Request wurde erstellt mit aussagekräftiger Beschreibung
- Claude überwacht die GitHub Actions und wartet auf Completion
- Bei erfolgreichem Build wird der PR automatisch gemerged

---

## 4.4 Große repetitive Aufgabe: Logging implementieren

**Ziel:** Claude soll das Logging-Issue aus Aufgabe 2.4 implementieren und dabei zeigen, warum große Tasks problematisch sind.

**Problem:** Wenn man Claude eine große repetitive Aufgabe gibt ("bau mal Logging ein"), läuft es minutenlang autonom, aber übersieht oft Details. Hat es alle Dateien erwischt? Alle Guideline-Muster umgesetzt? Als Mensch ist das kaum überprüfbar bei großen Changes.

**Aufgabe:**
- Leert den Context mit `/clear`.
- Gebt Claude nur den minimalen Prompt:

```text
Bau das mal: gh issue view <<LOGGING-ISSUE-NUMBER-VON-AUFGABE-2-4>>
```

- Lasst Claude komplett autonom arbeiten ohne Unterbrechung
- Beobachtet, wie lange es läuft und was es alles macht
- Am Ende: versucht zu verstehen und zu reviewen was gemacht wurde

**Erwartetes Ergebnis:**
- Claude implementiert Logging in vielen Dateien über mehrere Minuten hinweg
- Ihr werdet merken, dass der Review sehr schwierig wird
- Wahrscheinlich hat Claude nicht alle relevanten Stellen erwischt oder nicht alle Guideline-Patterns umgesetzt
- Der Context wird voll und die Qualität (zusammen mit dem Geld im Geldbeutel) nimmt ab

**Learning: "Hack es klein"**
Sub-Tasks sind ultra wichtig bei großen repetitiven Aufgaben. Statt "implementiere Logging überall" sollte man aufteilen in:
- Logging für User-Flow A (CLI-Parsing)
- Logging für User-Flow B (POM-Parsing)
- Logging für Error-Handling
- etc.

Kleine Sub-Tasks haben Vorteile:
- Context bleibt klein (kostet weniger, bessere Qualität)
- Review wird einfacher (egal ob Mensch oder AI)
- Claude macht weniger Fehler bei fokussierten Aufgaben
- Man kann iterativ nachbessern

**Resourcen**:
[Context Window Depletion](https://claudelog.com/mechanics/context-window-depletion/) - wenn der Context voll wird, sinkt die Qualität exponentiell

---

## 4.5 Kleine Tasks: Aufgabe intelligent zerteilen

**Ziel:** Claude soll die große Logging-Aufgabe in sinnvolle kleine Sub-Tasks aufteilen und diese parallel bearbeiten.

**Aufgabe:**
- Revertiert zuerst den großen Change aus 4.4: Nein, benutzt NICHT `git reset --hard HEAD~X`, sondern bittet Claude höflich darum dies für euch zu tun. "Schmeiss die Implementierung mal weg" funktioniert genauso gut.
- Leert den Context mit `/clear`.
- Gebt Claude den Auftrag zur intelligenten Aufteilung:

```text
Nimm das Logging-Issue <<LOGGING-ISSUE-NUMBER-VON-AUFGABE-2-4>> und untersuche die Codebase. Wie können wir das in 3-4 sinnvolle Sub-Tasks aufteilen? Am besten nach Workflows/Code-Paths.

Leg dann für jede Sub-Task ein eigenes GitHub Issue an.
```

- Startet 4 Terminal-Sessions parallel
- Gebt jedem Claude-Agent eine der Sub-Tasks:

```text
Terminal 1: Implementiere Logging für [WORKFLOW-A]: Hier ist das GH Issue [ISSUE-1]
Terminal 2: Implementiere Logging für [WORKFLOW-B]: Hier ist das GH Issue [ISSUE-2]
Terminal 3: Implementiere Logging für [WORKFLOW-C]: Hier ist das GH Issue [ISSUE-3]
Terminal 4: Implementiere Logging für [WORKFLOW-D]: Hier ist das GH Issue [ISSUE-4]
```

**Erwartetes Ergebnis:**
- Claude analysiert die Codebase und teilt die Aufgabe intelligent auf
- 3-4 GitHub Issues werden für verschiedene Workflows erstellt
- Parallel laufende Claude-Agents bearbeiten ihre jeweiligen Sub-Tasks


**Learnings:**
- Parallelisierung ist möglich und effizient 😳🤯😱 - aber mit Fallstricken
- Ein Git-Repository + mehrere parallele Agents = potentielle Konflikte
- Agents können sich gegenseitig ihre Änderungen überschreiben
- Lösung: verschiedene Branches, verschiedene Dateien, oder sequentielle Bearbeitung mit separaten Commits
- Advanced: [Git Worktrees](https://www.anthropic.com/engineering/claude-code-best-practices) für echte Parallelisierung (sehr fortgeschritten, aber mächtig)
