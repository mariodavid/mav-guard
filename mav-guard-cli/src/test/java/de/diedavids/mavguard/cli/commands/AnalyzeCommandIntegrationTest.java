package de.diedavids.mavguard.cli.commands;

import de.diedavids.mavguard.MavGuardApplication; // Assuming this is the main Spring Boot app
import de.diedavids.mavguard.commands.AnalyzeCommand;
import de.diedavids.mavguard.xml.PomParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MavGuardApplication.class) // Specify main application class
@ExtendWith(OutputCaptureExtension.class)
class AnalyzeCommandIntegrationTest {

    @Autowired
    private AnalyzeCommand analyzeCommand; // The actual command bean

    // PomParser would also be autowired if needed directly in test,
    // but for this test, we rely on it being used by AnalyzeCommand.

    @Test
    void testAnalyzeCommandWithSimplePom(CapturedOutput output) throws Exception {
        // Create a temporary POM file for the test
        Path tempDir = Files.createTempDirectory("mavguard-cli-test-");
        File testPomFile = tempDir.resolve("simple-cli-test-pom.xml").toFile();

        // Write the content of simple-cli-test-pom.xml to the temp file
        // (Ideally, load from resources, but direct string for robustness here)
        String pomContent = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                            "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">" +
                            "<modelVersion>4.0.0</modelVersion>" +
                            "<groupId>com.cli.test</groupId>" +
                            "<artifactId>simple-cli-app</artifactId>" +
                            "<version>1.0</version>" +
                            "<dependencies>" +
                            "<dependency>" +
                            "<groupId>org.slf4j</groupId>" +
                            "<artifactId>slf4j-api</artifactId>" +
                            "<version>1.7.30</version>" +
                            "</dependency>" +
                            "</dependencies>" +
                            "</project>";
        try (PrintWriter out = new PrintWriter(new FileOutputStream(testPomFile))) {
            out.println(pomContent);
        }

        // Simulate command execution.
        // For picocli, when run within Spring Boot, direct invocation of `call()` is often best for tests.
        // We need to manually set parameters that would normally be parsed by picocli.
        // This requires AnalyzeCommand to have public setters or a way to set params for testing.
        // If AnalyzeCommand's fields are private and only set by picocli,
        // we might need to use `CommandLine.call`.

        // Assuming AnalyzeCommand's 'filePath' can be set for testing:
        // (This might require a setter in AnalyzeCommand or package-private access)
        // analyzeCommand.setFilePath(testPomFile.getAbsolutePath());
        // Integer exitCode = analyzeCommand.call();

        // Alternative: Use Picocli's CommandLine to execute
        // This is closer to how it runs in production with Spring
        // This assumes AnalyzeCommand is registered as a bean and picked up by picocli
        String[] args = {"analyze", testPomFile.getAbsolutePath()};

        // If MavGuardApplication.main() is how commands are dispatched
        MavGuardApplication.main(args);


        // Assertions
        // assertThat(exitCode).isEqualTo(0); // Check for successful execution
        String consoleOutput = output.getOut();
        assertThat(consoleOutput).contains("Analyzing single module project: com.cli.test:simple-cli-app:1.0");
        assertThat(consoleOutput).contains("org.slf4j:slf4j-api:1.7.30");

        // Clean up the temporary file
        Files.deleteIfExists(testPomFile.toPath());
        Files.deleteIfExists(tempDir);
    }
}
