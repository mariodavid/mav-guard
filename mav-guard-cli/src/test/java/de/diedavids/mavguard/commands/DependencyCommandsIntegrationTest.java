package de.diedavids.mavguard.commands;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import org.junit.jupiter.api.BeforeEach;
// import java.util.List; // No longer directly used here

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import de.diedavids.mavguard.MavGuardApplication;
import picocli.CommandLine;

@LocalOnlyTest
@SpringBootTest(classes = MavGuardApplication.class, webEnvironment = WebEnvironment.NONE)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
class DependencyCommandsIntegrationTest {

    @Autowired
    private CheckUpdatesCommand checkUpdatesCommand;

    @Autowired
    private DependencyVersionService dependencyVersionService; // Uses TestDependencyVersionService

    @Autowired
    private CommandLine.IFactory factory;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // TestDependencyVersionService is automatically injected and configured
        // No mocking needed - it provides test data directly
    }

    @Test
    void testCheckUpdatesCommand(CapturedOutput output) throws IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomXml());

        // When
        int exitCode = new CommandLine(checkUpdatesCommand, factory)
                .execute(pomFile.toString());

        // Verify the result
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("--- Project Analysis (Single Module): com.example:test-project:1.0.0 ---");
        assertThat(output).contains("Project: com.example:test-project:1.0.0");
        assertThat(output).contains("--- Update Check Results ---");
        assertThat(output).containsPattern("org.springframework.boot:spring-boot-starter\\s+2.7.0\\s+-> 2.7.5");
    }

    @Test
    void testCheckUpdatesMultiModuleCommand(CapturedOutput output) throws IOException {
        // Given
        createMultiModuleProject();
        Path rootPomFile = tempDir.resolve("pom.xml");

        // When
        int exitCode = new CommandLine(checkUpdatesCommand, factory)
                .execute(rootPomFile.toString());

        // Verify the result
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("--- Project Analysis (Multi-Module): com.example:multi-module-test:1.0.0 ---");
        assertThat(output).contains("Root Project: com.example:multi-module-test:1.0.0");
        assertThat(output).contains("--- Update Check Results ---");
        assertThat(output).contains("Consolidated Dependency Updates Available:");
        assertThat(output).containsPattern("org.springframework:spring-core\\s+5.3.10\\s+-> 5.3.25");
        assertThat(output).containsPattern("org.springframework:spring-context\\s+5.3.10\\s+-> 5.3.25");
        assertThat(output).containsPattern("org.slf4j:slf4j-api\\s+1.7.30\\s+-> 1.7.36");
    }
    
    private void createMultiModuleProject() throws IOException {
        Path parentPomFile = tempDir.resolve("pom.xml");
        Files.writeString(parentPomFile, getMultiModuleParentPomXml());
        
        Path moduleADir = tempDir.resolve("module-a");
        Path moduleBDir = tempDir.resolve("module-b");
        Files.createDirectories(moduleADir);
        Files.createDirectories(moduleBDir);
        
        Path moduleAPomFile = moduleADir.resolve("pom.xml");
        Path moduleBPomFile = moduleBDir.resolve("pom.xml");
        Files.writeString(moduleAPomFile, getModuleAPomXml());
        Files.writeString(moduleBPomFile, getModuleBPomXml());
    }

    private String getTestPomXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test-project</artifactId>
                    <version>1.0.0</version>
                    <name>Test Project</name>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter</artifactId>
                            <version>2.7.0</version> <!-- Assuming this version has updates -->
                        </dependency>
                    </dependencies>
                </project>
                """;
    }
    
    private String getMultiModuleParentPomXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>multi-module-test</artifactId>
                    <version>1.0.0</version>
                    <packaging>pom</packaging>
                    <name>Multi Module Test Project</name>
                    <modules>
                        <module>module-a</module>
                        <module>module-b</module>
                    </modules>
                    <properties>
                        <spring.version>5.3.10</spring.version> <!-- Older Spring version -->
                        <slf4j.version>1.7.30</slf4j.version> <!-- Older slf4j version -->
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
                        <artifactId>multi-module-test</artifactId>
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
                        <artifactId>multi-module-test</artifactId>
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