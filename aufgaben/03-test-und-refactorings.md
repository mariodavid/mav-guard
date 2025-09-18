# 03 – Code schreiben mit Claude Code

In diesem Modul erlebst du Claude Code bei autonomer Code-Generierung und komplexen Refactorings. Du lernst Test-Feedback-Schleifen kennen, erforschst den Plan Mode für strukturierte Refactorings und übst dich als "Babysitter" bei längerlaufenden autonomen Tasks. Das Modul zeigt dir, wann Claude minutenlang selbständig arbeitet und wo du als Supervisor eingreifen musst.

Nach diesem Modul wisst ihr:
- wie Test-Feedback-Schleifen funktionieren: Claude generiert Tests, führt sie aus, interpretiert Fehler und korrigiert selbständig,
- wann und wie ihr den Plan Mode für komplexe Refactorings nutzt: Struktur planen, reviewen, dann erst implementieren,
- was Baby-Agent-Supervision bedeutet: Claude bei größeren Tasks überwachen und verifizieren, dass es wirklich alles gemacht hat,
- wie autonome länger laufende Tasks ablaufen: Claude arbeitet mehrere Minuten selbständig an komplexen Aufgaben wie Guideline-Downloads und großflächigen Refactorings.

---

## 3.1 Unit Test für DependencyConflictResolver.java erstellen

**Ziel:** Erlebe, wie Claude aus einem sehr einfachen Prompt eigenständig einen Unit Test für eine bestehende Klasse generiert.

**Aufgabe:**
- Leert den Context mit `/clear`.
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
- Ein Bug-Issue wurde automatisch auf GitHub angelegt, das die Situation beschreibt und bereits erste Ideen zur Lösung enthält.

## 3.3 Testdaten-Erzeugung nach Guideline refactoren

In Aufgabe 3.1. und 3.2 hat Claude vermutlich Testdaten auf die trivialste Weise in den Tests erzeugt. Das wollen wir nun verbessern.

**Ziel:** Wir möchten die Testdaten für Objekte (Projekt & Dependency) nicht mehr mit `new` im Test selbst erzeugen, sondern ein spezielles Muster für Testdaten-Erzeugung nutzen (Provisioning Classes + Builder). 

**Aufgabe:**
- Leert den Context mit `/clear`.
- Bittet Claude die dafür vorbereitete Guideline hier herunterzuladen:
  https://gist.githubusercontent.com/mariodavid/10c7ceefa385dbc185fc90f20fa9b37b/raw/bcee6e92f9c968251d2306533b3d1a057e49ac68/test-data-setup-guideline.md und als Markdown-Datei im Verzeichnis `guidelines/` (z. B. `guidelines/testdata-pattern.md`) abzulegen.
- Überprüft, ob der Inhalt **vollständig** heruntergeladen wurde (vermutlich nicht, wenn Claude sein eigenes Web-Fetch Tool verwendet, den Inhalt interpretiert und dann eine Ausgabe Datei schreibt).
- Falls der Inhalt unvollständig ist oder zusammengefasst aussieht: schreibt eine Follow-up-Message an Claude mit der Bitte, `curl` oder `wget` zu benutzen, um die Datei vollständig herunterzuladen.
- Nachdem die Guideline heruntergeladen wurde, bittet Claude, die bestehenden Tests aus 3.1 und 3.2 entsprechend der Guideline umzubauen (Testdaten-Erzeugung zentralisieren und Builders/Provisioning Classes nutzen) und wenn es schon dabei ist: das Muster `// Given`, `// When`, `// Then` durch `// arrange` `//act` und `//assert` zu ersetzen.

- Fragt Claude z. B.:

```text
Baue mal den DependencyConflictResolverTest so um dass er zu @guidelines/test-data-setup-guideline.md passt. Wenn du Provisioning Klassen anlegen musst, dann denke immer an Nike: "Just do it!". 

Dieses // Given // When // Then ist nicht so mein Style. Bitte umstellen auf // arrange // act // assert.
```

