# 🛡️ MavGuard – Your Maven Dependency Watchdog

**MavGuard** is a lightweight tool for continuous monitoring and updating of Maven dependencies in Java projects.  
It regularly checks all `pom.xml` files in your code repository and identifies:

- Outdated versions and possible updates
- Security advisories for known libraries
- Inconsistent version usage across modules
- SNAPSHOT dependencies in production contexts

## ✨ Features

- Integration with CI/CD pipelines (e.g., via GitHub Actions or Jenkins)
- Configurable rules (e.g., no automatic major upgrades)
- Output as JSON, Markdown, or Pull Request comments
- Optional: automatic opening of PRs with upgrade suggestions
- Compatible with OSS scanners like OWASP Dependency-Check or Snyk

## 🧑‍💻 Use Case

Perfect for teams who want to keep dependencies under control  
without manually running `mvn versions:display-dependency-updates` or waiting for Dependabot.

## 🏗️ Project Structure

MavGuard is built as a Maven multi-module project with a clean, modular architecture:

### Module Overview

| Module | Description | Dependencies |
|--------|-------------|--------------|
| **mav-guard** | Parent project with common configuration | None |
| **[mav-guard-model](mav-guard-model/README.md)** | Core domain model for Maven projects | None |
| **[mav-guard-xml-parser](mav-guard-xml-parser/README.md)** | XML parsing for POM files | mav-guard-model |
| **[mav-guard-cli](mav-guard-cli/README.md)** | Command-line interface with Spring Shell | mav-guard-model, mav-guard-xml-parser |

### Architecture

The project follows a layered architecture:

1. **Model Layer** (mav-guard-model): Contains the core domain objects representing Maven projects and dependencies
2. **Service Layer** (mav-guard-xml-parser): Provides services for parsing and analyzing Maven POM files
3. **Presentation Layer** (mav-guard-cli): Offers a user interface for interacting with the system

This modular design allows for:
- Clear separation of concerns
- Independent development and testing of modules
- Flexibility to add new modules (e.g., a future web interface)
- Reuse of core functionality across different interfaces
