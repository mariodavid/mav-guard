package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.MavGuardApplication;
import de.diedavids.mavguard.model.Project;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern; // Import Pattern

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = MavGuardApplication.class, properties = "spring.main.web-application-type=none")
class AnalyzeCommandIntegrationTest {

    @Autowired
    private AnalyzeCommand analyzeCommand;

    @Autowired
    private CommandLine.IFactory factory;


    private File createTempPomFile(String content) throws IOException {
        Path tempFilePath = Files.createTempFile("pom", ".xml");
        File tempFile = tempFilePath.toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    private String getSimplePomXml() {
        return """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>simple-project</artifactId>
                    <version>1.0.0</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.commons</groupId>
                            <artifactId>commons-lang3</artifactId>
                            <version>3.12.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }

    private String getSimplePomWithManagedImportXml() {
        return """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>simple-project-managed</artifactId>
                    <version>1.0.0</version>
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

    private File createMultiModuleProject(String rootPomContent, String moduleAPomContent) throws IOException {
        Path tempProjectDir = Files.createTempDirectory("multi-module-project-");
        tempProjectDir.toFile().deleteOnExit();

        File rootPomFile = new File(tempProjectDir.toFile(), "pom.xml");
        try (FileWriter writer = new FileWriter(rootPomFile)) {
            writer.write(rootPomContent);
        }
        rootPomFile.deleteOnExit();

        File moduleADir = new File(tempProjectDir.toFile(), "module-a");
        if (!moduleADir.mkdir()) {
            throw new IOException("Could not create directory for module-a");
        }
        moduleADir.deleteOnExit();

        File moduleAPomFile = new File(moduleADir, "pom.xml");
        try (FileWriter writer = new FileWriter(moduleAPomFile)) {
            writer.write(moduleAPomContent);
        }
        moduleAPomFile.deleteOnExit();
        return rootPomFile;
    }

    private String getRootPomXml(String... modules) {
        StringBuilder modulesXml = new StringBuilder();
        for (String module : modules) {
            modulesXml.append("<module>").append(module).append("</module>\n");
        }
        return String.format("""
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>multi-module-root</artifactId>
                    <version>1.0.0</version>
                    <packaging>pom</packaging>
                    <modules>
                        %s
                    </modules>
                </project>
                """, modulesXml.toString());
    }

    private String getModuleAPomXml() {
        return """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>com.example</groupId>
                        <artifactId>multi-module-root</artifactId>
                        <version>1.0.0</version>
                    </parent>
                    <artifactId>module-a</artifactId>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.commons</groupId>
                            <artifactId>commons-collections4</artifactId>
                            <version>4.4</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
    }

    private File createMultiModuleProjectWithVersionInconsistencies() throws IOException {
        Path tempProjectDir = Files.createTempDirectory("multi-module-inconsistent-");
        tempProjectDir.toFile().deleteOnExit();

        String rootPom = getRootPomXml("module-a", "module-b");
        File rootPomFile = new File(tempProjectDir.toFile(), "pom.xml");
        try (FileWriter writer = new FileWriter(rootPomFile)) {
            writer.write(rootPom);
        }
        rootPomFile.deleteOnExit();

         String moduleAPom = """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>com.example</groupId>
                        <artifactId>multi-module-root</artifactId>
                        <version>1.0.0</version>
                    </parent>
                    <artifactId>module-a</artifactId>
                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>1.7.32</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        File moduleADir = new File(tempProjectDir.toFile(), "module-a");
        moduleADir.mkdir();
        moduleADir.deleteOnExit();
        File moduleAPomFile = new File(moduleADir, "pom.xml");
        try (FileWriter writer = new FileWriter(moduleAPomFile)) {
            writer.write(moduleAPom);
        }
        moduleAPomFile.deleteOnExit();

        String moduleBPom = """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>com.example</groupId>
                        <artifactId>multi-module-root</artifactId>
                        <version>1.0.0</version>
                    </parent>
                    <artifactId>module-b</artifactId>
                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>1.7.30</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        File moduleBDir = new File(tempProjectDir.toFile(), "module-b");
        moduleBDir.mkdir();
        moduleBDir.deleteOnExit();
        File moduleBPomFile = new File(moduleBDir, "pom.xml");
        try (FileWriter writer = new FileWriter(moduleBPomFile)) {
            writer.write(moduleBPom);
        }
        moduleBPomFile.deleteOnExit();

        return rootPomFile;
    }


    @Test
    void testUsageHelp(CapturedOutput output) {
        new CommandLine(analyzeCommand, factory).execute("--help");
        assertThat(output).contains("Usage: analyze [-hV] [--detailed-usage] [--force-multi-module]");
        assertThat(output).contains("[--color=<colorMode>] <filePath>");
        assertThat(output).contains("--force-multi-module", "Force parsing as a multi-module project");
        assertThat(output).contains("--color=<colorMode>", "When to use colors: auto (default), always, never");
    }

    @Test
    void testAnalyzeSinglePomCommand(CapturedOutput output) throws IOException {
        File pomFile = createTempPomFile(getSimplePomXml());
        new CommandLine(analyzeCommand, factory).execute(pomFile.getAbsolutePath());
        assertThat(output).contains("Analyzing single module project: com.example:simple-project:1.0.0");
        assertThat(output).contains("Project: com.example:simple-project:1.0.0");
        assertThat(output).contains("Parent: None");
    }

    @Test
    void testAnalyzeSinglePomExtractsDependencies(CapturedOutput output) throws IOException {
        File pomFile = createTempPomFile(getSimplePomWithManagedImportXml());
        new CommandLine(analyzeCommand, factory).execute(pomFile.getAbsolutePath());
        assertThat(output).contains("Analyzing single module project: com.example:simple-project-managed:1.0.0");
        assertThat(output).contains("Dependencies (1):");
        assertThat(output).contains("- org.springframework.boot:spring-boot-dependencies:2.7.0 (scope: import) (type: pom)");
    }

    @Test
    void testAnalyzeMultiModuleCommandBaseFunctionality(CapturedOutput output) throws IOException {
        File rootPom = createMultiModuleProject(getRootPomXml("module-a"), getModuleAPomXml());
        new CommandLine(analyzeCommand, factory).execute(rootPom.getAbsolutePath());

        assertThat(output).contains("Analyzing multi-module project: com.example:multi-module-root:1.0.0");
        assertThat(output).contains("Root Project Context: com.example:multi-module-root:1.0.0");
        assertThat(output).contains("Modules included in build (2):");
        // Use regex for root module path to handle temporary directory (reverted to original more flexible regex)
        assertThat(output).containsPattern(Pattern.compile("  - Module: com.example:multi-module-root:1.0.0 \\(Path: .*pom\\.xml\\)"));
        // Use regex for child module path to handle temporary directory and File.separator (reverted to original more flexible regex)
        assertThat(output).containsPattern(Pattern.compile("  - Module: com.example:module-a:1.0.0 \\(Path: .*module-a[\\\\/]pom\\.xml\\)"));
    }


    @Test
    void testAnalyzeMultiModuleCommandShowsConsolidatedDependencies(CapturedOutput output) throws IOException {
        File rootPom = createMultiModuleProject(getRootPomXml("module-a"), getModuleAPomXml());
        new CommandLine(analyzeCommand, factory).execute(rootPom.getAbsolutePath());
        assertThat(output).contains("Root Project Context: com.example:multi-module-root:1.0.0");
        assertThat(output).contains("Consolidated Dependencies (1 unique across all modules):");
        assertThat(output).contains("- org.apache.commons:commons-collections4:4.4");
    }

    @Test
    void testAnalyzeMultiModuleCommandWithVersionInconsistencies(CapturedOutput output) throws IOException {
        File rootPomFile = createMultiModuleProjectWithVersionInconsistencies();
        new CommandLine(analyzeCommand, factory).execute(rootPomFile.getAbsolutePath());

        assertThat(output).contains("WARNING: Found 1 inconsistent dependency versions:");
        assertThat(output).containsPattern("Dependency org.slf4j:slf4j-api has inconsistent versions:\\s*");
        assertThat(output).containsPattern("Version 1\\.7\\.32 used in modules: module-a\\s*");
        assertThat(output).containsPattern("Version 1\\.7\\.30 used in modules: module-b\\s*");
    }

    @Test
    void testAnalyzeMultiModuleCommandWithDetailedUsage(CapturedOutput output) throws IOException {
        File rootPom = createMultiModuleProject(getRootPomXml("module-a"), getModuleAPomXml());
        new CommandLine(analyzeCommand, factory).execute(rootPom.getAbsolutePath(), "--detailed-usage");

        assertThat(output).contains("Dependency Usage by Module:");
        assertThat(output).contains("---------------------------");
        // Use regex for more flexible matching of spacing
        assertThat(output).containsPattern(Pattern.compile("org\\.apache\\.commons:commons-collections4\\s*-\\s*Used/Declared in modules:\\s*module-a"));
    }

    @Disabled("Functionality of checking inconsistencies with non-zero exit code was removed, this is now default part of analyze output.")
    @Test
    void testAnalyzeMultiModuleCommandWithCheckInconsistencies(CapturedOutput output) throws IOException {
        // This test might need to be re-thought or removed as --check-inconsistencies option is gone.
        // For now, keeping it disabled.
    }
}
