# 02 – Claude Werkzeug Verwendung

In dieser Übung geht es darum, die praktischen Möglichkeiten von Claude Code als Shell-Helfer zu erleben. Ihr lernt, dass Claude nicht nur Quellcode analysieren kann, sondern auch Bash-Befehle ausführen und damit komplexe Aufgaben wie das Lesen und Schreiben von Dateien, das Ausführen von Tests, den Umgang mit git oder die Nutzung der GitHub CLI erledigen kann. Ziel ist es, ein Gefühl dafür zu bekommen, wie ihr diese Funktionen gezielt nutzen könnt, um Arbeitsschritte zu automatisieren und mit externen Systemen in natürlicher Sprache zu interagieren.

## Nach dieser Übung wisst ihr:

- wie ihr Claude in euren eigenen Programmen verwenden könnt.
- wie ihr Claude als interaktiven Assistenten in der Kommandozeile einsetzt.
- wie ihr Claude über eine API ansteuert.
- wie ihr Claude als Werkzeug in eurer Shell nutzt.
- wie ihr Claude einsetzen könnt, um mit externen Systemen über CLI-Tools wie die GitHub CLI in natürlicher Sprache („in human language“) zu interagieren.

## 2.1 Projektverlauf aus Git-Historie ermitteln

**Ziel:** Erlebt, dass Claude eigenständig die Git-Historie analysiert und euch in normaler Sprache eine Übersicht liefert.

**Aufgabe:**
- Leert den Context: `/clear` um mit einer leeren Conversation zu starten
- Fragt Claude z. B.:

```text
Beschreibe mir auf Basis der Git-Historie, wie sich das Projekt entwickelt hat.  
Wer war beteiligt?  
Wurde KI eingesetzt?  
```

- basierend auf den Ergebnissen, stellt weitere sinnvolle Nachfragen, wie bspw. "Wann ist das Nexus-Modul hinzugekommen?" oder auch "gab es mal Versuche statt eigenes pom.xml parsing eine fertige Library zu verwenden?"

**Erwartetes Ergebnis:** Eine narrative Zusammenfassung der Projektgeschichte auf Basis der Git-Historie. Claude führt auch kompliziertere Shell Befehle wie `Bash(git log --all --oneline | xargs -I {} git show {} --name-only | grep -i "maven.*model\|resolver\|aether" | head -20)` aus, um mithilfe von git und anderen CLI Werkzeugen Informationen sinnvoll zu ermitteln


## 2.2 Neue Datei anlegen und committen

**Ziel:** Erlebt, dass Claude eigenständig Dateien anlegt und git-Operationen ausführt, inklusive sinnvoller Commit-Message.

**Aufgabe:**
- Fragt Claude z. B.:

```text
Committe bitte die neue Datei  @docs/testing-strategy.md mit einer initialen Teststrategie an und committe sie mit einer passenden Commit-Message.
```

**Erwartetes Ergebnis:**
- Das Git-Commit-Kommando sollte noch von euch bestätigt werden und dadurch hattet ihr noch die Möglichkeit die Commit Message zu sehen.
- Ein Git-Commit mit einer sinnvollen Commit Message wurde erstellt.


## 2.3 GitHub CLI: Neues Issue im Fork anlegen

**Ziel:** Erlebt, dass Claude über die authentifizierte GitHub CLI direkt mit GitHub interagieren und eigenständig ein Issue in eurem Fork (origin) anlegen kann.

**Aufgabe:**
- Fragt Claude z. B.:

```text
Lege in meinem aktuellen Repository (mein Fork) ein neues GitHub-Issue an (schau noch mal ob es wirklich mein Fork ist, oder ob ich nicht ausversehen noch auf dem original repository: github.com/mariodavid/mav-guard arbeite).  
Den Inhalt des Issues findest du in der Datei @aufgaben/03-code-schreiben.md.
Gib mir am Ende den erzeugten Issue‑Link zurück oder öffne den direkt im Browser wenn es geht.
```

- Falls Claude Rückfragen hat (z.B. ob `gh` bereits authentifiziert ist oder wo die Aufgaben‑Datei liegt), beantwortet sie knapp oder lasst es die nötigen Informationen selbst ermitteln.
- Wenn es geklappt hat, bitte Claude in einer Follow-up-Nachricht dies für alle Teilaufgaben in der Datei (3.1 - 3.5) durchführt.

**Erwartetes Ergebnis:**
- Ein neues Issue wurde angelegt.
- Der **Issue‑Text** basiert auf der Beschreibung aus **Aufgabe 3.1** (aus der Markdown‑Datei im `aufgaben/`‑Verzeichnis).
- Claude gibt den **direkten Link** zum neu erstellten Issue aus.


## 2.4 Web-Fetch + lokale Code-Analyse + Aktion ausführen

**Ziel:** Kombiniert „web-fetch“, lokales Dateien-Lesen/Interpretieren und eine daraus abgeleitete Aktion. Ihr erlebt, wie Claude externe Informationen holt, gegen den lokalen Code spiegelt und anschließend selbständig handelt.

**Aufgabe:**
- Leert den Context mit `/clear`.
- Fragt Claude z. B.:

```text
Lies bitte das GitHub-Issue https://github.com/mariodavid/mav-guard/issues/16 (Implement comprehensive logging based on logging guidelines) und fasse die Anforderung kurz zusammen.  
Prüfe anschließend, ob die beschriebene Funktionalität bereits implementiert ist.  
– Falls ja: Zeige mir die relevanten Dateien/Methoden/Commits und eine kurze Begründung.  
– Falls nein: Lege in meinem Fork ein neues GitHub-Issue an, das die Ticket Beschreibung aus dem Ursprungsticket übernimmt. Verlinke das Ursprungsticket (#16) und füge einen kurzen Kommentar hinzu, der die Referenz erklärt. Antworte mir bitte mit "🤠 Yeeeehaaaa, Sir. I've done my Job! ✅🎉" wenn alles fertig ist...
```

- Falls Claude Rückfragen hat (z.B. `gh`-Login, Repo-Pfad, fehlende Rechte), beantwortet sie kurz oder lasst es die nötigen Informationen selbst ermitteln.

**Erwartetes Ergebnis:**
- Das externe Issue **#16** wurde via **web-fetch** gelesen und **präzise zusammengefasst**.
- Claude hat geprüft, ob die Funktionalität lokal existiert und ermittelt das dies nicht umgesetzt ist.
- Ein **neues Issue** wurde im **Fork (origin)** angelegt, das auf **#16** verlinkt; ein Kommentar mit der **Referenz** ist enthalten.
- Der **direkte Link** zum neu erstellten Issue wurde ausgegeben und viel wichtiger: "🤠 Yeeeehaaaa, Sir. I've done my Job! ✅🎉" kam als Antwort.
