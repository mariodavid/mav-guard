# MavGuard Sample Projects

This directory contains sample projects demonstrating MavGuard's functionality.

## Available Examples

### Simple-Project

A simple Maven project with a single POM file. This project is well-suited for:
- Testing basic dependency extraction
- Demonstrating property resolution

### Multi-Module-Project

A more comprehensive Maven project with multiple nested modules. This project shows:
- How MavGuard handles multi-module projects
- Different dependency versions in different modules
- Nested modules (up to two levels)
- Dependencies between modules

### Spring-Boot-Parent-Example

A sample project with Spring Boot as the parent POM, specifically designed for testing parent version checking:
- Demonstrates checking for available updates to the parent POM
- Shows how MavGuard identifies older Spring Boot versions
- Contains detailed README with testing instructions

## Usage

To analyze the examples with MavGuard, first navigate to the appropriate project directory. Then you can execute the MavGuard commands as described in the respective README files.