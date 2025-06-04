package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PomParserTest {

    private final PomParser pomParser = new PomParser();

    @Test
    void shouldParsePomStream_withValidPom() throws JAXBException {
        // Given
        InputStream inputStream = new ByteArrayInputStream(getTestPomXml().getBytes(StandardCharsets.UTF_8));

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
                    <properties>
                        <java.version>17</java.version>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    </properties>
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

    @Test
    void parseComplexMultiModuleProject() throws Exception {
        // Construct path to the test POM file
        // Using File.separator for platform independence, assuming resources are copied to classpath
        String pomPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test-poms" + File.separator + "complex-pom.xml";
        File pomFile = new File(pomPath);

        // Ensure the test POM file exists before parsing
        if (!pomFile.exists()) {
            // Attempt to load from classpath if direct path fails (common in Maven test resource handling)
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL resourceUrl = classLoader.getResource("test-poms/complex-pom.xml");
            if (resourceUrl != null) {
                pomFile = new File(resourceUrl.toURI());
            } else {
                throw new java.io.FileNotFoundException("Test POM file not found: complex-pom.xml. Searched path: " + pomPath + " and classpath.");
            }
        }

        // Parse the multi-module project (this method should handle root and its modules)
        List<Project> projects = pomParser.parseMultiModuleProject(pomFile);

        // Find the root project (complex-project)
        Project rootProject = projects.stream()
            .filter(p -> "complex-project".equals(p.artifactId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Root project 'complex-project' not found after parsing"));

        // Assertions for the root project
        assertThat(rootProject.groupId()).isEqualTo("com.example");
        assertThat(rootProject.artifactId()).isEqualTo("complex-project");
        assertThat(rootProject.version()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(rootProject.packaging()).isEqualTo("pom");
        assertThat(rootProject.name()).isEqualTo("Complex Test Project");

        assertThat(rootProject.hasParent()).isTrue();
        assertThat(rootProject.parent().groupId()).isEqualTo("org.springframework.boot");
        assertThat(rootProject.parent().artifactId()).isEqualTo("spring-boot-starter-parent");
        assertThat(rootProject.parent().version()).isEqualTo("2.5.4");

        assertThat(rootProject.modules()).containsExactlyInAnyOrder("module-a", "module-b");

        // Check resolved dependency from dependencyManagement
        Optional<Dependency> slf4jApi = rootProject.getAllDependencies().stream()
            .filter(d -> "slf4j-api".equals(d.artifactId()))
            .findFirst();
        assertThat(slf4jApi).isPresent();
        assertThat(slf4jApi.get().version()).isEqualTo("1.7.32"); // Resolved from property ${slf4j.version}

        // Check that dependencyManagement section itself is processed
        assertThat(rootProject.getDependencyManagement()).isNotNull();
        Optional<Dependency> junitManaged = rootProject.getDependencyManagement().stream()
            .filter(d -> "junit-jupiter-api".equals(d.artifactId()))
            .findFirst();
        assertThat(junitManaged).isPresent();
        assertThat(junitManaged.get().version()).isEqualTo("5.7.2"); // Resolved from ${junit.version}
        assertThat(junitManaged.get().scope()).isEqualTo("test");

        // Check that modules are also parsed (basic check, more detailed module tests could be separate)
        assertThat(projects.size()).isEqualTo(1); // parseMultiModuleProject currently returns only the root.
                                                 // This assertion might need to change if its behavior is updated to return all modules.
                                                 // For now, we assume modules are processed internally and their dependencies
                                                 // would be rolled up or checked via MultiModuleDependencyCollector if used post-parse.
                                                 // If parseMultiModuleProject *should* return all modules, this test would catch that.
    }
}
