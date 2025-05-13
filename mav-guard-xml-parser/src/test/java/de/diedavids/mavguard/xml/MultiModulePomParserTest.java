package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the multi-module support in PomParser
 */
public class MultiModulePomParserTest {

    private PomParser pomParser;
    private MultiModuleDependencyCollector dependencyCollector;

    @TempDir
    Path tempDir;

    private Path rootPomPath;
    private Path module1PomPath;
    private Path module2PomPath;

    @BeforeEach
    void setUp() throws IOException {
        pomParser = new PomParser();
        dependencyCollector = new MultiModuleDependencyCollector();

        // Create a test multi-module project structure
        rootPomPath = createRootPom();
        module1PomPath = createModule1Pom();
        module2PomPath = createModule2Pom();
    }

    @Test
    void testParseMultiModuleProject() throws JAXBException {
        // Parse the multi-module project
        List<Project> projects = pomParser.parseMultiModuleProject(rootPomPath.toFile());

        // Verify the result
        assertNotNull(projects);
        assertEquals(3, projects.size(), "Should find root and 2 modules");

        // Verify project coordinates
        Map<String, Project> projectMap = projects.stream()
                .collect(Collectors.toMap(
                        project -> project.artifactId(),
                        project -> project
                ));

        assertTrue(projectMap.containsKey("multi-module-parent"));
        assertTrue(projectMap.containsKey("module1"));
        assertTrue(projectMap.containsKey("module2"));

        // Verify parent-child relationships
        Project module1 = projectMap.get("module1");
        assertNotNull(module1.parent());
        assertEquals("multi-module-parent", module1.parent().artifactId());

        Project module2 = projectMap.get("module2");
        assertNotNull(module2.parent());
        assertEquals("multi-module-parent", module2.parent().artifactId());
    }

    @Test
    void testPropertyInheritance() throws JAXBException {
        // Parse the multi-module project
        List<Project> projects = pomParser.parseMultiModuleProject(rootPomPath.toFile());
        
        // Get module1 project
        Project module1 = projects.stream()
                .filter(p -> "module1".equals(p.artifactId()))
                .findFirst()
                .orElseThrow();
        
        // Verify that the property was resolved correctly from parent
        Map<String, String> properties = module1.properties();
        assertTrue(properties.containsKey("spring.version"), "Should contain spring.version property");
        assertEquals("5.3.15", properties.get("spring.version"), "Should resolve property from parent");
    }

    @Test
    void testVersionInconsistencies() throws JAXBException {
        // Parse the multi-module project
        List<Project> projects = pomParser.parseMultiModuleProject(rootPomPath.toFile());
        
        // Verify that module2 has a different junit version defined
        Project module2 = projects.stream()
                .filter(p -> "module2".equals(p.artifactId()))
                .findFirst()
                .orElseThrow();
        
        Map<String, String> module2Properties = module2.properties();
        assertTrue(module2Properties.containsKey("junit.version"), "Module 2 should have junit.version property");
        assertEquals("5.9.0", module2Properties.get("junit.version"), "Module 2 should have junit.version=5.9.0");
        
        // Get parent project
        Project parent = projects.stream()
                .filter(p -> "multi-module-parent".equals(p.artifactId()))
                .findFirst()
                .orElseThrow();
        
        Map<String, String> parentProperties = parent.properties();
        assertTrue(parentProperties.containsKey("junit.version"), "Parent should have junit.version property");
        assertEquals("5.8.2", parentProperties.get("junit.version"), "Parent should have junit.version=5.8.2");
    }

