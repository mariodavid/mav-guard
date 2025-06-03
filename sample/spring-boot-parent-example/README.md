# Spring Boot Parent Example

Dieses Beispielprojekt demonstriert die Verwendung einer Spring Boot Parent POM und wie man mit MavGuard überprüfen kann, ob Updates für die Parent-Version verfügbar sind.

## Projekt-Details

- **Parent-POM**: org.springframework.boot:spring-boot-starter-parent:2.7.0
- **Java-Version**: 17
- **Abhängigkeiten**:
  - spring-boot-starter-web
  - spring-boot-starter-test (Test-Scope)

## Überprüfung von Parent-Updates mit MavGuard

### Voraussetzung

MavGuard muss gebaut und installiert sein. Führe im Hauptverzeichnis des MavGuard-Projekts aus:

```bash
mvn clean package
```

### Ausführung

Du kannst die Parent-Updates mit dem folgenden Befehl überprüfen:

```bash
# Im Verzeichnis des MavGuard-Hauptprojekts
java -jar mav-guard-cli.jar dependencies check-updates sample/spring-boot-parent-example/pom.xml
```

### Erwartete Ausgabe

Die Ausgabe sollte in etwa so aussehen:

```
Checking for updates for dependencies in com.example:spring-boot-parent-example:0.0.1-SNAPSHOT
-----------------------------------------------------
org.springframework.boot:spring-boot-starter-web       (managed) ->      X.Y.Z
org.springframework.boot:spring-boot-starter-test      (managed) ->      X.Y.Z

Checking for parent updates:
-----------------------------------------------------
Parent: org.springframework.boot:spring-boot-starter-parent      2.7.0 ->      3.X.Y
```

Wobei X.Y.Z die neueste verfügbare Version zum Zeitpunkt der Ausführung ist.

## Manuelles Testen von verschiedenen Parent-Versionen

1. **Wechsle zu einer älteren Version**:
   Ändere die Parent-Version in der pom.xml zu einer älteren Version, z.B. `2.5.0`:

   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>2.5.0</version>
       <relativePath/>
   </parent>
   ```

2. **Wechsle zu einer neueren Version**:
   Ändere die Parent-Version in der pom.xml auf eine neuere Version, z.B. `3.1.0`:

   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.1.0</version>
       <relativePath/>
   </parent>
   ```

3. **Führe MavGuard aus und beobachte die Unterschiede in der Ausgabe**.

## Multi-Modul-Projekt mit Parent-Überprüfung

Um Parent-Updates in einem Multi-Modul-Projekt zu überprüfen, verwende den `--multi-module` Parameter:

```bash
java -jar mav-guard-cli.jar dependencies check-updates sample/multi-module-project/pom.xml --multi-module
```

Bei Multi-Modul-Projekten zeigt MavGuard Parent-Updates für jedes Modul an, das eine andere Parent-Definition als das Hauptprojekt hat.