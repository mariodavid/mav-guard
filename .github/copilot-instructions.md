# MavGuard Repository Guidelines

This is a Java-based multi-module Maven project for continuous monitoring and updating of Maven dependencies. It analyzes `pom.xml` files to identify outdated versions, security advisories, and version inconsistencies. Please follow these guidelines when contributing:

## Code Standards

### Required Before Each Commit
- Run `mvn clean verify` before committing to ensure all tests pass
- Ensure all new code follows Java 21 conventions and uses Records where appropriate

### Development Flow
- Build: `mvn clean package`
- Test: `mvn test`
- Run specific module tests: `mvn -pl mav-guard-xml-parser test`
- Run application: `java -jar mav-guard-cli.jar`

## Repository Structure
- `mav-guard-model/`: Core domain model with Java Records (Project, Dependency, PluginDependency)
- `mav-guard-xml-parser/`: XML parsing for POM files using JAXB
- `mav-guard-cli/`: Command-line interface using Spring Shell and PicoCLI
- `mav-guard-nexus/`: Integration with Nexus repository manager (in progress)
- `sample/`: Example projects for testing various scenarios
- `.mvn/`: Maven wrapper configuration
- `target/`: Build output (do not commit)

## Key Guidelines

1. **Architecture Principles**
   - Maintain clean separation between modules
   - Model layer should have no dependencies on other MavGuard modules
   - Use dependency injection via Spring Boot where appropriate
   - Follow layered architecture: Model → Service → Presentation

2. **Java Conventions**
   - Use Java Records for immutable data structures in the model layer
   - Leverage Java 21 features where beneficial
   - Follow standard Java naming conventions
   - Use meaningful variable and method names

3. **Testing Requirements**
   - Write unit tests for all new functionality
   - Use JUnit 5 for testing
   - Mock external dependencies appropriately
   - Test both single-module and multi-module scenarios
   - Include integration tests for CLI commands

4. **CLI Development**
   - Commands should be intuitive and follow the pattern: `analyze` and `check-updates`
   - Provide clear, formatted output for users
   - Support both single and multi-module Maven projects
   - Include helpful error messages with actionable suggestions

5. **Documentation**
   - Update relevant README files when adding features
   - Document public APIs and complex logic
   - Keep CLAUDE.md updated with new development commands
   - Use clear commit messages following conventional commits

## Maven Property Resolution
When working with POM parsing:
- Ensure proper resolution of Maven property placeholders (`${property}`)
- Support inheritance of properties from parent POMs
- Handle multi-module projects with different property values at various levels

## Dependency Management
- Implement Maven's "nearest wins" conflict resolution
- Track which modules use which dependencies in multi-module projects
- Provide clear reporting of version inconsistencies

## Command Examples
```bash
# Analyze a project
java -jar mav-guard-cli.jar analyze pom.xml

# Check for updates
java -jar mav-guard-cli.jar check-updates pom.xml

# Analyze with detailed usage
java -jar mav-guard-cli.jar analyze pom.xml --detailed-usage

# Force multi-module analysis
java -jar mav-guard-cli.jar analyze pom.xml --force-multi-module
```

## Configuration
The application supports different repository configurations:
- Default: Maven Central without authentication
- Nexus: Private repository with authentication
- Configuration via properties files, system properties, or environment variables

## Best Practices
1. Always test with both single-module and multi-module sample projects
2. Verify output formatting is clear and aligned
3. Ensure backward compatibility when modifying commands
4. Performance matters - consider impact when processing large multi-module projects
5. Handle network timeouts gracefully when checking for updates