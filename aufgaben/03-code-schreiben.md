# 03 – Code schreiben mit Claude Code

In diesem Modul lernst du, wie du Claude Code gezielt zum **Erstellen von Code und Tests** einsetzt. Wir beginnen mit einfachen, klar formulierten Prompts und bauen so ein Gefühl dafür auf, wie Claude Code plant, implementiert, Tests generiert, ausführt und anhand der Ergebnisse verbessert. Dabei geht es nicht nur um Unit-Tests, sondern auch darum, neue Features und Funktionen umzusetzen.

Ziel dieses Kapitels ist, dass du:
- verstehst, wie Claude Code für dich neue Funktionen und Tests erstellt und erweitert,
- lernst, mit sehr einfachen Prompts zu beginnen und die Ergebnisse dann gemeinsam zu verfeinern,
- siehst, wie man sowohl Tests als auch neuen Quellcode generieren lässt,
- Tests ausführen lässt, Ergebnisse interpretiert und daraus Folgeaktionen ableitet.

---

## 3.1 Unit Test für DependencyConflictResolver.java erstellen

**Ziel:** Erlebe, wie Claude aus einem sehr einfachen Prompt eigenständig einen Unit Test für eine bestehende Klasse generiert.

**Aufgabe:**
- Fragt Claude z. B.:

```text
Erstell mal einen Unit Test für DependencyConflictResolver.java
```

- Ihr solltet beobachten können das Claude nachdem es den Test erstellt hat versucht auf der Kommandozeile `mvn` Befehle auszuführen wie bspw. `Bash(mvn -pl mav-guard-xml-parser test -Dtest=DependencyConflictResolverTest)`. Sofern dies nicht passiert, fordert Claude auf den Test auszuführen: "Du musst den Test auch ausprobieren, nachdem du ihn geschrieben hast...". 
- Wenn bestimmte Tests fehlschlagen, sollte Claude selbständig die Fehler in der Testausgabe auslesen und basierend darauf Änderungen an dem Test vornehmen und daraufhin die Tests erneut ausführen.
- Fragt Claude ob es auf die Äquivalenzklassen eingehen kann und erklären kann welche nun gecovered sind und welche ggf. noch fehlen: "was ist mit den Äquivalenzklassen? Welche gibt es da und welche hast du gecovered?"

**Erwartetes Ergebnis:**
- Ein neuer Test (z. B. `DependencyConflictResolverTest`) wird erstellt.
- Der Test deckt mindestens die Kernlogik der Klasse ab.
- Claude hat (ggf. durch Aufforderung) den Test ausgefügt und ist in eine Feedbackschleife gelangt
- Claude erklärt kurz, was getestet wird und welche Äquivalenzklassen abgedeckt sind.

---

## 3.2 Follow‑up Test: Zirkuläre Parent‑Referenzen (A → B → A)

**Ziel:** Aufbauend auf 3.1 ergänzen wir eine **fehlende Äquivalenzklasse**: zirkuläre Parent‑Beziehungen in der Projekt‑Hierarchie. Ziel ist es, das aktuelle Verhalten zu dokumentieren und einen Test hinzuzufügen, der Zyklen (A → B → A) abbildet.

**Aufgabe:**
- Fragt Claude z. B.:

```text
Erstelle einen zusätzlichen Unit Test für DependencyConflictResolver.java, der eine zirkuläre Parent‑Referenz (A → B → A) modelliert.  
Der Test soll robust sein (kein StackOverflow/keine Endlosschleife), das aktuelle Verhalten dokumentieren und klar benennen, was erwartet wird.  
Erkläre mir anschließend kurz, wie der Resolver aktuell mit Zyklen umgeht und ob ein Schutz nötig wäre.
```

- Beim Ausführen des ersten Tests ein StackOverflowError oder eine Endlosschleife auftritt, bitte Claude den Test so umzuschreiben, dass er das aktuelle kaputte Verhalten dokumentiert, z.B. mittels `assertThrows(StackOverflowError.class, …)` und mit einem aussagekräftigen Kommentar im Test.
- Stelle anschließend eine Nachfrage an Claude, einen zweiten Test zu ergänzen, der den korrekten Zielzustand beschreibt (z.B. "Cycle wird erkannt, Verarbeitung bricht ab, kein StackOverflow"). Lass Claude den Test ausführen, und verifiziere dann manuell, dass der Test wirklich fehlschlägt (wie erwartet). Bitte Claude danach den Test zu disablen und ein Bug als GH Issue anzulegen, der die Situation beschreibt. In der Beschreibung soll Claude bereits ein paar Ideen aufschreiben, wie man dieses Problem lösen kann.

**Erwartetes Ergebnis:**
- Eine neue Testmethode (z. B. `handlesCircularParentReferences()`), die die Äquivalenzklasse Zirkularität abdeckt.
- Der Test dokumentiert das Ist‑Verhalten (inkl. kurzer Begründung im Kommentar) und läuft deterministisch (keine Endlosschleifen).
- Optionaler Hinweis von Claude, wie eine Cycle‑Detection im Code aussehen könnte (Visited‑Set, Schutz gegen Wiederbesuch).
- Zwei komplementäre Tests: (1) dokumentiert das aktuelle Fehlverhalten (grün), (2) dokumentiert den Zielzustand und ist mit `@Disabled` markiert, nachdem der Fehlschlag bestätigt wurde.

