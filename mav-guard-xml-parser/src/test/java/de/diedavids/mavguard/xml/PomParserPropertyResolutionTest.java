package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PomParserPropertyResolutionTest {

    @Test
    void shouldResolveDependencyVersionFromProperty(@TempDir Path tempDir) throws JAXBException, IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getPomWithPropertiesXml());
        PomParser pomParser = new PomParser();

        // When
        Project project = pomParser.parsePomFile(pomFile.toFile());

        // Then
        assertThat(project).isNotNull();
        List<Dependency> dependencies = project.getAllDependencies();
        assertThat(dependencies).hasSize(2);

        // Check first dependency with property version
        Dependency springBootDependency = dependencies.get(0);
        assertThat(springBootDependency.groupId()).isEqualTo("org.springframework.boot");
        assertThat(springBootDependency.artifactId()).isEqualTo("spring-boot-starter");
        assertThat(springBootDependency.version()).isEqualTo("3.2.0"); // Resolved from property

        // Check second dependency with direct version
        Dependency secondDependency = dependencies.get(1);
        assertThat(secondDependency.groupId()).isEqualTo("org.junit.jupiter");
        assertThat(secondDependency.artifactId()).isEqualTo("junit-jupiter");
        assertThat(secondDependency.version()).isEqualTo("5.8.2");
    }

    @Test
    void shouldResolveNestedPropertyInVersion(@TempDir Path tempDir) throws JAXBException, IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getPomWithNestedPropertiesXml());
        PomParser pomParser = new PomParser();

        // When
        Project project = pomParser.parsePomFile(pomFile.toFile());

        // Then
        assertThat(project).isNotNull();
        List<Dependency> dependencies = project.getAllDependencies();
        assertThat(dependencies).hasSize(1);

        // Check dependency with nested property version
        Dependency springBootDependency = dependencies.get(0);
        assertThat(springBootDependency.groupId()).isEqualTo("org.springframework.boot");
        assertThat(springBootDependency.artifactId()).isEqualTo("spring-boot-starter");
        assertThat(springBootDependency.version()).isEqualTo("3.2.0"); // Resolved from nested property
    }

    @Test
    void shouldResolveProjectPropertyInVersion(@TempDir Path tempDir) throws JAXBException, IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getPomWithProjectPropertyXml());
        PomParser pomParser = new PomParser();

        // When
        Project project = pomParser.parsePomFile(pomFile.toFile());

        // Then
        assertThat(project).isNotNull();
        List<Dependency> dependencies = project.getAllDependencies();
        assertThat(dependencies).hasSize(1);

        // Check dependency with project property version
        Dependency dependency = dependencies.get(0);
        assertThat(dependency.groupId()).isEqualTo("com.example");
        assertThat(dependency.artifactId()).isEqualTo("example-core");
        assertThat(dependency.version()).isEqualTo("1.0.0"); // Resolved from project.version
    }

    @Test
    void shouldHandleUnresolvedProperty(@TempDir Path tempDir) throws JAXBException, IOException {
        // Given
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, getPomWithUnresolvedPropertyXml());
        PomParser pomParser = new PomParser();

        // When
        Project project = pomParser.parsePomFile(pomFile.toFile());

        // Then
        assertThat(project).isNotNull();
        List<Dependency> dependencies = project.getAllDependencies();
        assertThat(dependencies).hasSize(1);

        // Check dependency with unresolved property
        Dependency dependency = dependencies.get(0);
        assertThat(dependency.groupId()).isEqualTo("org.springframework.boot");
        assertThat(dependency.artifactId()).isEqualTo("spring-boot-starter");
        assertThat(dependency.version()).isEqualTo("${unknown.version}"); // Unresolved property stays as is
    }

    private String getPomWithPropertiesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test-project</artifactId>
                    <version>1.0.0</version>
                    <name>Test Project</name>
                    
                    <properties>
                        <spring.boot.version>3.2.0</spring.boot.version>
                        <junit.version>5.8.2</junit.version>
                    </properties>
                    
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter</artifactId>
                            <version>${spring.boot.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter</artifactId>
                            <version>5.8.2</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }

    private String getPomWithNestedPropertiesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test-project</artifactId>
                    <version>1.0.0</version>
                    <name>Test Project</name>
                    
                    <properties>
                        <spring.version>3.2.0</spring.version>
                        <spring.boot.version>${spring.version}</spring.boot.version>
                    </properties>
                    
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter</artifactId>
                            <version>${spring.boot.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }

    private String getPomWithProjectPropertyXml() {
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
                            <groupId>com.example</groupId>
                            <artifactId>example-core</artifactId>
                            <version>${project.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }

    private String getPomWithUnresolvedPropertyXml() {
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
                            <version>${unknown.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }
}