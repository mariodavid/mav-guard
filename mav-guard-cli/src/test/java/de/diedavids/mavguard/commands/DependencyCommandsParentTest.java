package de.diedavids.mavguard.commands;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import de.diedavids.mavguard.MavGuardApplication;
import picocli.CommandLine;

@LocalOnlyTest
@SpringBootTest(classes = MavGuardApplication.class, webEnvironment = WebEnvironment.NONE)
@ExtendWith(OutputCaptureExtension.class)
class DependencyCommandsParentTest {

    @Autowired
    private DependencyCommands dependencyCommands;

    @Autowired
    private CommandLine.IFactory factory;

    @TempDir
    Path tempDir;

    @Test
    void testCheckUpdatesCommandWithParent(CapturedOutput output) throws IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomWithParentXml());

        // When
        CommandLine.ParseResult parseResult = new CommandLine(dependencyCommands, factory)
                .parseArgs("check-updates", pomFile.toString());

        // Then
        assertThat(parseResult.hasSubcommand()).isTrue();
        CommandLine.ParseResult subResult = parseResult.subcommand();
        var command = (DependencyCommands.CheckUpdatesCommand) subResult.commandSpec().userObject();

        // Execute the command
        Integer result = command.call();

        // Verify the result
        assertThat(result).isEqualTo(0);
        assertThat(output).contains("Checking for updates for dependencies in com.example:test-project:1.0.0");
        assertThat(output).contains("Checking for parent updates:");
        assertThat(output).contains("Parent: org.springframework.boot:spring-boot-starter-parent");
    }

    @Test
    void testCheckUpdatesMultiModuleCommandWithParent(CapturedOutput output) throws IOException {
        // Given
        createMultiModuleProjectWithParents();

        // When
        CommandLine.ParseResult parseResult = new CommandLine(dependencyCommands, factory)
                .parseArgs("check-updates", tempDir.resolve("pom.xml").toString(), "--multi-module");

        // Then
        assertThat(parseResult.hasSubcommand()).isTrue();
        CommandLine.ParseResult subResult = parseResult.subcommand();
        var command = (DependencyCommands.CheckUpdatesCommand) subResult.commandSpec().userObject();

        // Execute the command
        Integer result = command.call();

        // Verify the result
        assertThat(result).isEqualTo(0);
        assertThat(output).contains("Checking for updates for dependencies in multi-module project");
        assertThat(output).contains("Checking for parent updates:");
        // The parent is org.springframework.boot:spring-boot-starter-parent based on our test POM
        assertThat(output).contains("Parent: org.springframework.boot:spring-boot-starter-parent");
    }
    
    private void createMultiModuleProjectWithParents() throws IOException {
        // Create parent POM
        Path parentPomFile = tempDir.resolve("pom.xml");
        Files.writeString(parentPomFile, getMultiModuleParentWithParentPomXml());
        
        // Create module directories
        Path moduleADir = tempDir.resolve("module-a");
        Path moduleBDir = tempDir.resolve("module-b");
        Files.createDirectories(moduleADir);
        Files.createDirectories(moduleBDir);
        
        // Create module POMs
        Path moduleAPomFile = moduleADir.resolve("pom.xml");
        Path moduleBPomFile = moduleBDir.resolve("pom.xml");
        Files.writeString(moduleAPomFile, getModuleAPomWithParentXml());
        Files.writeString(moduleBPomFile, getModuleBPomWithParentXml());
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
                        <spring.version>5.3.20</spring.version>
                        <slf4j.version>1.7.36</slf4j.version>
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
    
    private String getModuleAPomWithParentXml() {
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
    
    private String getModuleBPomWithParentXml() {
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