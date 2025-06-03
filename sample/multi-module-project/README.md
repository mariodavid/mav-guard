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

## Features

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
mvn clean package
cd sample/multi-module-project
```

### Step 2: Run Multi-Module Analysis

This is the main command for analyzing multi-module projects:

```bash
# Basic analysis of the multi-module project
java -jar mav-guard-cli.jar analyze pom.xml
```

The output shows a summary of detected modules and dependencies, as well as possible version inconsistencies.

### Step 3: Detailed Dependency Analysis

For more details about dependencies across all modules:

```bash
# Detailed dependency analysis with module usage
java -jar mav-guard-cli.jar analyze pom.xml --detailed-usage
```

### Step 4: Check for Updates

Check for available updates across all modules:

```bash
# Check for updates in the multi-module project
java -jar mav-guard-cli.jar check-updates pom.xml
```

This sample project intentionally contains version inconsistencies for demonstration:
- Jackson: 2.13.4 (Root) vs. 2.14.0 (moduleB)
- JUnit: 5.8.2 (Root) vs. 5.9.1 (moduleC)
- Mockito: 4.8.0 (Root) vs. 5.2.0 (submoduleD)

### Step 5: Extract Dependencies from a Single Module

If you only want to analyze a single module:

```bash
# Analyze moduleB separately
java -jar mav-guard-cli.jar analyze moduleB/pom.xml
```

### Example Output

When running the `analyze` command, you should see output similar to:

```
Multi-module project with 5 modules:
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