# Spring Boot Parent Example

This sample project demonstrates the use of a Spring Boot parent POM and how to check for available updates to the parent version using MavGuard.

## Project Details

- **Parent POM**: org.springframework.boot:spring-boot-starter-parent:2.7.0
- **Java Version**: 17
- **Dependencies**:
  - spring-boot-starter-web
  - spring-boot-starter-test (test scope)

## Checking Parent Updates with MavGuard

### Prerequisite

MavGuard must be built and installed. Run in the main directory of the MavGuard project:

```bash
mvn clean package
```

### Execution

You can check for parent updates with the following command:

```bash
# In the MavGuard main project directory
java -jar mav-guard-cli.jar check-updates sample/spring-boot-parent-example/pom.xml
```

### Expected Output

The output should look something like:

```
Checking for updates for dependencies in com.example:spring-boot-parent-example:0.0.1-SNAPSHOT
-----------------------------------------------------
org.springframework.boot:spring-boot-starter-web       (managed) ->      X.Y.Z
org.springframework.boot:spring-boot-starter-test      (managed) ->      X.Y.Z

Checking for parent updates:
-----------------------------------------------------
Parent: org.springframework.boot:spring-boot-starter-parent      2.7.0 ->      3.X.Y
```

Where X.Y.Z is the latest available version at the time of execution.

## Manual Testing of Different Parent Versions

1. **Switch to an older version**:
   Change the parent version in pom.xml to an older version, e.g. `2.5.0`:

   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>2.5.0</version>
       <relativePath/>
   </parent>
   ```

2. **Switch to a newer version**:
   Change the parent version in pom.xml to a newer version, e.g. `3.1.0`:

   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.1.0</version>
       <relativePath/>
   </parent>
   ```

3. **Run MavGuard and observe the differences in output**.

## Multi-Module Project with Parent Check

To check parent updates in a multi-module project:

```bash
java -jar mav-guard-cli.jar check-updates sample/multi-module-project/pom.xml
```

For multi-module projects, MavGuard shows parent updates for each module that has a different parent definition than the main project.