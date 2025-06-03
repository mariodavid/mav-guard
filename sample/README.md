# MavGuard Sample Projects

This directory contains sample projects to demonstrate MavGuard's functionality.

## Available Examples

### Simple-Project

A simple Maven project with a single POM file. This project is suitable for:
- Testing basic dependency extraction
- Demonstrating property resolution

### Multi-Module-Project

A more comprehensive Maven project with multiple nested modules. This project shows:
- How MavGuard handles multi-module projects
- Different dependency versions across modules
- Nested modules (up to two levels)
- Dependencies between modules

### Spring-Boot-Parent-Example

A sample project with Spring Boot as parent POM, specifically designed for testing parent version checks:
- Demonstrates checking for available parent POM updates
- Shows how MavGuard identifies older Spring Boot versions
- Includes detailed README with test instructions

## Usage

To analyze the examples with MavGuard, first navigate to the corresponding project directory. Then you can run the MavGuard commands as described in the respective README files.