package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import de.diedavids.mavguard.service.ColorOutputService;
import de.diedavids.mavguard.xml.MultiModuleDependencyCollector;
import de.diedavids.mavguard.xml.PomParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckUpdatesCommandTest {

    @Mock
    private PomParser pomParser;

    @Mock
    private DependencyVersionService versionService;

    @Mock
    private MultiModuleDependencyCollector dependencyCollector;

    @Mock
    private ColorOutputService colorOutput;

    @InjectMocks
    private CheckUpdatesCommand checkUpdatesCommand;

    @Captor
    private ArgumentCaptor<String> printfFormatCaptor;

    @Captor
    private ArgumentCaptor<Object[]> printfArgsCaptor;

    @Captor
    private ArgumentCaptor<String> printlnCaptor;

    @Captor
    private ArgumentCaptor<ColorOutputService.ColorType> colorTypeCaptor;

    private Project mockProject;
    private Project.Parent mockParent;

    @BeforeEach
    void setUp() {
        // Initialize PicoCLI command with options
        checkUpdatesCommand = new CheckUpdatesCommand(pomParser, versionService, dependencyCollector, colorOutput);
        CommandLine commandLine = new CommandLine(checkUpdatesCommand);
        // Provide a default valid file path for tests that need it.
        // Tests that need specific file interactions will mock parsePomFile directly.
        commandLine.parseArgs("dummy.pom");


        mockProject = new Project("com.example", "test-project", "1.0.0");
        // Ensure relativePath is not null to avoid NPEs if logic relies on it.
        // For tests focusing on single module, this might not be strictly needed for parsing itself,
        // but good for consistency if Project objects are reused.
        mockProject.setRelativePath("dummy.pom");
    }

    // Helper method to create a Dependency
    private Dependency createDependency(String groupId, String artifactId, String version) {
        Dependency dep = new Dependency(groupId, artifactId, version);
        return dep;
    }

    // Helper method to create a Parent
    private Project.Parent createParent(String groupId, String artifactId, String version) {
        Project.Parent parent = new Project.Parent(groupId, artifactId, version);
        return parent;
    }

    // Test Case 1: Basic Alignment (Short Names/Versions) for Single Module
    @Test
    void testHandleSingleModuleUpdates_BasicAlignment() throws Exception {
        // Arrange
        Dependency dep1 = createDependency("com.group", "artifact", "1.0");
        mockProject.addDependency(dep1);
        mockProject.setParent(null); // No parent for this specific test part or mock explicitly if needed

        when(pomParser.parsePomFile(any(File.class))).thenReturn(mockProject);
        when(versionService.getLatestVersion(dep1)).thenReturn(Optional.of("1.1"));
        // No parent update for simplicity in this test case

        // Act
        checkUpdatesCommand.call();

        // Assert
        verify(colorOutput, atLeastOnce()).printf(printfFormatCaptor.capture(), printfArgsCaptor.capture());
        verify(colorOutput, atLeastOnce()).println(printlnCaptor.capture());

        List<String> allFormats = printfFormatCaptor.getAllValues();
        List<String> allPrintlns = printlnCaptor.getAllValues();

        // Expected minimums or header lengths will dominate
        // depColWidth = Math.max(Math.max(("com.group:artifact").length(), 30), "DEPENDENCY".length()) = Math.max(19, 30) = 30
        // currentVerColWidth = Math.max(Math.max("1.0".length(), 15), "CURRENT".length()) = Math.max(3,15) = 15
        // latestVerColWidth = Math.max(Math.max("1.1".length(), 15), "LATEST".length()) = Math.max(3,15) = 15
        String expectedFormat = "  %-30s %-15s %-5s %-15s%n";
        assertTrue(allFormats.stream().anyMatch(f -> f.equals(expectedFormat)),
            "Expected format string not found. Found: " + allFormats);

        int expectedTotalWidth = 30 + 15 + 5 + 15 + 3; // cols + spaces
        String expectedSeparator = "  " + "-".repeat(expectedTotalWidth);
        assertTrue(allPrintlns.stream().anyMatch(s -> s.equals(expectedSeparator)),
            "Expected separator line not found or incorrect. Found: " + allPrintlns);

        // Check header print
        boolean headerPrinted = false;
        for (int i = 0; i < allFormats.size(); i++) {
            if (allFormats.get(i).equals(expectedFormat)) {
                Object[] args = printfArgsCaptor.getAllValues().get(i);
                if (args[0].equals("DEPENDENCY") && args[1].equals("CURRENT") && args[3].equals("LATEST")) {
                    headerPrinted = true;
                    break;
                }
            }
        }
        assertTrue(headerPrinted, "Table header was not printed correctly with the expected format.");

        // Check data print
        boolean dataPrinted = false;
        for (int i = 0; i < allFormats.size(); i++) {
            if (allFormats.get(i).equals(expectedFormat)) {
                Object[] args = printfArgsCaptor.getAllValues().get(i);
                if (args[0].equals("com.group:artifact") && args[1].equals("1.0") && args[3].equals("1.1")) {
                    dataPrinted = true;
                    break;
                }
            }
        }
        assertTrue(dataPrinted, "Dependency update data was not printed correctly with the expected format.");
    }

    // Test Case 2: Long Dependency Names
    @Test
    void testHandleSingleModuleUpdates_LongDependencyName() throws Exception {
        // Arrange
        String longGroupId = "com.example.very.long.group.id.that.exceeds.min.width";
        String longArtifactId = "my-super-duper-long-artifact-id-that-is-just-too-long";
        String depName = longGroupId + ":" + longArtifactId; // Length > 30
        Dependency dep1 = createDependency(longGroupId, longArtifactId, "1.0");
        mockProject.addDependency(dep1);
        mockProject.setParent(null);

        when(pomParser.parsePomFile(any(File.class))).thenReturn(mockProject);
        when(versionService.getLatestVersion(dep1)).thenReturn(Optional.of("1.1"));

        // Act
        checkUpdatesCommand.call();

        // Assert
        verify(colorOutput, atLeastOnce()).printf(printfFormatCaptor.capture(), printfArgsCaptor.capture());
        List<String> allFormats = printfFormatCaptor.getAllValues();

        int expectedDepColWidth = Math.max(depName.length(), "DEPENDENCY".length()); // minWidth (30) will be overridden
        String expectedFormat = String.format("  %%-%ds %%-15s %%-5s %%-15s%%n", expectedDepColWidth);

        assertTrue(allFormats.stream().anyMatch(f -> f.equals(expectedFormat)),
            "Expected format string with dynamic width for long dependency name not found. Expected: " + expectedFormat + ", Found: " + allFormats);
    }

    // Test Case 3: Long Version Strings
    @Test
    void testHandleSingleModuleUpdates_LongVersionStrings() throws Exception {
        // Arrange
        Dependency dep1 = createDependency("com.group", "artifact", "1.0.0-alpha-very-long-SNAPSHOT-version");
        String latestVersionStr = "1.1.0-beta-even-longer-latest-version-string";
        mockProject.addDependency(dep1);
        mockProject.setParent(null);

        when(pomParser.parsePomFile(any(File.class))).thenReturn(mockProject);
        when(versionService.getLatestVersion(dep1)).thenReturn(Optional.of(latestVersionStr));

        // Act
        checkUpdatesCommand.call();

        // Assert
        verify(colorOutput, atLeastOnce()).printf(printfFormatCaptor.capture(), printfArgsCaptor.capture());
        List<String> allFormats = printfFormatCaptor.getAllValues();

        int expectedCurrentVerColWidth = Math.max("1.0.0-alpha-very-long-SNAPSHOT-version".length(), "CURRENT".length()); // minWidth (15) overridden
        int expectedLatestVerColWidth = Math.max(latestVersionStr.length(), "LATEST".length()); // minWidth (15) overridden
        String expectedFormat = String.format("  %%-30s %%-%ds %%-5s %%-%ds%%n", expectedCurrentVerColWidth, expectedLatestVerColWidth);

        assertTrue(allFormats.stream().anyMatch(f -> f.equals(expectedFormat)),
            "Expected format string with dynamic width for long versions not found. Expected: " + expectedFormat + ", Found: " + allFormats);
    }

    // Test Case 4: Managed Dependencies (Null Version)
    @Test
    void testHandleSingleModuleUpdates_ManagedDependency() throws Exception {
        // Arrange
        Dependency dep1 = createDependency("com.group", "managed-artifact", null); // Managed dependency
        mockProject.addDependency(dep1);
        mockProject.setParent(null);

        when(pomParser.parsePomFile(any(File.class))).thenReturn(mockProject);
        when(versionService.getLatestVersion(dep1)).thenReturn(Optional.of("1.5")); // Assume a latest version is found

        // Act
        checkUpdatesCommand.call();

        // Assert
        verify(colorOutput, atLeastOnce()).printf(printfFormatCaptor.capture(), printfArgsCaptor.capture());
        List<String> allFormats = printfFormatCaptor.getAllValues();
        List<Object[]> allArgs = printfArgsCaptor.getAllValues();

        // Current version displayed as "managed"
        // depColWidth = Math.max("com.group:managed-artifact".length(), 30) = 30
        // currentVerColWidth = Math.max("managed".length(), 15) = 15
        // latestVerColWidth = Math.max("1.5".length(), 15) = 15
        String expectedFormat = "  %-30s %-15s %-5s %-15s%n";
        assertTrue(allFormats.stream().anyMatch(f -> f.equals(expectedFormat)),
            "Expected format string for managed dependency not found. Found: " + allFormats);

        boolean dataPrintedCorrectly = false;
        for (int i = 0; i < allFormats.size(); i++) {
            if (allFormats.get(i).equals(expectedFormat)) {
                Object[] args = allArgs.get(i);
                if (args[0].equals("com.group:managed-artifact") && args[1].equals("managed") && args[3].equals("1.5")) {
                    dataPrintedCorrectly = true;
                    break;
                }
            }
        }
        assertTrue(dataPrintedCorrectly, "Managed dependency data not printed correctly.");
    }

    // Test Case 5: No Updates for dependencies
    @Test
    void testHandleSingleModuleUpdates_NoUpdates() throws Exception {
        // Arrange
        Dependency dep1 = createDependency("com.group", "artifact", "1.0");
        mockProject.addDependency(dep1);
        mockProject.setParent(null); // No parent for this test

        when(pomParser.parsePomFile(any(File.class))).thenReturn(mockProject);
        when(versionService.getLatestVersion(dep1)).thenReturn(Optional.of("1.0")); // Same version, so no update

        // Act
        checkUpdatesCommand.call();

        // Assert
        verify(colorOutput, atLeastOnce()).println(printlnCaptor.capture(), any(ColorOutputService.ColorType.class));
        List<String> allPrintlns = printlnCaptor.getAllValues();

        assertTrue(allPrintlns.stream().anyMatch(s -> s.contains("All dependencies are up to date.")),
            "Expected 'All dependencies are up to date.' message not found. Found: " + allPrintlns);

        // Check that no dependency update data rows were printed using printf, beyond the header.
        // The header for dependencies is always printed if there are dependencies.
        // We expect 1 call to printf for the header.
        verify(colorOutput, times(1)).printf(anyString(), eq("DEPENDENCY"), eq("CURRENT"), anyString(), eq("LATEST"));
    }

    // Test Case 6: Parent Update Only
    @Test
    void testHandleSingleModuleUpdates_ParentUpdateOnly() throws Exception {
        // Arrange
        Dependency dep1 = createDependency("com.group", "artifact", "1.0");
        mockProject.addDependency(dep1);

        Project.Parent parent = createParent("com.parent", "parent-artifact", "2.0");
        mockProject.setParent(parent);

        when(pomParser.parsePomFile(any(File.class))).thenReturn(mockProject);
        when(versionService.getLatestVersion(dep1)).thenReturn(Optional.of("1.0")); // Dependency is up-to-date
        when(versionService.getLatestParentVersion(parent)).thenReturn(Optional.of("2.1")); // Parent has update

        // Act
        checkUpdatesCommand.call();

        // Assert
        verify(colorOutput, atLeastOnce()).println(printlnCaptor.capture(), any(ColorOutputService.ColorType.class));
        verify(colorOutput, atLeastOnce()).printf(printfFormatCaptor.capture(), printfArgsCaptor.capture());

        List<String> allPrintlns = printlnCaptor.getAllValues();
        assertTrue(allPrintlns.stream().anyMatch(s -> s.contains("All dependencies are up to date.")),
            "Expected 'All dependencies are up to date.' message not found.");

        List<String> allFormats = printfFormatCaptor.getAllValues();
        List<Object[]> allArgs = printfArgsCaptor.getAllValues();

        // Parent table format (similar to basic dependency, assuming short names for parent)
        // parentColWidth = Math.max(Math.max(("com.parent:parent-artifact").length(), 30), "PARENT".length()) = 30
        // parentCurrentVerColWidth = Math.max(Math.max("2.0".length(), 15), "CURRENT".length()) = 15
        // parentLatestVerColWidth = Math.max(Math.max("2.1".length(), 15), "LATEST".length()) = 15
        String expectedParentFormat = "  %-30s %-15s %-5s %-15s%n";

        boolean parentHeaderPrinted = false;
        boolean parentDataPrinted = false;

        for (int i = 0; i < allFormats.size(); i++) {
            if (allFormats.get(i).equals(expectedParentFormat)) {
                Object[] args = allArgs.get(i);
                if (args[0].equals("PARENT") && args[1].equals("CURRENT") && args[3].equals("LATEST")) {
                    parentHeaderPrinted = true;
                }
                if (args[0].equals("com.parent:parent-artifact") && args[1].equals("2.0") && args[3].equals("2.1")) {
                    parentDataPrinted = true;
                }
            }
        }
        assertTrue(parentHeaderPrinted, "Parent update table header was not printed correctly.");
        assertTrue(parentDataPrinted, "Parent update data was not printed correctly.");
    }

    // TODO: Add test cases for multi-module scenarios:
    // Test Case 7: Consolidated Dependencies - Mixed Lengths & Affected Modules (for multi-module)
    @Test
    void testHandleMultiModuleUpdates_ConsolidatedDependencies_MixedLengths() throws Exception {
        // Arrange
        // Force multi-module mode for this test
        checkUpdatesCommand = new CheckUpdatesCommand(pomParser, versionService, dependencyCollector, colorOutput);
        new CommandLine(checkUpdatesCommand).parseArgs("--force-multi-module", "dummy.pom");


        Project rootProject = new Project("com.example", "root-project", "1.0.0");
        rootProject.setRelativePath("pom.xml");
        Project moduleA = new Project("com.example", "moduleA", "1.0.0");
        moduleA.setRelativePath("moduleA/pom.xml");
        List<Project> allProjects = List.of(rootProject, moduleA);

        Dependency dep1 = createDependency("com.group.long", "artifact-name-that-is-very-long", "1.0"); // Long name
        Dependency dep2 = createDependency("short:dep", "1.0.0-short", "1.1.0-also-short"); // Short versions
        Dependency dep3 = createDependency("com.group", "another-artifact", "2.0.0-very-very-long-version-string"); // Long current version

        List<Dependency> consolidatedDeps = List.of(dep1, dep2, dep3);
        MultiModuleDependencyCollector.DependencyReport mockReport = mock(MultiModuleDependencyCollector.DependencyReport.class);

        Map<String, List<String>> usageMap = Map.of(
            "com.group.long:artifact-name-that-is-very-long", List.of("moduleA", "root-project-very-long-module-name-to-test-width"),
            "short:dep", List.of("moduleA"),
            "com.group:another-artifact", List.of("root")
        );

        when(pomParser.parseMultiModuleProject(any(File.class))).thenReturn(allProjects);
        // Ensure parsePomFile is also mocked if called internally before parseMultiModuleProject for root context
        when(pomParser.parsePomFile(any(File.class))).thenReturn(rootProject);


        when(dependencyCollector.collectDependencies(allProjects)).thenReturn(mockReport);
        when(mockReport.getConsolidatedDependencies()).thenReturn(consolidatedDeps);
        when(mockReport.getDependencyUsageByModule()).thenReturn(usageMap);

        when(versionService.getLatestVersion(dep1)).thenReturn(Optional.of("1.1")); // Update for long name dep
        when(versionService.getLatestVersion(dep2)).thenReturn(Optional.of("1.2.0-a-bit-longer-latest")); // Update for short name dep
        when(versionService.getLatestVersion(dep3)).thenReturn(Optional.of("2.1")); // Update for long current version dep

        // Act
        checkUpdatesCommand.call();

        // Assert
        verify(colorOutput, atLeastOnce()).printf(printfFormatCaptor.capture(), printfArgsCaptor.capture());
        List<String> allFormats = printfFormatCaptor.getAllValues();

        // Calculate expected widths based on the data
        int dep1NameLen = "com.group.long:artifact-name-that-is-very-long".length();
        int dep2NameLen = "short:dep".length();
        int dep3NameLen = "com.group:another-artifact".length();
        int maxDepLen = Math.max(Math.max(dep1NameLen, dep2NameLen), dep3NameLen);
        maxDepLen = Math.max(maxDepLen, "DEPENDENCY".length()); // Header
        maxDepLen = Math.max(maxDepLen, 30); // Min width

        int dep1VerLen = "1.0".length();
        int dep2VerLen = "1.0.0-short".length();
        int dep3VerLen = "2.0.0-very-very-long-version-string".length();
        int maxCurrentVerLen = Math.max(Math.max(dep1VerLen, dep2VerLen), dep3VerLen);
        maxCurrentVerLen = Math.max(maxCurrentVerLen, "CURRENT".length());
        maxCurrentVerLen = Math.max(maxCurrentVerLen, 15);

        int dep1LatestLen = "1.1".length();
        int dep2LatestLen = "1.2.0-a-bit-longer-latest".length();
        int dep3LatestLen = "2.1".length();
        int maxLatestVerLen = Math.max(Math.max(dep1LatestLen, dep2LatestLen), dep3LatestLen);
        maxLatestVerLen = Math.max(maxLatestVerLen, "LATEST".length());
        maxLatestVerLen = Math.max(maxLatestVerLen, 15);

        int affectedModules1Len = "moduleA, root-project-very-long-module-name-to-test-width".length();
        int affectedModules2Len = "moduleA".length();
        int affectedModules3Len = "root".length();
        int maxAffectedLen = Math.max(Math.max(affectedModules1Len, affectedModules2Len), affectedModules3Len);
        maxAffectedLen = Math.max(maxAffectedLen, "AFFECTED MODULES".length());
        maxAffectedLen = Math.max(maxAffectedLen, 20);

        String expectedFormat = String.format("  %%-%ds %%-%ds %%-5s %%-%ds (Modules: %%-%ds)%%n",
            maxDepLen, maxCurrentVerLen, maxLatestVerLen, maxAffectedLen);

        assertTrue(allFormats.stream().anyMatch(f -> f.equals(expectedFormat)),
            "Expected format for consolidated dependencies not found. Expected: " + expectedFormat + ". Found: " + allFormats);
    }

    // Test Case 8: Parent Updates in Multi-Module - Mixed Lengths
    @Test
    void testHandleMultiModuleUpdates_ParentUpdates_MixedLengths() throws Exception {
        // Arrange
        checkUpdatesCommand = new CheckUpdatesCommand(pomParser, versionService, dependencyCollector, colorOutput);
        new CommandLine(checkUpdatesCommand).parseArgs("--force-multi-module", "dummy.pom");

        Project rootProject = new Project("com.example", "root-project", "1.0.0");
        rootProject.setRelativePath("pom.xml");
        Project.Parent rootParent = createParent("com.root.parent", "parent-artifact-long-name", "1.0");
        rootProject.setParent(rootParent);

        Project moduleA = new Project("com.example", "moduleA-short", "1.0.0"); // Short module name
        moduleA.setRelativePath("moduleA/pom.xml");
        Project.Parent moduleAParent = createParent("com.moduleA", "parentA", "2.0.0-long-version"); // Long parent version
        moduleA.setParent(moduleAParent);

        List<Project> allProjects = List.of(rootProject, moduleA);
        MultiModuleDependencyCollector.DependencyReport mockReport = mock(MultiModuleDependencyCollector.DependencyReport.class);
        when(mockReport.getConsolidatedDependencies()).thenReturn(Collections.emptyList()); // No dependency updates for this test

        when(pomParser.parseMultiModuleProject(any(File.class))).thenReturn(allProjects);
        when(pomParser.parsePomFile(any(File.class))).thenReturn(rootProject); // Root context for initial parse
        when(dependencyCollector.collectDependencies(allProjects)).thenReturn(mockReport);

        when(versionService.getLatestParentVersion(rootParent)).thenReturn(Optional.of("1.1")); // Update for root parent
        when(versionService.getLatestParentVersion(moduleAParent)).thenReturn(Optional.of("2.1")); // Update for moduleA parent

        // Act
        checkUpdatesCommand.call();

        // Assert
        verify(colorOutput, atLeastOnce()).printf(printfFormatCaptor.capture(), printfArgsCaptor.capture());
        List<String> allFormats = printfFormatCaptor.getAllValues();

        // Calculate expected widths
        int rootModuleNameLen = "root-project".length();
        int moduleANameLen = "moduleA-short".length();
        int maxModuleLen = Math.max(rootModuleNameLen, moduleANameLen);
        maxModuleLen = Math.max(maxModuleLen, "MODULE".length());
        maxModuleLen = Math.max(maxModuleLen, 20); // minModuleLen

        int rootParentNameLen = "com.root.parent:parent-artifact-long-name".length();
        int moduleAParentNameLen = "com.moduleA:parentA".length();
        int maxParentNameLen = Math.max(rootParentNameLen, moduleAParentNameLen);
        maxParentNameLen = Math.max(maxParentNameLen, "PARENT".length());
        maxParentNameLen = Math.max(maxParentNameLen, 30); // minParentLen

        int rootParentVerLen = "1.0".length();
        int moduleAParentVerLen = "2.0.0-long-version".length();
        int maxParentVerLen = Math.max(rootParentVerLen, moduleAParentVerLen);
        maxParentVerLen = Math.max(maxParentVerLen, "CURRENT".length());
        maxParentVerLen = Math.max(maxParentVerLen, 15); // minParentCurrentVerLen

        int rootParentLatestLen = "1.1".length();
        int moduleAParentLatestLen = "2.1".length();
        int maxParentLatestVerLen = Math.max(rootParentLatestLen, moduleAParentLatestLen);
        maxParentLatestVerLen = Math.max(maxParentLatestVerLen, "LATEST".length());
        maxParentLatestVerLen = Math.max(maxParentLatestVerLen, 15); // minParentLatestVerLen

        String expectedFormat = String.format("  %%-%ds %%-%ds %%-%ds %%-5s %%-%ds%%n",
            maxModuleLen, maxParentNameLen, maxParentVerLen, maxParentLatestVerLen);

        assertTrue(allFormats.stream().anyMatch(f -> f.equals(expectedFormat)),
            "Expected format for multi-module parent updates not found. Expected: " + expectedFormat + ". Found: " + allFormats);
    }
}
