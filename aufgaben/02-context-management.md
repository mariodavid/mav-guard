# 02 – Context Management

In dieser Übung geht es darum, die praktischen Verwaltung des Contexts zu beobachten

## 2.1 CLAUDE.md mit /init neu generieren lassen

**Ziel:** Verstehe, wie `/init` eine CLAUDE.md automatisch generiert und vergleiche den Inhalt mit einer manuell
erstellten Version.

**Aufgabe:**

- Sichere die existierende CLAUDE.md: `mv CLAUDE.md CLAUDE.md.bak`
- Führe `/init` aus und lass Claude eine neue CLAUDE.md generieren
- Lies die neu generierte CLAUDE.md durch
- Vergleiche mit der alten Version.

- Optional: Entscheide, welche Version du behalten möchtest oder kombiniere beide

**Erwartetes Ergebnis:** Du verstehst, wie `/init` automatisch eine projektspezifische CLAUDE.md erstellt und siehst die
Unterschiede zwischen manuell gepflegter und auto-generierter Dokumentation.

**Zeitaufwand:** 10 Minuten

---

## 2.2 CLAUDE.md erweitern und Verhaltensänderung beobachten

**Ziel:** Verstehe, wie Änderungen in der `CLAUDE.md` das Verhalten von Claude direkt beeinflussen und wie Claude
projektspezifische Tool-Präferenzen lernt.

**Aufgabe:**

- Öffne die `CLAUDE.md` im Root-Verzeichnis
- Teste was passiert wenn Claude aufgefordert wird auf JIRA Tickets zuzugreifen. Frag z. B.:

```text
Lies mir mal das Jira Ticket https://jira.convista.com/browse/LIN-4591 aus
```

**Erwartetes Ergebnis:** Claude nutzt das WebFetch Tool, bekommt dabei ein Zugriffsproblem und scheitert dann
schlussendlich mit der Aussage das es keinen Zugriff auf JIRA hat.

- Füge folgenden Abschnitt am Ende der CLAUDE.md Datei hinzu:

````markdown
# JiraCLI - Issue Management

IMPORTANT: For Jira URLs (e.g. https://jira.convista.com/browse/LIN-4591) ALWAYS use the CLI, never WebFetch!

## Creating Issues

```bash
# With interactive prompt
jira issue create

# Direct with parameters (no prompt)
jira issue create -tBug -s"Issue Title" -yHigh -b"Description" --no-input

# With template
jira issue create --template /path/to/template.txt
echo "Description" | jira issue create -s"Title" -tTask
```

## Reading Issues

```bash
# As JSON (no interactive UI)
jira issue list --raw

# As CSV
jira issue list --csv

# Own issues
jira issue list -a$(jira me) --raw

# Show issue details
jira issue view ISSUE-1

# Issue details with comments
jira issue view ISSUE-1 --comments 5
```

## Comments

```bash
# Add comment
jira issue comment add ISSUE-1 "My comment"

# With template
echo "Comment text" | jira issue comment add ISSUE-1

# Internal comment
jira issue comment add ISSUE-1 "Internal comment" --internal
```
````

- Beende Claude (`exit`) und starte es neu
- Teste, ob Claude die neue Regel befolgt. Frag z. B.:

```text
Lies mir mal das Jira Ticket https://jira.convista.com/browse/LIN-4591 aus
```

- Beobachte, ob Claude nun das `jira` CLI-Tool verwendet statt WebFetch. Da vermutlich das CLI Werkzeug nicht
  installiert ist, gibt es andere Fehlermeldungen. Entscheidend ist allerdings, dass der korrekte Befehl befolgt wurde.

**Erwartetes Ergebnis:** Du siehst, dass Änderungen in `CLAUDE.md` sofort (nach Neustart) wirksam werden und Claude
Tool-Präferenzen aus der Datei übernimmt.

**Zeitaufwand:** 10 Minuten

---

## 2.3 Hierarchische CLAUDE.md Dateien

**Ziel:** Verstehe, wie Claude mit hierarchischen CLAUDE.md Dateien umgeht und wie modulspezifische Richtlinien die
globalen Projektrichtlinien ergänzen oder überschreiben können.

**Aufgabe:**

- Wechsle in das Verzeichnis `mav-guard-cli`: `cd mav-guard-cli`
- Starte Claude im Modul-Verzeichnis (als eigene Session)
- Führe `/init` aus und lass Claude eine modulspezifische CLAUDE.md generieren
- Bitte Claude, die CLAUDE.md zu erweitern. Sag z. B.:

```text
Schreib in die CLAUDE.md rein, dass in diesem CLI Modul nur Spring Integration Tests geschrieben werden sollen,
keine Unit-Tests. Außerdem sollen Test-Support-Klassen immer in einem sub-package test_support liegen.
```

- Starte Claude neu.
- Frag Claude dann z. B.:

```text
Schau dir das aktuelle Modul an und entscheide ob die Test Struktur den Richtlinien dieses Moduls entspricht.
```

- Beobachte, wie Claude sowohl die Root-CLAUDE.md als auch die modulspezifische CLAUDE.md berücksichtigt
- Optional: Bitte Claude einen Test zu schreiben und beobachte, ob die modulspezifischen Regeln befolgt werden

**Erwartetes Ergebnis:** Du verstehst, dass Claude hierarchische CLAUDE.md Dateien unterstützt, wobei
modulspezifische Anweisungen die globalen Projektanweisungen ergänzen. Dies ermöglicht unterschiedliche Konventionen
für verschiedene Module innerhalb eines Multi-Modul-Projekts.

**Zeitaufwand:** 10 Minuten



