# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MavGuard is a tool for continuous monitoring and updating of Maven dependencies in Java projects. It analyzes `pom.xml` files to identify outdated versions, security advisories, inconsistent version usage, and SNAPSHOT dependencies in production contexts.

## Project Structure

MavGuard is a Maven multi-module project with the following modules:

- **mav-guard-model**: Core domain model for Maven projects (dependencies, projects)
- **mav-guard-xml-parser**: XML parsing for POM files
- **mav-guard-cli**: Command-line interface with Spring Shell
- **mav-guard-nexus**: Integration with Nexus repository manager (in progress)

## Architecture

The project follows a layered architecture:

1. **Model Layer** (mav-guard-model): Contains domain objects representing Maven projects using Java records
2. **Service Layer** (mav-guard-xml-parser): Services for parsing and analyzing Maven POM files using JAXB
3. **Presentation Layer** (mav-guard-cli): CLI interface using Spring Shell and PicoCLI

## Development Commands

### Building the Application

```bash
# From the project root directory
mvn clean package
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for a specific module
mvn -pl mav-guard-xml-parser test

# Run a specific test class
mvn -pl mav-guard-xml-parser test -Dtest=PomParserTest
```

### Running the Application

```bash
# Run the CLI application
java -jar mav-guard-cli.jar

# Show available commands
java -jar mav-guard-cli.jar --help

# Parse a POM file
java -jar mav-guard-cli.jar xml parse-pom path/to/pom.xml

# Extract dependencies from a POM file
java -jar mav-guard-cli.jar xml extract-dependencies path/to/pom.xml
```

## Key Technical Components

- **Java Records**: Domain model uses Java Records for immutable data structures
- **JAXB**: XML binding for POM files using annotations in XML model classes
- **Spring Boot**: Application framework
- **PicoCLI**: Command-line argument parsing
- **Maven**: Build system and project model