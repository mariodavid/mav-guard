package de.diedavids.mavguard.commands;

import static org.assertj.core.api.Assertions.assertThat;

import de.diedavids.mavguard.MavGuardApplication;
import de.diedavids.mavguard.model.Dependency; // Added this import
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import picocli.CommandLine;

@LocalOnlyTest
@SpringBootTest(classes = MavGuardApplication.class, webEnvironment = WebEnvironment.NONE)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("parent-test")
class DependencyCommandsParentTest {

    @Autowired
    private CheckUpdatesCommand checkUpdatesCommand;

    @Autowired
    private DependencyVersionService dependencyVersionService; // Uses TestParentVersionService

    @Autowired
    private CommandLine.IFactory factory;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // TestParentVersionService is automatically injected and configured
        // No mocking needed - it provides test data directly
    }

    @Test
    void testCheckUpdatesCommandWithParent(CapturedOutput output) throws IOException {
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomWithParentXml());

        int exitCode = new CommandLine(checkUpdatesCommand, factory)
                .execute(pomFile.toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("--- Project Analysis (Single Module): com.example:test-project:1.0.0 ---");
        assertThat(output).contains("Parent: org.springframework.boot:spring-boot-starter-parent:2.7.0");
        assertThat(output).contains("--- Update Check Results ---");
        assertThat(output).contains("Parent Project Update (org.springframework.boot:spring-boot-starter-parent:2.7.0):");
        assertThat(output).containsPattern("org.springframework.boot:spring-boot-starter-parent\\s+2.7.0\\s+-> 2.7.8");
    }

    @Test
    void testCheckUpdatesMultiModuleCommandWithParent(CapturedOutput output) throws IOException {
        createMultiModuleProjectWithParents();
        Path rootPomFile = tempDir.resolve("pom.xml");

        int exitCode = new CommandLine(checkUpdatesCommand, factory)
                .execute(rootPomFile.toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("--- Project Analysis (Multi-Module): com.example:multi-module-parent:1.0.0 ---");
        assertThat(output).contains("Root Parent: org.springframework.boot:spring-boot-starter-parent:2.7.0");
        assertThat(output).contains("--- Update Check Results ---");
        assertThat(output).contains("Parent Project Updates (Per Module):");
        // Check for the root project's parent update (if it's listed per module or once)
        // The current output lists parent updates per module if that module *directly* has an updatable parent.
        // The root project (multi-module-parent) itself has spring-boot-starter-parent.
        // Modules A and B have multi-module-parent as parent, which is not updated.
        // So, only the root's parent update should be prominent for its own context.
        // The CheckUpdatesCommand output for parent updates is tabular.
        // Example: multi-module-parent  org.springframework.boot:spring-boot-starter-parent 2.7.0                -> 2.7.8
        // Regex needs to match this table row structure.
        assertThat(output).containsPattern(java.util.regex.Pattern.compile(
            "^\\s*multi-module-parent\\s+org\\.springframework\\.boot:spring-boot-starter-parent\\s+2\\.7\\.0\\s+->\\s+2\\.7\\.8\\s*$",
            java.util.regex.Pattern.MULTILINE // Ensure ^ and $ match start/end of lines
        ));
    }
    
    private void createMultiModuleProjectWithParents() throws IOException {
        Path parentPomFile = tempDir.resolve("pom.xml");
        Files.writeString(parentPomFile, getMultiModuleParentWithParentPomXml());
        
        Path moduleADir = tempDir.resolve("module-a");
        Path moduleBDir = tempDir.resolve("module-b");
        Files.createDirectories(moduleADir);
        Files.createDirectories(moduleBDir);
        
        Path moduleAPomFile = moduleADir.resolve("pom.xml");
        Path moduleBPomFile = moduleBDir.resolve("pom.xml");
        Files.writeString(moduleAPomFile, getModuleAPomXml());
        Files.writeString(moduleBPomFile, getModuleBPomXml());
    }

    private String getTestPomWithParentXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>2.7.0</version>
                        <relativePath/>
                    </parent>
                    <groupId>com.example</groupId>
                    <artifactId>test-project</artifactId>
                    <version>1.0.0</version>
                    <name>Test Project</name>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter</artifactId>
                            <!-- Version inherited from parent -->
                        </dependency>
                    </dependencies>
                </project>
                """;
    }
    
    private String getMultiModuleParentWithParentPomXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>2.7.0</version>
                        <relativePath/>
                    </parent>
                    <groupId>com.example</groupId>
                    <artifactId>multi-module-parent</artifactId>
                    <version>1.0.0</version>
                    <packaging>pom</packaging>
                    <name>Multi Module Parent</name>
                    <modules>
                        <module>module-a</module>
                        <module>module-b</module>
                    </modules>
                    <properties>
                        <spring.version>5.3.10</spring.version>
                        <slf4j.version>1.7.30</slf4j.version>
                    </properties>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.springframework</groupId>
                                <artifactId>spring-core</artifactId>
                                <version>${spring.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>org.springframework</groupId>
                                <artifactId>spring-context</artifactId>
                                <version>${spring.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>org.slf4j</groupId>
                                <artifactId>slf4j-api</artifactId>
                                <version>${slf4j.version}</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                </project>
                """;
    }
    
    private String getModuleAPomXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>com.example</groupId>
                        <artifactId>multi-module-parent</artifactId>
                        <version>1.0.0</version>
                    </parent>
                    <artifactId>module-a</artifactId>
                    <version>1.0.0</version>
                    <packaging>jar</packaging>
                    <name>Module A</name>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework</groupId>
                            <artifactId>spring-core</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }
    
    private String getModuleBPomXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>com.example</groupId>
                        <artifactId>multi-module-parent</artifactId>
                        <version>1.0.0</version>
                    </parent>
                    <artifactId>module-b</artifactId>
                    <version>1.0.0</version>
                    <packaging>jar</packaging>
                    <name>Module B</name>
                    <dependencies>
                        <dependency>
                            <groupId>com.example</groupId>
                            <artifactId>module-a</artifactId>
                            <version>1.0.0</version>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework</groupId>
                            <artifactId>spring-context</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }
}