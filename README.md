# 🛡️ MavGuard – Your Maven Dependency Watchdog

**MavGuard** is a command-line tool for analyzing and monitoring Maven dependencies in Java projects.  
It examines `pom.xml` files in your repository and provides:

- Comprehensive dependency analysis for single and multi-module projects
- Detection of available updates for dependencies and parent POMs
- Identification of version inconsistencies across modules
- Clear, tabular output showing current vs. available versions

## ✨ Features

- **Dependency Analysis**: Comprehensive analysis of single and multi-module Maven projects
- **Update Checking**: Automatic detection of available updates for dependencies and parent POMs
- **Version Inconsistency Detection**: Identifies when different modules use different versions of the same dependency
- **Multi-Module Support**: Auto-detection and analysis of Maven multi-module projects with consolidated dependency views
- **Parent POM Updates**: Checks for updates to parent POMs in your project hierarchy
- **Clear Output**: Well-formatted, tabular output for easy reading
- **Detailed Usage Reports**: Optional `--detailed-usage` flag shows which modules use which dependencies
- **Flexible Project Detection**: Automatic detection with optional `--force-multi-module` override

## 🧑‍💻 Use Cases

- **Dependency Auditing**: Quickly analyze all dependencies in your Maven projects
- **Version Management**: Identify outdated dependencies and available updates
- **Multi-Module Consistency**: Ensure consistent dependency versions across modules
- **CI/CD Integration**: Automate dependency checks in your build pipeline
- **Security**: Stay informed about updates that might include security fixes

## 🏗️ Project Structure

MavGuard is built as a Maven multi-module project with a clean, modular architecture:

```
mav-guard/
├── mav-guard-model/          # Core domain model
├── mav-guard-xml-parser/     # POM file parsing
├── mav-guard-cli/            # Command-line interface
├── mav-guard-nexus/          # Nexus repository integration
└── sample/                   # Example projects for testing
    ├── simple-project/
    └── multi-module-project/
```

### Module Overview

| Module | Description | Key Technologies |
| **mav-guard** | Parent project with common configuration | Maven |  
| **[mav-guard-model](mav-guard-model/README.md)** | Core domain model (Java Records) | Java 21, Records |
| **[mav-guard-xml-parser](mav-guard-xml-parser/README.md)** | XML parsing for POM files | JAXB, Spring Context |
| **[mav-guard-cli](mav-guard-cli/README.md)** | Command-line interface | Spring Boot, PicoCLI |
| **mav-guard-nexus** | Nexus integration (in progress) | Spring WebClient |

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

### Key Technologies

- **Java 21**: Leveraging modern Java features including Records
- **Spring Boot 3.4**: For dependency injection and application framework
- **PicoCLI**: For powerful command-line argument parsing
- **JAXB**: For XML binding and POM file parsing
- **Maven**: As both build tool and the target domain

## 🔧 Prerequisites

- Java 21 or higher
- Maven 3.8.0 or higher

## 🚀 Building and Running

### Building the Application

To build the application as an executable JAR:

```bash
# From the project root directory
mvn clean package
```

This will compile the code, run the tests, and package the application into an executable JAR file located in the `mav-guard-cli/target` directory.

### Running the Application

After building, you can run the application using:

```bash
java -jar mav-guard-cli.jar
```

#### 🎯 Available Commands

MavGuard provides two main commands for analyzing your Maven projects:

##### 1. **`analyze`** - Project Analysis
Provides a comprehensive overview of your Maven project structure and dependencies.

```bash
# Analyze a single module project
java -jar mav-guard-cli.jar analyze pom.xml

# Analyze a multi-module project
java -jar mav-guard-cli.jar analyze /path/to/root/pom.xml

# Show detailed dependency usage by module
java -jar mav-guard-cli.jar analyze pom.xml --detailed-usage
```

**Features:**
- Displays project coordinates and parent information
- Lists all dependencies (consolidated for multi-module projects)
- Detects and warns about version inconsistencies across modules
- Auto-detects single vs. multi-module projects

##### 2. **`check-updates`** - Update Detection
Performs all analysis features plus checks for available updates.

