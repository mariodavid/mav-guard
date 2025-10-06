# 03 – Claude Werkzeug Verwendung

In dieser Übung geht es darum, die praktischen Möglichkeiten von Claude Code als Shell-Helfer zu erleben. Du lernst, dass Claude nicht nur Quellcode analysieren kann, sondern auch Bash-Befehle ausführen und damit komplexe Aufgaben wie das Lesen und Schreiben von Dateien, das Ausführen von Tests, den Umgang mit git oder die Nutzung der GitHub CLI erledigen kann. Ziel ist es, ein Gefühl dafür zu bekommen, wie du diese Funktionen gezielt nutzen kannst, um Arbeitsschritte zu automatisieren und mit externen Systemen in natürlicher Sprache zu interagieren.

## Nach dieser Übung weißt du:

- wie du Claude in deinen eigenen Programmen verwenden kannst.
- wie du Claude als interaktiven Assistenten in der Kommandozeile einsetzt.
- wie du Claude über eine API ansteuerst.
- wie du Claude als Werkzeug in deiner Shell nutzt.
- wie du Claude einsetzen kannst, um mit externen Systemen über CLI-Tools wie die GitHub CLI in natürlicher Sprache ("in human language") zu interagieren.

## 3.1 Projektverlauf aus Git-Historie ermitteln

**Ziel:** Erlebe, dass Claude eigenständig die Git-Historie analysiert und dir in normaler Sprache eine Übersicht liefert.

**Aufgabe:**
- Leere den Context: `/clear` um mit einer leeren Conversation zu starten
- Frag Claude z. B.:

```text
Beschreibe mir auf Basis der Git-Historie, wie sich das Projekt entwickelt hat.  
Wer war beteiligt?  
Wurde KI eingesetzt?  
```

- basierend auf den Ergebnissen, stellt weitere sinnvolle Nachfragen, wie bspw. "Wann ist das Nexus-Modul hinzugekommen?" oder auch "gab es mal Versuche statt eigenes pom.xml parsing eine fertige Library zu verwenden?"

**Erwartetes Ergebnis:** Eine narrative Zusammenfassung der Projektgeschichte auf Basis der Git-Historie. Claude führt auch kompliziertere Shell Befehle wie `Bash(git log --all --oneline | xargs -I {} git show {} --name-only | grep -i "maven.*model\|resolver\|aether" | head -20)` aus, um mithilfe von git und anderen CLI Werkzeugen Informationen sinnvoll zu ermitteln

**Zeitaufwand:** 5 Minuten


## 3.2 Neue Datei anlegen und committen

**Ziel:** Erlebe, dass Claude eigenständig Dateien anlegt und git-Operationen ausführt, inklusive sinnvoller Commit-Message.

**Aufgabe:**
- Frag Claude z. B.:

```text
Committe bitte die neue Datei  @docs/testing-strategy.md mit einer passenden Commit-Message.
```

**Erwartetes Ergebnis:**
- Das Git-Commit-Kommando sollte noch von dir bestätigt werden und dadurch hattest du noch die Möglichkeit die Commit Message zu sehen.
- Ein Git-Commit mit einer sinnvollen Commitmessage wurde erstellt.

**Zeitaufwand:** 5 Minuten


## 3.3 GitHub CLI: Neues Issue im Template Repository anlegen

**Ziel:** Erlebe, dass Claude über die authentifizierte GitHub CLI direkt mit GitHub interagieren und eigenständig ein Issue in deinem Repository anlegen kann.

**Aufgabe:**
- Frag Claude z. B.:

```text
Lege ein neues GitHub-Issue an. Den Inhalt des Issues findest du in der Datei @aufgaben/04-test-und-refactorings.md.
Gib mir am Ende den erzeugten Issue‑Link zurück oder öffne den direkt im Browser wenn es geht.
```

- Falls Claude Rückfragen hat (z.B. ob `gh` bereits authentifiziert ist oder wo die Aufgaben‑Datei liegt), beantworte sie knapp oder lass es die nötigen Informationen selbst ermitteln.
- Wenn es geklappt hat, bitte Claude in einer Follow-up-Nachricht dies für alle Teilaufgaben in der Datei (4.1 - 4.5) durchzuführen.

**Erwartetes Ergebnis:**
- Ein neues Issue wurde angelegt.
- Der Issue‑Text basiert auf der Beschreibung aus Aufgabe 4.1 (aus der Markdown‑Datei im `aufgaben/`‑Verzeichnis).
- Claude gibt den direkten Link zum neu erstellten Issue aus.

**Zeitaufwand:** 5 Minuten


## 3.4 Web-Fetch + lokale Code-Analyse + Aktion ausführen

**Ziel:** Kombiniere "web-fetch", lokales Dateien-Lesen/Interpretieren und eine daraus abgeleitete Aktion. Du erlebst, wie Claude externe Informationen holt, gegen den lokalen Code spiegelt und anschließend selbständig handelt.

**Aufgabe:**
- Leere den Context mit `/clear`.
- Frag Claude z. B.:

```text
Lies bitte das GitHub-Issue https://github.com/mariodavid/mav-guard/issues/16 (Implement comprehensive logging based on logging guidelines) und fasse die Anforderung kurz zusammen.
Prüfe anschließend, ob die beschriebene Funktionalität bereits implementiert ist.
– Falls ja: Zeige mir die relevanten Dateien/Methoden/Commits und eine kurze Begründung.
– Falls nein: Lege ein neues GitHub-Issue an, das die Ticket Beschreibung aus dem Ursprungsticket übernimmt. Verlinke das Ursprungsticket (#16) und füge einen kurzen Kommentar hinzu, der die Referenz erklärt. Antworte mir bitte mit "🤠 Yeeeehaaaa, Sir. I've done my Job! ✅🎉" wenn alles fertig ist...
```

**Erwartetes Ergebnis:**
- Das externe Issue #16 wurde via web-fetch gelesen und präzise zusammengefasst.
- Claude hat geprüft, ob die Funktionalität lokal existiert und ermittelt das dies nicht umgesetzt ist.
- Ein neues Issue wurde im deinem Repository angelegt, das auf #16 verlinkt; ein Kommentar mit der Referenz ist enthalten.
- Der direkte Link zum neu erstellten Issue wurde ausgegeben und viel wichtiger: "🤠 Yeeeehaaaa, Sir. I've done my Job! ✅🎉" kam als Antwort.

**Zeitaufwand:** 10 Minuten
