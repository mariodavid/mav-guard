package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PomParserTest {

    @Test
    void shouldParsePomStream_withValidPom() throws JAXBException {
        // Given
        InputStream inputStream = new ByteArrayInputStream(getTestPomXml().getBytes(StandardCharsets.UTF_8));
        XmlParser xmlParser = new XmlParser();
        PomParser pomParser = new PomParser(xmlParser);

        // When
        Project project = pomParser.parsePomStream(inputStream);

        // Then
        assertThat(project).isNotNull();
        assertThat(project.groupId()).isEqualTo("com.example");
        assertThat(project.artifactId()).isEqualTo("test-project");
        assertThat(project.version()).isEqualTo("1.0.0");
        assertThat(project.name()).isEqualTo("Test Project");
    }

    @Test
    void shouldParsePomFile_withValidPom(@TempDir Path tempDir) throws JAXBException, IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomXml());
        XmlParser xmlParser = new XmlParser();
        PomParser pomParser = new PomParser(xmlParser);

        // When
        Project project = pomParser.parsePomFile(pomFile.toFile());

        // Then
        assertThat(project).isNotNull();
        assertThat(project.groupId()).isEqualTo("com.example");
        assertThat(project.artifactId()).isEqualTo("test-project");
        assertThat(project.version()).isEqualTo("1.0.0");
    }

    @Test
    void shouldExtractDependencies_fromValidPom(@TempDir Path tempDir) throws JAXBException, IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getTestPomXml());
        XmlParser xmlParser = new XmlParser();
        PomParser pomParser = new PomParser(xmlParser);

        // When
        Project project = pomParser.parsePomFile(pomFile.toFile());
        List<Dependency> dependencies = project.getAllDependencies();

        // Then
        assertThat(dependencies).isNotNull();
        assertThat(dependencies).hasSize(3); // 2 from dependencies + 1 from dependencyManagement

        // Check first dependency
        Dependency firstDependency = dependencies.get(0);
        assertThat(firstDependency.groupId()).isEqualTo("org.springframework.boot");
        assertThat(firstDependency.artifactId()).isEqualTo("spring-boot-starter");
        assertThat(firstDependency.version()).isEqualTo("2.7.0");

        // Check second dependency
        Dependency secondDependency = dependencies.get(1);
        assertThat(secondDependency.groupId()).isEqualTo("org.junit.jupiter");
        assertThat(secondDependency.artifactId()).isEqualTo("junit-jupiter");
        assertThat(secondDependency.version()).isEqualTo("5.8.2");
        assertThat(secondDependency.scope()).isEqualTo("test");

        // Check third dependency
        Dependency thirdDependency = dependencies.get(2);
        assertThat(thirdDependency.groupId()).isEqualTo("org.springframework.boot");
        assertThat(thirdDependency.artifactId()).isEqualTo("spring-boot-dependencies");
        assertThat(thirdDependency.version()).isEqualTo("2.7.0");
        assertThat(thirdDependency.type()).isEqualTo("pom");
        assertThat(thirdDependency.scope()).isEqualTo("import");
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