- Dieses Refactoring ist relativ gross (Claude wird vermutlich mehrere minuten autonom argieren), da mehrere Dateien anlegen muss und dann in einem Schritt die Tests umbauen muss. Daher ist es hier **wichtig** die Ergebnisse des Refactorings zu überprüfen. Es kann hier passieren, dass Claude die Tests evtl. nicht mehr konsistent am Ende ausführt, nicht alle Tests umgebaut hat oder nicht konsequent das "Delta Principle" aus der Guideline in allen Tests umgesetzt hat. Stattdessen einfach behauptet es sei alles fertig. 

**Erwartetes Ergebnis:**
- Die Guideline ist als vollständige Markdown-Datei im Verzeichnis `guidelines/` vorhanden.
- Der neue Test wurde umgebaut und neue Provisioning Klassen angelegt.
- Claude hat den Test am Ende des Refactorings ausgeführt und sichergestellt das alles weiterhin funktioniert.
- Du hast den Test reviewed und bist sicher das Claude dich nicht gefooled hat

**Learnings**:
- Claude will zwar später mal ein echter Agent werden, aber momentan eher ein Baby Agent mit Windeln an - und du bist der Babysitter. Immer daran denken: besonders bei grossen Aufgaben sagt Claude am Ende häufig es sei fertig. Aber ob das stimmt, kannst nur du beurteilen... - [You are absolutely right...](https://github.com/anthropics/claude-code/issues/3382).

## 3.4 Test weiter refactoren: JUnit 5 @Nested

**Ziel:** Den bestehenden `DependencyConflictResolverTest` in logisch gruppierte **JUnit 5 `@Nested`-Klassen** umbauen, um Lesbarkeit, Navigierbarkeit und Verantwortlichkeiten der Tests zu verbessern – im gleichen Arbeitsmodus wie 3.2/3.3 (kleiner Scope, klare Schritte, Plan zuerst).

**Aufgabe:**
- Leert den Context mit `/clear`.
- **Schaltet den Planning Mode ein** (SHIFT+TAB → „⏸ plan mode on“).
- Fragt Claude z.B.:

```text
Bau den DependencyConflictResolverTest mal auf JUnit 5 Nested Klassen um. Ich hätte gerne eine sinnvolle Gruppierung...
```

- Lest den High‑Level‑Plan und schlagt ggf. 1–2 Verbesserungen vor (z.B. Gruppierung nach „Single Project“, „Dependency Management“, „Parent‑Child Hierarchie“, „Multi‑Module“, „Zirkuläre Referenzen“; konsistente Namenskonventionen; gemeinsame `@BeforeEach` je Gruppe; Wiederverwendung der Provisioning‑Helfer aus 3.3).
- Approvt anschließend den Plan und lasst Claude die Umsetzung starten.
- Stellt sicher, dass `// arrange`, `// act`, `// assert` als Trennkommentare beibehalten bzw. ergänzt werden.
- Fordert Claude auf, am Ende alle Tests auszuführen und bei Bedarf die Struktur nachzuziehen.

**Erwartetes Ergebnis:**
- Die Testklasse ist in klar benannte `@Nested`‑Klassen strukturiert (z.B. `SingleProject`, `DependencyManagement`, `ParentChild`, `CircularReferences`).
- Gemeinsame Setups pro Gruppe liegen in `@BeforeEach` innerhalb der jeweiligen `@Nested`‑Klasse; übergreifende Helfer bleiben oben.

**Learnings:**
- [Planning Mode](https://claudelog.com/mechanics/plan-mode/) hat drei entscheidende Vorteile: 
    1. du siehst den High-Level Plan upfront und bist daher in der läge viel einfacher zu verstehen was gleich passieren wird. 
    2. Den Plan mit deinen eigenen Vorstellungen abzugleichen ist viel einfacher & schneller als aus dem fertig generierten Quelltext im Kopf reverse-zu-engineeren was "the bigger picture" war. 
    3. Wenn dir der Plan nicht gefällt, kannst du vor der Implementierung eingreifen. Nach der Impl. Einzugreifen geht auch, aber dann muss Claude den konzeptuellen Umbau auf dem fertigen Erzeugnis machen - viel schlechter, langsamer & teurer.
- Wenn du dich im Planning Modus befindest, kannst du den Prompt trotzdem so formulieren als würdest du direkt die Implementierung haben wollen. In der Regel wird Claude dann dennoch einen echten Plan erstellen und nicht einfach die komplette Implementierung in einer Vorschau präsentieren.