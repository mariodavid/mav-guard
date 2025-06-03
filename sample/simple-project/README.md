# Simple Maven Project

This project is a simple Maven example for MavGuard.

## Structure

- Single module with direct `pom.xml`
- Multiple dependencies with different versions
- Use of Maven properties for versions

## Dependency Checks with MavGuard

Here's how to perform a dependency check with MavGuard:

### Step 1: Make sure MavGuard is built

Navigate to the root directory of the MavGuard project and run the Maven build:

```bash
cd ../../
mvn clean package
```

### Step 2: Basic Analysis

```bash
# Navigate back to simple-project
cd sample/simple-project

# Analyze the project
java -jar mav-guard-cli.jar analyze pom.xml
```

### Step 3: Check dependencies for updates

```bash
# Check if there are newer versions of dependencies
java -jar mav-guard-cli.jar check-updates pom.xml
```

### Example Output

When running the `analyze` command, you should see output similar to:

```
Project: com.example:simple-project:1.0.0
Parent: None

Dependencies found in POM file:
- org.springframework:spring-core:5.3.27
- org.springframework:spring-context:5.3.27
- com.fasterxml.jackson.core:jackson-databind:2.14.2
- org.apache.commons:commons-lang3:3.12.0
- org.junit.jupiter:junit-jupiter:5.9.2 (scope: test)
- org.mockito:mockito-core:5.3.1 (scope: test)
```

With `check-updates` you should see output similar to:

```
Checking for updates for dependencies in com.example:simple-project:1.0.0
-----------------------------------------------------
org.springframework:spring-core              5.3.27 ->   7.0.0-M4
org.springframework:spring-context           5.3.27 ->   7.0.0-M4
com.fasterxml.jackson.core:jackson-databind     2.14.2 ->     2.19.0
org.apache.commons:commons-lang3             3.12.0 -> 3.17.0.redhat-00001
org.junit.jupiter:junit-jupiter               5.9.2 ->  5.13.0-M3
org.mockito:mockito-core                      5.3.1 ->     5.17.0
```