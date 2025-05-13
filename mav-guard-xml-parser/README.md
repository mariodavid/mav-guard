# MavGuard XML Parser Module

## Overview
The XML Parser module is responsible for parsing and processing Maven POM files. It provides a clean, abstracted interface for converting XML-based POM files into structured Java objects that can be easily analyzed and manipulated by the application. With the new multi-module support, it can now process complex project structures with nested modules and correctly resolve the Maven inheritance hierarchy.

## Core Concepts

### XML Parsing
The module uses JAXB (Java Architecture for XML Binding) to handle the conversion between XML and Java objects:

- **XmlParser**: A generic utility class for parsing any XML file into a specified Java class
- **PomParser**: A specialized parser that converts Maven POM files into Project objects
- **PomFileProcessor**: An interface defining the contract for POM file processing operations
- **DependencyConflictResolver**: Resolves dependency conflicts according to Maven's rules
- **MultiModuleDependencyCollector**: Consolidates dependencies across modules

### Key Features
- Robust error handling for malformed XML files
- Support for both file-based and stream-based parsing
- Validation of input files and streams
- Multi-module project processing with parent-child relationship resolution
- Maven property resolution across module boundaries
- Implementation of Maven's "nearest wins" conflict resolution
- Clean separation between generic XML parsing and Maven-specific parsing logic

### Design Principles
The module follows several important design principles:

- **Single Responsibility**: Each class has a clear, focused purpose
- **Interface Segregation**: The PomFileProcessor interface defines a clear contract
- **Dependency Inversion**: High-level modules depend on abstractions, not concrete implementations
- **Error Handling**: Comprehensive exception handling with meaningful error messages

## Multi-Module Support

The XML Parser now fully supports Maven multi-module projects:

### Module Structure Processing
- **Recursive Module Detection**: Automatically identifies and processes all modules in a project
- **Hierarchy Resolution**: Establishes parent-child relationships between modules
- **Module Path Resolution**: Handles relative paths between modules correctly

### Dependency Management
- **Property Resolution**: Resolves Maven property placeholders (`${property}`) with inheritance support
- **Version Inheritance**: Supports inheritance of dependency versions from parent POMs
- **Conflict Resolution**: Implements Maven's version conflict resolution rules
- **Effective Version Calculation**: Determines the effective version of dependencies accounting for inheritance

### Analysis Features
- **Version Consistency Checks**: Identifies inconsistent versions of the same dependency across modules 
- **Dependency Usage Tracking**: Reports which modules use which dependencies
- **Consolidated Reporting**: Provides comprehensive dependency information across all modules

## Relationship to Other Modules

The XML Parser module:

- **Depends on**: The Model module for the domain objects that represent Maven projects
- **Is used by**: The CLI module to provide XML parsing functionality to end users

This module serves as a bridge between the raw XML data in POM files and the structured object model used throughout the application. It encapsulates all XML parsing logic, allowing other modules to work with clean Java objects without worrying about the underlying XML structure.

## Usage Example

```java
// Initialize the parser
PomParser parser = new PomParser();

// Parse a single POM file
File pomFile = new File("/path/to/pom.xml");
Project project = parser.parsePomFile(pomFile);

// Parse a multi-module project
File rootPomFile = new File("/path/to/root-pom.xml");
List<Project> projects = parser.parseMultiModuleProject(rootPomFile);

// Analyze dependencies across modules
MultiModuleDependencyCollector collector = new MultiModuleDependencyCollector();
MultiModuleDependencyCollector.DependencyReport report = collector.collectDependencies(projects);

// Check for version inconsistencies
if (report.hasVersionInconsistencies()) {
    // Handle inconsistencies
    System.out.println(report.getSummary());
}
```