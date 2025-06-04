package de.diedavids.mavguard.commands;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
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

import de.diedavids.mavguard.MavGuardApplication;
import picocli.CommandLine;

/**
 * End-to-end integration test that validates real Maven Central integration.
 * This test makes actual network calls to Maven Central to verify functionality.
 */
@SpringBootTest(classes = MavGuardApplication.class, webEnvironment = WebEnvironment.NONE)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("real-maven-central") // Different profile to avoid mock service
class DependencyCommandsRealMavenCentralTest {

    @Autowired
    private CheckUpdatesCommand checkUpdatesCommand;

    @Autowired
    private DependencyVersionService dependencyVersionService; // Should use real RepositoryDependencyService

    @Autowired
    private CommandLine.IFactory factory;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Verify we're using the real service, not the mock
        assertThat(dependencyVersionService).isNotInstanceOf(TestDependencyVersionService.class);
    }

    @Test
    void testCheckUpdatesCommandWithRealMavenCentral(CapturedOutput output) throws IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomXmlWithOldDependencies());

        // When
        int exitCode = new CommandLine(checkUpdatesCommand, factory)
                .execute(pomFile.toString());

        // Then
        assertThat(exitCode).isEqualTo(0);
        
        // Verify basic project analysis structure
        assertThat(output).contains("--- Project Analysis (Single Module): com.example:test-project:1.0.0 ---");
        assertThat(output).contains("Project: com.example:test-project:1.0.0");
        assertThat(output).contains("--- Update Check Results ---");
        
        // For this smoke test, we just verify that:
        // 1. The command executed successfully (exit code 0)
        // 2. Basic output structure is present
        // 3. At least one of our known old dependencies is mentioned in output
        // We don't assert specific version numbers since they change over time in Maven Central
        
        String outputText = output.toString();
        boolean hasJunitReference = outputText.contains("junit:junit");
        boolean hasSlf4jReference = outputText.contains("org.slf4j:slf4j-api");
        
        // At least one of our dependencies should be mentioned (either as update available or processed)
        assertThat(hasJunitReference || hasSlf4jReference)
            .describedAs("Expected at least one dependency (junit or slf4j) to be mentioned in output")
            .isTrue();
    }

    private String getTestPomXmlWithOldDependencies() {
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
                        <!-- Using known old versions that should have updates in Maven Central -->
                        <dependency>
                            <groupId>junit</groupId>
                            <artifactId>junit</artifactId>
                            <version>4.12</version> <!-- Released 2014, should have updates -->
                        </dependency>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>1.7.25</version> <!-- Released 2017, should have updates -->
                        </dependency>
                    </dependencies>
                </project>
                """;
    }
}