```bash
# Check for updates in current project
java -jar mav-guard-cli.jar check-updates pom.xml

# Force multi-module analysis
java -jar mav-guard-cli.jar check-updates pom.xml --force-multi-module
```

**Features:**
- All features from `analyze` command
- Checks for newer versions of all dependencies
- Checks for parent POM updates
- Shows current vs. latest versions in aligned columns
- For multi-module projects, shows which modules are affected by each update

##### Example Output

```
--- Update Check Results ---

Consolidated Dependency Updates Available:
  DEPENDENCY                        CURRENT         LATEST          AFFECTED MODULES
  --------------------------------------------------------------------------------
  org.springframework:spring-core   5.3.10      ->  5.3.25         (Modules: module-a, module-b)
  org.slf4j:slf4j-api              1.7.30      ->  1.7.36         (Modules: module-a)

Parent Project Updates:
  MODULE               PARENT                                    CURRENT      LATEST
  --------------------------------------------------------------------------------
  root                 org.springframework.boot:spring-boot-...  2.7.0    ->  2.7.8
```

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run tests for a specific module:
```bash
mvn -pl mav-guard-cli test
```

## 📦 Repository Configuration

MavGuard supports different repository types for dependency update checks:

### Maven Central (Default)
Uses Maven Central repository without authentication:

```properties
# Default configuration in application.properties
mavguard.repository.type=MAVEN_CENTRAL
mavguard.repository.base-url=https://repo1.maven.org/maven2
mavguard.repository.connection-timeout=5000
mavguard.repository.read-timeout=10000
```

### Private Nexus Repository
For enterprise environments with private Nexus repositories:

```properties
# Nexus configuration
mavguard.repository.type=NEXUS
mavguard.repository.base-url=https://nexus.your-company.com
mavguard.repository.username=your-username
mavguard.repository.password=your-password
mavguard.repository.repository=maven-public
mavguard.repository.connection-timeout=5000
mavguard.repository.read-timeout=10000
```

### Configuration Methods

**Option 1: External Properties File**
Create `application-secrets.properties` in the classpath:
```bash
# Create configuration file
cp mav-guard-cli/src/main/resources/application-secrets.properties.sample application-secrets.properties
# Edit with your Nexus credentials
```

**Option 2: System Properties**
```bash
java -Dmavguard.repository.type=NEXUS \
     -Dmavguard.repository.base-url=https://nexus.company.com \
     -Dmavguard.repository.username=user \
     -Dmavguard.repository.password=pass \
     -Dmavguard.repository.repository=maven-public \
     -jar mav-guard-cli.jar check-updates pom.xml
```

**Option 3: Environment Variables**
```bash
export MAVGUARD_REPOSITORY_TYPE=NEXUS
export MAVGUARD_REPOSITORY_BASE_URL=https://nexus.company.com
export MAVGUARD_REPOSITORY_USERNAME=user
export MAVGUARD_REPOSITORY_PASSWORD=pass
export MAVGUARD_REPOSITORY_REPOSITORY=maven-public

java -jar mav-guard-cli.jar check-updates pom.xml
```

## 🎮 Try It Out

### Quick Start with Sample Projects

The repository includes sample projects for testing:

```bash
# Build the application
mvn clean package

# Try the simple project (single module)
java -jar mav-guard-cli.jar analyze sample/simple-project/pom.xml

# Try the multi-module project
java -jar mav-guard-cli.jar analyze sample/multi-module-project/pom.xml --detailed-usage

# Check for updates using Maven Central
java -jar mav-guard-cli.jar check-updates sample/simple-project/pom.xml
```

### Test with Your Own Project

```bash
# Analyze your project
java -jar mav-guard-cli.jar analyze /path/to/your/project/pom.xml

# Check for updates (uses Maven Central by default)
java -jar mav-guard-cli.jar check-updates /path/to/your/project/pom.xml
```

### Expected Output

**For `analyze` command:**
- Project information (coordinates, parent)
- Complete dependency list
- Version inconsistency warnings (for multi-module projects)
- Module breakdown (with `--detailed-usage`)

**For `check-updates` command:**
- All analysis information above
- Update availability table showing current vs. latest versions
- Parent POM update information
- Summary with total number of potential updates

**Note:** The first run may take a moment as it fetches metadata from the configured repository.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
