# Multi-Module Maven Project

This project is an example of a Maven multi-module project for MavGuard.

## Structure

```
multi-module-project/
├── pom.xml                  (Root POM)
├── moduleA/                 (Module A)
│   ├── pom.xml
│   └── src/
├── moduleB/                 (Module B)
│   ├── pom.xml
│   └── src/
└── moduleC/                 (Module C with submodule)
    ├── pom.xml
    ├── src/
    └── submoduleD/          (Submodule D)
        ├── pom.xml
        └── src/
```

## Special Features

- Hierarchical module structure with nested modules
- Different property values at various levels:
  - `moduleB` uses a different Jackson version than the parent POM
  - `moduleC` uses a different JUnit version than the parent POM
  - `submoduleD` uses a different Mockito version than the parent POM
- Dependencies between modules (`moduleB` depends on `moduleA`, etc.)

## Dependency Checks with MavGuard

Here's how to perform dependency checks for a multi-module project:

### Step 1: Build MavGuard (if not already done)

```bash
cd ../../
mvn clean install
cd sample/multi-module-project
```

### Step 2: Execute Multi-Module Analysis

This is the most important command for analyzing multi-module projects:

```bash
# Basic analysis of the multi-module project
java -jar mav-guard-cli.jar xml analyze-multi-module pom.xml
```

The output shows a summary of detected modules and dependencies, as well as possible version inconsistencies.

### Step 3: Detailed Dependency Analysis

For more details about the dependencies in all modules:

```bash
# Detailed dependency analysis with module usage
java -jar mav-guard-cli.jar xml analyze-multi-module pom.xml --detailed-usage
```

### Step 4: Check for Version Inconsistencies

This command returns an error code if version inconsistencies are found:

```bash
# Check for version inconsistencies (returns error code if found)
java -jar mav-guard-cli.jar xml analyze-multi-module pom.xml --check-inconsistencies
```

This sample project intentionally contains version inconsistencies for demonstration:
- Jackson: 2.13.4 (Root) vs. 2.14.0 (moduleB)
- JUnit: 5.8.2 (Root) vs. 5.9.1 (moduleC)
- Mockito: 4.8.0 (Root) vs. 5.2.0 (submoduleD)

### Step 5: Extract Dependencies from a Single Module

If you want to analyze only a single module:

```bash
# Extract dependencies from moduleB
java -jar mav-guard-cli.jar xml extract-dependencies moduleB/pom.xml
```

### Example Output

When executing the `analyze-multi-module` command, you should see output similar to the following:

```
Multi-module project with X modules:
- com.example:multi-module-project:1.0.0-SNAPSHOT
- com.example:moduleA:1.0.0-SNAPSHOT
- com.example:moduleB:1.0.0-SNAPSHOT
- com.example:moduleC:1.0.0-SNAPSHOT
- com.example:submoduleD:1.0.0-SNAPSHOT

WARNING: Found inconsistent dependency versions:
Dependency com.fasterxml.jackson.core:jackson-databind has inconsistent versions:
  - Version 2.13.4 used in modules: multi-module-project, moduleA
  - Version 2.14.0 used in modules: moduleB

...
```