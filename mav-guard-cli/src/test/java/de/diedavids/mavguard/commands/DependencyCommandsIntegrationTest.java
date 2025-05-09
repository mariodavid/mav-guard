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
class DependencyCommandsIntegrationTest {

    @Autowired
    private DependencyCommands dependencyCommands;

    @Autowired
    private CommandLine.IFactory factory;

    @TempDir
    Path tempDir;

    @Test
    void testCheckUpdatesCommand(CapturedOutput output) throws IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomXml());

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
        assertThat(output).contains("org.springframework.boot:spring-boot-starter      2.7.0 -> ");
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
                            <version>2.7.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }
}