    /**
     * Creates the root POM file for testing
     */
    private Path createRootPom() throws IOException {
        Path pomPath = tempDir.resolve("pom.xml");
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>com.example</groupId>\n" +
                "    <artifactId>multi-module-parent</artifactId>\n" +
                "    <version>1.0.0</version>\n" +
                "    <packaging>pom</packaging>\n" +
                "    <name>Multi-Module Parent</name>\n" +
                "    \n" +
                "    <properties>\n" +
                "        <java.version>17</java.version>\n" +
                "        <spring.version>5.3.15</spring.version>\n" +
                "        <junit.version>5.8.2</junit.version>\n" +
                "    </properties>\n" +
                "    \n" +
                "    <modules>\n" +
                "        <module>module1</module>\n" +
                "        <module>module2</module>\n" +
                "    </modules>\n" +
                "    \n" +
                "    <dependencyManagement>\n" +
                "        <dependencies>\n" +
                "            <dependency>\n" +
                "                <groupId>org.springframework</groupId>\n" +
                "                <artifactId>spring-core</artifactId>\n" +
                "                <version>${spring.version}</version>\n" +
                "            </dependency>\n" +
                "            <dependency>\n" +
                "                <groupId>org.springframework</groupId>\n" +
                "                <artifactId>spring-context</artifactId>\n" +
                "                <version>${spring.version}</version>\n" +
                "            </dependency>\n" +
                "            <dependency>\n" +
                "                <groupId>org.junit.jupiter</groupId>\n" +
                "                <artifactId>junit-jupiter</artifactId>\n" +
                "                <version>${junit.version}</version>\n" +
                "                <scope>test</scope>\n" +
                "            </dependency>\n" +
                "        </dependencies>\n" +
                "    </dependencyManagement>\n" +
                "</project>";
        Files.writeString(pomPath, pomContent);
        
        // Create module directories
        Files.createDirectory(tempDir.resolve("module1"));
        Files.createDirectory(tempDir.resolve("module2"));
        
        return pomPath;
    }

    /**
     * Creates the module1 POM file for testing
     */
    private Path createModule1Pom() throws IOException {
        Path moduleDir = tempDir.resolve("module1");
        Path pomPath = moduleDir.resolve("pom.xml");
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <parent>\n" +
                "        <groupId>com.example</groupId>\n" +
                "        <artifactId>multi-module-parent</artifactId>\n" +
                "        <version>1.0.0</version>\n" +
                "    </parent>\n" +
                "    <artifactId>module1</artifactId>\n" +
                "    <name>Module 1</name>\n" +
                "    \n" +
                "    <dependencies>\n" +
                "        <dependency>\n" +
                "            <groupId>org.springframework</groupId>\n" +
                "            <artifactId>spring-core</artifactId>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.junit.jupiter</groupId>\n" +
                "            <artifactId>junit-jupiter</artifactId>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>com.fasterxml.jackson.core</groupId>\n" +
                "            <artifactId>jackson-databind</artifactId>\n" +
                "            <version>2.13.3</version>\n" +
                "        </dependency>\n" +
                "    </dependencies>\n" +
                "</project>";
        Files.writeString(pomPath, pomContent);
        return pomPath;
    }

    /**
     * Creates the module2 POM file for testing
     */
    private Path createModule2Pom() throws IOException {
        Path moduleDir = tempDir.resolve("module2");
        Path pomPath = moduleDir.resolve("pom.xml");
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <parent>\n" +
                "        <groupId>com.example</groupId>\n" +
                "        <artifactId>multi-module-parent</artifactId>\n" +
                "        <version>1.0.0</version>\n" +
                "    </parent>\n" +
                "    <artifactId>module2</artifactId>\n" +
                "    <name>Module 2</name>\n" +
                "    \n" +
                "    <properties>\n" +
                "        <junit.version>5.9.0</junit.version>\n" +
                "    </properties>\n" +
                "    \n" +
                "    <dependencies>\n" +
                "        <dependency>\n" +
                "            <groupId>org.springframework</groupId>\n" +
                "            <artifactId>spring-context</artifactId>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.junit.jupiter</groupId>\n" +
                "            <artifactId>junit-jupiter</artifactId>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>com.fasterxml.jackson.core</groupId>\n" +
                "            <artifactId>jackson-databind</artifactId>\n" +
                "            <version>2.13.3</version>\n" +
                "        </dependency>\n" +
                "    </dependencies>\n" +
                "</project>";
        Files.writeString(pomPath, pomContent);
        return pomPath;
    }
}