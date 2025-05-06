# MavGuard CLI Module

## Overview
The Command Line Interface (CLI) module provides a user-friendly interface for interacting with MavGuard's functionality. Built on Spring Shell, it offers a set of commands that allow users to parse, analyze, and extract information from Maven POM files directly from the command line.

## Core Concepts

### Command Structure
The module is organized around a set of shell commands that expose the functionality of the underlying modules:

- **XML Parsing Commands**: Commands for parsing XML files and extracting information
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
- Extensible command structure for future enhancements

## Relationship to Other Modules

The CLI module:

- **Depends on**: 
  - The Model module for the domain objects
  - The XML Parser module for parsing functionality
  
- **Provides**: A user interface layer that makes the functionality of the other modules accessible to end users

This module serves as the primary entry point for users interacting with the MavGuard system. It orchestrates the other modules to provide a cohesive user experience, translating user commands into the appropriate operations on the underlying modules and formatting the results for display.

## Usage Examples

```bash
# Parse a POM file and display basic information
parse-pom /path/to/pom.xml

# Extract and list all dependencies from a POM file
extract-dependencies /path/to/pom.xml

# Parse a generic XML file (for testing/debugging)
parse-xml /path/to/file.xml
```

These commands demonstrate the basic functionality currently available. As the application evolves, additional commands will be added to support more advanced use cases.