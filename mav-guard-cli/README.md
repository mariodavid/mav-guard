# MavGuard CLI Module

## Overview
The Command Line Interface (CLI) module provides a user-friendly interface for interacting with MavGuard's functionality. Built on Spring Shell, it offers a set of commands that allow users to parse, analyze, and extract information from Maven POM files directly from the command line.

## Core Concepts

### Command Structure
The module is organized around a set of shell commands that expose the functionality of the underlying modules:

- **XML Parsing Commands**: Commands for parsing XML files and extracting information
- **Multi-Module Support**: Commands for analyzing Maven multi-module projects
- **Future Command Groups**: As the application grows, additional command groups will be added for dependency analysis, version checking, and security scanning

### Spring Shell Integration
The module leverages Spring Shell to provide a rich command-line experience:

- Interactive shell with command history and tab completion
- Command parameter validation and help documentation
- Consistent command output formatting
- Support for both interactive and script-based usage

### Key Features
- Simple, intuitive command syntax
- Comprehensive error reporting
- Human-readable output formatting
- Support for processing multiple POM files
- Multi-module Maven project processing
- Detection of dependency version conflicts
- Extensible command structure for future enhancements

## Relationship to Other Modules

The CLI module:

- **Depends on**: 
  - The Model module for the domain objects
  - The XML Parser module for parsing functionality
  
- **Provides**: A user interface layer that makes the functionality of the other modules accessible to end users

This module serves as the primary entry point for users interacting with the MavGuard system. It orchestrates the other modules to provide a cohesive user experience, translating user commands into the appropriate operations on the underlying modules and formatting the results for display.

## Usage Examples

### Basic Commands

```bash
# Launch the interactive shell
java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar

# Show available commands
java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar --help

# Parse a POM file and display basic information
java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar xml parse-pom /path/to/pom.xml

# Extract and list all dependencies from a POM file
java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar xml extract-dependencies /path/to/pom.xml
```

### Multi-Module Project Commands

```bash
# Extract dependencies from a multi-module project
java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar xml extract-dependencies /path/to/root-pom.xml --multi-module

# Analyze a multi-module Maven project
java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar xml analyze-multi-module /path/to/root-pom.xml

# Get detailed dependency usage per module
java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar xml analyze-multi-module /path/to/root-pom.xml --detailed-usage

# Check for version inconsistencies with non-zero exit code on detection
java -jar mav-guard-cli/target/mav-guard-cli-0.0.1-SNAPSHOT.jar xml analyze-multi-module /path/to/root-pom.xml --check-inconsistencies
```

## Multi-Module Support

MavGuard now fully supports Maven multi-module projects with the following features:

### Hierarchical Processing
- Automatic detection and recursive processing of all modules
- Full support for Maven inheritance hierarchy (Parent -> Child modules)
- Processing of nested modules (e.g., root → moduleA → submoduleB)

### Dependency Resolution
- Support for dependency version inheritance from parent POMs
- Property placeholder resolution across module boundaries
- Implementation of Maven's "nearest-wins" conflict resolution for versions

### Dependency Analysis
- Detection of version inconsistencies across modules
- Consolidated dependency reports across all modules
- Module-specific dependency usage analysis

### Integration Support
- Exit code support for CI/CD integration
- Detailed conflict reporting for easier troubleshooting

These capabilities allow for comprehensive dependency management across complex Maven project structures, helping to maintain consistency and identify potential issues before they cause runtime problems.