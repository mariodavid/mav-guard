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

@SpringBootTest(classes = MavGuardApplication.class, webEnvironment = WebEnvironment.NONE)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
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
        assertThat(output).contains("Parent: org.springframework.boot:spring-boot-starter-parent:2.6.0");
        assertThat(output).contains("--- Update Check Results ---");
        // Check that parent updates are detected (Spring Boot 2.6.0 should have updates available)
        assertThat(output).contains("Parent Project Update");
    }

    @Test
    void testCheckUpdatesMultiModuleCommandWithParent(CapturedOutput output) throws IOException {
        createMultiModuleProjectWithParents();
        Path rootPomFile = tempDir.resolve("pom.xml");

        int exitCode = new CommandLine(checkUpdatesCommand, factory)
                .execute(rootPomFile.toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("--- Project Analysis (Multi-Module): com.example:multi-module-parent:1.0.0 ---");
        assertThat(output).contains("Root Parent: org.springframework.boot:spring-boot-starter-parent:2.6.0");
        assertThat(output).contains("--- Update Check Results ---");
        // Check for parent updates - Spring Boot 2.6.0 should have updates available
        assertThat(output).contains("Parent Project Updates");
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
                        <version>2.6.0</version>
                        <relativePath/>
                    </parent>
                    <groupId>com.example</groupId>
                    <artifactId>test-project</artifactId>
                    <version>1.0.0</version>
                    <name>Test Project</name>
                    <dependencies>
                        <dependency>
                            <groupId>junit</groupId>
                            <artifactId>junit</artifactId>
                            <version>4.12</version>
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
                        <version>2.6.0</version>
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
                        <junit.version>4.12</junit.version>
                        <slf4j.version>1.7.25</slf4j.version>
                    </properties>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>junit</groupId>
                                <artifactId>junit</artifactId>
                                <version>${junit.version}</version>
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
                            <groupId>junit</groupId>
                            <artifactId>junit</artifactId>
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
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }
}