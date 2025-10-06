package de.diedavids.mavguard.xml;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests error handling and edge cases for PomParser that were missing in the original test suite.
 * These tests ensure the parser fails gracefully with meaningful error messages.
 */
class PomParserErrorHandlingTest {

    private final PomParser parser = new PomParser();

    @Test
    void shouldThrowExceptionForNullFile() {
        assertThatThrownBy(() -> parser.parsePomFile(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("POM file must not be null and must exist");
    }

    @Test
    void shouldThrowExceptionForNonExistentFile() {
        File nonExistentFile = new File("does-not-exist.xml");
        assertThatThrownBy(() -> parser.parsePomFile(nonExistentFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("POM file must not be null and must exist");
    }

    @Test
    void shouldThrowExceptionForNullInputStream() {
        assertThatThrownBy(() -> parser.parsePomStream(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input stream must not be null");
    }

    @Test
    void shouldThrowJAXBExceptionForMalformedXml() {
        String malformedXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                    <unclosed-tag>
                        <nested>value</nested>
                    <!-- Missing closing tag -->
                </project>
                """;

        InputStream stream = new ByteArrayInputStream(malformedXml.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> parser.parsePomStream(stream))
                .isInstanceOf(JAXBException.class);
    }

    @Test
    void shouldThrowJAXBExceptionForInvalidXmlStructure() {
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <not-a-maven-project>
                    <this-is-not-maven>true</this-is-not-maven>
                </not-a-maven-project>
                """;

        InputStream stream = new ByteArrayInputStream(invalidXml.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> parser.parsePomStream(stream))
                .isInstanceOf(JAXBException.class);
    }

    @Test
    void shouldHandleEmptyFile(@TempDir Path tempDir) throws IOException {
        Path emptyPomFile = tempDir.resolve("empty.xml");
        Files.writeString(emptyPomFile, "");

        assertThatThrownBy(() -> parser.parsePomFile(emptyPomFile.toFile()))
                .isInstanceOf(JAXBException.class);
    }

    @Test
    void shouldHandleFileWithOnlyWhitespace(@TempDir Path tempDir) throws IOException {
        Path whitespacePomFile = tempDir.resolve("whitespace.xml");
        Files.writeString(whitespacePomFile, "   \n\t  \n  ");

        assertThatThrownBy(() -> parser.parsePomFile(whitespacePomFile.toFile()))
                .isInstanceOf(JAXBException.class);
    }

    @Test
    void shouldHandlePomWithMissingRequiredFields(@TempDir Path tempDir) throws IOException, JAXBException {
        // This POM is missing required fields like groupId
        String incompletePom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <!-- Missing groupId -->
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
                """;

        Path pomFile = tempDir.resolve("incomplete.xml");
        Files.writeString(pomFile, incompletePom);

        // Parser should not throw exception for missing fields (Maven allows inheritance)
        // but the resulting object should handle missing data gracefully
        var project = parser.parsePomFile(pomFile.toFile());
        assertThat(project).isNotNull();
        // GroupId might be null when inherited from parent
        assertThat(project.artifactId()).isEqualTo("test");
        assertThat(project.version()).isEqualTo("1.0.0");
    }

    @Test 
    void shouldHandlePomWithInvalidCharacters(@TempDir Path tempDir) throws IOException {
        String pomWithInvalidChars = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test-\u0000-invalid</artifactId>
                    <version>1.0.0</version>
                </project>
                """;

        Path pomFile = tempDir.resolve("invalid-chars.xml");
        Files.writeString(pomFile, pomWithInvalidChars);

        // Should either parse successfully (ignoring invalid chars) or throw JAXBException
        // The important thing is it doesn't crash with unexpected exceptions
        try {
            parser.parsePomFile(pomFile.toFile());
            // If it succeeds, that's fine too
        } catch (JAXBException e) {
            // This is also acceptable behavior
            assertThat(e).isInstanceOf(JAXBException.class);
        }
    }

    @Test
    void shouldGracefullyHandleCircularPropertyReferences(@TempDir Path tempDir) throws IOException, JAXBException {
        String pomWithCircularProps = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>circular-test</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <prop.a>${prop.b}</prop.a>
                        <prop.b>${prop.c}</prop.b>
                        <prop.c>${prop.a}</prop.c>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.example</groupId>
                            <artifactId>test</artifactId>
                            <version>${prop.a}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        Path pomFile = tempDir.resolve("circular.xml");
        Files.writeString(pomFile, pomWithCircularProps);

        // Should not crash, should handle circular references gracefully
        // Either by detecting the cycle or by limiting resolution depth
        var project = parser.parsePomFile(pomFile.toFile());
        assertThat(project).isNotNull();
        
        // The dependency version should still contain the placeholder if circular reference is detected
        var dependencies = project.getAllDependencies();
        assertThat(dependencies).hasSize(1);
        // Version should either be unresolved placeholder or some error indicator
        assertThat(dependencies.get(0).version()).contains("${");
    }
}