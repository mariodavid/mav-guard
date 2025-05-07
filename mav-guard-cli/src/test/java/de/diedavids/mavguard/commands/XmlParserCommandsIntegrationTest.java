package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.MavGuardApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MavGuardApplication.class, webEnvironment = WebEnvironment.NONE)
@ExtendWith(OutputCaptureExtension.class)
class XmlParserCommandsIntegrationTest {

    @Autowired
    private XmlParserCommands xmlParserCommands;

    @Autowired
    private CommandLine.IFactory factory;

    @TempDir
    Path tempDir;


    @Test
    void testUsageHelp() {
        // Given
        String expected = """
                Usage: xml [-hV] [COMMAND]
                XML parsing operations
                  -h, --help      Show this help message and exit.
                  -V, --version   Print version information and exit.
                Commands:
                  parse-pom             Parse a Maven POM file
                  extract-dependencies  Extract dependencies from a Maven POM file
                """;

        // When
        String actual = new CommandLine(xmlParserCommands, factory).getUsageMessage(CommandLine.Help.Ansi.OFF);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testParsePomCommand(CapturedOutput output) throws IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomXml());

        // When
        CommandLine.ParseResult parseResult = new CommandLine(xmlParserCommands, factory)
            .parseArgs("parse-pom", pomFile.toString());

        // Then
        assertThat(parseResult.hasSubcommand()).isTrue();
        CommandLine.ParseResult subResult = parseResult.subcommand();
        XmlParserCommands.ParsePomCommand command = 
            (XmlParserCommands.ParsePomCommand) subResult.commandSpec().userObject();

        // Execute the command
        Integer result = command.call();

        // Verify the result
        assertThat(result).isEqualTo(0);
        assertThat(output).contains("Successfully parsed POM file: com.example:test-project:1.0.0");
    }

    @Test
    void testExtractDependenciesCommand(CapturedOutput output) throws IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomXml());

        // When
        CommandLine.ParseResult parseResult = new CommandLine(xmlParserCommands, factory)
            .parseArgs("extract-dependencies", pomFile.toString());

        // Then
        assertThat(parseResult.hasSubcommand()).isTrue();
        CommandLine.ParseResult subResult = parseResult.subcommand();
        XmlParserCommands.ExtractDependenciesCommand command = 
            (XmlParserCommands.ExtractDependenciesCommand) subResult.commandSpec().userObject();

        // Execute the command
        Integer result = command.call();

        // Verify the result
        assertThat(result).isEqualTo(0);
        assertThat(output).contains("Dependencies found in POM file:");
        assertThat(output).contains("org.springframework.boot:spring-boot-starter:2.7.0");
        assertThat(output).contains("org.junit.jupiter:junit-jupiter:5.8.2 (scope: test)");
        assertThat(output).contains("org.springframework.boot:spring-boot-dependencies:2.7.0 (scope: import) (type: pom)");
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
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter</artifactId>
                            <version>5.8.2</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-dependencies</artifactId>
                                <version>2.7.0</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                </project>
                """;
    }
}
