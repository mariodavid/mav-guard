# MavGuard CLI Module

## Overview
The Command Line Interface (CLI) module provides a user-friendly interface for interacting with MavGuard's functionality. Built using **Picocli**, it offers a set of commands that allow users to parse, analyze, and check for updates in Maven POM files directly from the command line. The primary commands are `analyze` and `check-updates`.

## Command Structure
The module is organized around commands that expose the functionality of the underlying MavGuard services:

-   **`analyze <pom-path>`**: This command provides a comprehensive overview of your Maven project. It displays project coordinates, lists dependencies (consolidated for multi-module projects), shows parent POM information, and identifies version inconsistencies in multi-module setups. Use the `--detailed-usage` option for a breakdown of dependency usage by module in multi-module projects.

-   **`check-updates <pom-path>`**: This command performs all actions of the `analyze` command and additionally checks for available updates for project dependencies and parent POMs. It clearly indicates newer versions available.

-   **Multi-Module Support**: Both `analyze` and `check-updates` automatically detect multi-module projects. They process the entire project hierarchy, providing consolidated reports and analysis. The `--force-multi-module` flag can be used to explicitly treat a project as multi-module.

## Picocli Integration
The module leverages Picocli to provide a rich command-line experience:

-   User-friendly help messages (accessible via `--help`).
-   Command parameter and option validation.
-   Consistent command structure and output formatting.
-   Support for both interactive and script-based usage.
-   Generation of native executables (if GraalVM is used, though not covered here).

### Key Features
-   Simple, intuitive command syntax (`analyze`, `check-updates`).
-   Comprehensive error reporting.
-   Human-readable, formatted output, especially for update checks.
-   Support for single and multi-module Maven projects (auto-detected).
-   Detection and reporting of dependency version inconsistencies in multi-module projects.
-   Extensible command structure for future enhancements.

## Relationship to Other Modules

The CLI module:

-   **Depends on**:
    -   The Model module for the domain objects (Project, Dependency, etc.).
    -   The XML Parser module for parsing POM files.
    -   The Nexus module for checking latest versions of dependencies.
-   **Provides**: A user interface layer that makes the functionality of the other modules accessible to end users.

This module serves as the primary entry point for users interacting with the MavGuard system. It orchestrates the other modules to provide a cohesive user experience.

## Usage Examples

### Basic Commands

```bash
# Show available commands and global options
java -jar mav-guard-cli.jar --help

# Display help for the 'analyze' command
java -jar mav-guard-cli.jar analyze --help

# Analyze a single POM file
java -jar mav-guard-cli.jar analyze /path/to/your/pom.xml

# Check for updates in a single POM file
java -jar mav-guard-cli.jar check-updates /path/to/your/pom.xml
```

### Multi-Module Project Commands

```bash
# Analyze a multi-module project (root POM specified)
java -jar mav-guard-cli.jar analyze /path/to/root-pom.xml

# Check for updates in a multi-module project
java -jar mav-guard-cli.jar check-updates /path/to/root-pom.xml

# Get detailed dependency usage per module in a multi-module project
java -jar mav-guard-cli.jar analyze /path/to/root-pom.xml --detailed-usage

# Force a POM to be treated as a multi-module project for analysis
java -jar mav-guard-cli.jar analyze pom.xml --force-multi-module
```

## Multi-Module Support

MavGuard provides robust support for Maven multi-module projects through its `analyze` and `check-updates` commands:

### Hierarchical Processing
-   Automatic detection and recursive processing of all modules defined in a root POM.
-   Full support for Maven inheritance hierarchy (Parent -> Child modules).
-   Processing of nested modules.

### Dependency Resolution & Analysis
-   Support for dependency version inheritance from parent POMs.
-   Property placeholder resolution.
-   Consolidated dependency reports across all modules when using `analyze` or `check-updates`.
-   Detection and clear reporting of version inconsistencies for dependencies used across different modules.
-   Module-specific dependency usage analysis available via `analyze --detailed-usage`.

### Update Checking
-   `check-updates` provides a consolidated list of dependency updates across all modules.
-   It also checks for updates for parent POMs referenced in any module.
-   Affected modules are listed for each consolidated dependency update.

These capabilities allow for comprehensive dependency management and update checking across complex Maven project structures, helping to maintain consistency and identify potential issues.
