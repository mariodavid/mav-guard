package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import de.diedavids.mavguard.service.ColorOutputService;
import de.diedavids.mavguard.xml.MultiModuleDependencyCollector;
import de.diedavids.mavguard.xml.PomParser; // Changed from PomFileProcessor
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import jakarta.xml.bind.JAXBException; // For parsing errors
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Map; // For detailed usage from report
import java.util.concurrent.Callable;

@Component
@Command(
    name = "check-updates",
    description = "Analyzes project and checks for dependency updates.",
    mixinStandardHelpOptions = true
)
public class CheckUpdatesCommand implements Callable<Integer> {

    private final PomParser pomParser;
    private final DependencyVersionService versionService;
    private final MultiModuleDependencyCollector dependencyCollector;
    private final ColorOutputService colorOutput;

    @Parameters(index = "0", description = "Path to the POM file")
    private String filePath;

    @Option(names = "--force-multi-module", description = "Force parsing as a multi-module project (auto-detected by default)")
    private boolean forceMultiModule = false;

    @Option(names = "--color", description = "When to use colors: auto (default), always, never")
    private String colorMode = "auto";

    public CheckUpdatesCommand(PomParser pomParser, DependencyVersionService versionService, MultiModuleDependencyCollector dependencyCollector, ColorOutputService colorOutput) {
        // Ensure MultiModuleDependencyCollector is initialized if it's not a Spring bean by default
        this.pomParser = pomParser;
        this.versionService = versionService;
        this.dependencyCollector = dependencyCollector != null ? dependencyCollector : new MultiModuleDependencyCollector();
        this.colorOutput = colorOutput;
    }

    @Override
    public Integer call() {
        // Initialize color mode
        try {
            ColorOutputService.ColorMode mode = ColorOutputService.ColorMode.valueOf(colorMode.toUpperCase());
            colorOutput.setColorMode(mode);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid color mode: " + colorMode + ". Valid options: auto, always, never");
            return 1;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return 1;
        }

        try {
            Project initialProject = pomParser.parsePomFile(file); // Parse the given POM first
            boolean isActuallyMultiModule = initialProject.isMultiModule() || forceMultiModule;

            if (isActuallyMultiModule) {
                // This will re-parse the root, but ensures all modules are loaded
                List<Project> allProjects = pomParser.parseMultiModuleProject(file);
                 // Try to find the initialProject in the allProjects list to use as the root context
                Project rootContext = allProjects.stream()
                                       .filter(p -> p.getCoordinates().equals(initialProject.getCoordinates()) &&
                                                    (p.relativePath() != null && p.relativePath().equals(initialProject.relativePath())))
                                       .findFirst()
                                       .orElse(initialProject); // Fallback to initialProject

                colorOutput.println("--- Project Analysis (Multi-Module): " + rootContext.getCoordinates() + " ---", ColorOutputService.ColorType.BLUE, ColorOutputService.ColorType.BOLD);
                analyzeMultiModuleOutput(allProjects, rootContext); // Display analysis
                colorOutput.println("\n--- Update Check Results ---", ColorOutputService.ColorType.BLUE, ColorOutputService.ColorType.BOLD);
                return handleMultiModuleUpdates(allProjects, rootContext);
            } else {
                colorOutput.println("--- Project Analysis (Single Module): " + initialProject.getCoordinates() + " ---", ColorOutputService.ColorType.BLUE, ColorOutputService.ColorType.BOLD);
                analyzeSingleModuleOutput(initialProject); // Display analysis
                colorOutput.println("\n--- Update Check Results ---", ColorOutputService.ColorType.BLUE, ColorOutputService.ColorType.BOLD);
                return handleSingleModuleUpdates(initialProject);
            }
        } catch (JAXBException e) {
            System.err.println("Error parsing POM file: " + filePath);
            System.err.println("Details: " + e.getMessage());
            System.err.println("Please ensure the file is a valid Maven POM XML file and the path is correct.");
            // e.printStackTrace(); // Uncomment for full stack trace during debugging
            return 1;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Uncomment for full stack trace
            return 1;
        }
    }

    // --- Analysis Display Methods (Adapted from AnalyzeCommand) ---
    // TODO: Refactor these display methods into a shared DisplayService or similar to avoid duplication.
    private void analyzeSingleModuleOutput(Project project) {
        colorOutput.printf("Project: %s%n", project.getCoordinates());
        if (project.name() != null && !project.name().isEmpty()) {
             colorOutput.printf("  Name: %s%n", project.name());
        }
        if (project.hasParent()) {
            Project.Parent parent = project.parent();
            colorOutput.printf("  Parent: %s%n", parent.getCoordinates());
        } else {
            colorOutput.printf("  Parent: None%n");
        }
        List<Dependency> dependencies = project.getAllDependencies();
        if (dependencies.isEmpty()) {
            colorOutput.println("\n  No dependencies found (direct or managed).");
        } else {
            colorOutput.println("\n  Dependencies (" + dependencies.size() + "):", ColorOutputService.ColorType.BLUE);
            for (Dependency dependency : dependencies) {
                colorOutput.println("- " + dependency);
            }
        }
    }

    private void analyzeMultiModuleOutput(List<Project> allProjectsInBuild, Project rootProjectContext) {
        colorOutput.printf("Root Project: %s%n", rootProjectContext.getCoordinates());
         if (rootProjectContext.name() != null && !rootProjectContext.name().isEmpty()) {
             colorOutput.printf("  Name: %s%n", rootProjectContext.name());
        }
        if (rootProjectContext.hasParent()) {
            Project.Parent parent = rootProjectContext.parent();
            colorOutput.printf("  Root Parent: %s%n", parent.getCoordinates());
        }

        colorOutput.println("\n  Modules (" + allProjectsInBuild.size() + "):", ColorOutputService.ColorType.BLUE);
        for(Project project : allProjectsInBuild) {
            colorOutput.printf("  - %s (Path: %s)%n", project.getCoordinates(), project.relativePath());
        }

        MultiModuleDependencyCollector.DependencyReport report = dependencyCollector.collectDependencies(allProjectsInBuild);
        List<Dependency> consolidatedDependencies = report.getConsolidatedDependencies();
        colorOutput.println("\n  Consolidated Dependencies (" + consolidatedDependencies.size() + " unique):", ColorOutputService.ColorType.BLUE);
        for (Dependency dependency : consolidatedDependencies) {
            colorOutput.println("  - " + dependency);
        }

        if (report.hasVersionInconsistencies()) {
            colorOutput.println("\n  WARNING: Version Inconsistencies Found!", ColorOutputService.ColorType.ORANGE, ColorOutputService.ColorType.BOLD);
            colorOutput.println("  ---------------------------------------");
            for (MultiModuleDependencyCollector.VersionInconsistency inconsistency : report.getVersionInconsistencies()) {
                colorOutput.println("  - " + inconsistency.toString(), ColorOutputService.ColorType.ORANGE);
            }
            colorOutput.println("  ---------------------------------------");
        } else {
            colorOutput.println("\n  No version inconsistencies found across modules.", ColorOutputService.ColorType.GREEN);
        }
    }
    // --- End of Analysis Display Methods ---


    private Integer handleSingleModuleUpdates(Project project) throws Exception {
        List<Dependency> dependencies = project.getAllDependencies();
        boolean updatesAvailable = false;
        int updateCount = 0;

        if (!dependencies.isEmpty()) {
            colorOutput.println("\nDependency Updates Available:", ColorOutputService.ColorType.BLUE);
            colorOutput.printf("  %-50s %-20s %-5s %-20s%n", "DEPENDENCY", "CURRENT", " ", "LATEST");
            colorOutput.println("  " + "-".repeat(97));
            boolean depHeaderPrinted = false;
            for (Dependency dependency : dependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    updatesAvailable = true;
                    updateCount++;
                    String arrow = colorOutput.getUpdateArrow(dependency.version(), latestVersion.get());
                    colorOutput.printf("  %-50s %-20s %s %-20s%n",
                        dependency.groupId() + ":" + dependency.artifactId(),
                        dependency.version(),
                        arrow,
                        latestVersion.get());
                    depHeaderPrinted = true;
                }
            }
            if (!depHeaderPrinted) {
                 colorOutput.println("  All dependencies are up to date.", ColorOutputService.ColorType.GREEN);
            }
        } else {
            colorOutput.println("\nNo dependencies to check for updates.");
        }

        if (project.hasParent()) {
            Project.Parent parent = project.parent();
            colorOutput.println("\nParent Project Update (" + parent.getCoordinates() + "):", ColorOutputService.ColorType.BLUE);
            colorOutput.printf("  %-50s %-20s %-5s %-20s%n", "PARENT", "CURRENT", " ", "LATEST");
            colorOutput.println("  " + "-".repeat(97));
            Optional<String> latestParentVersion = versionService.getLatestParentVersion(parent);
            if (latestParentVersion.isPresent() && !latestParentVersion.get().equals(parent.version())) {
                updatesAvailable = true;
                updateCount++;
                String arrow = colorOutput.getUpdateArrow(parent.version(), latestParentVersion.get());
                colorOutput.printf("  %-50s %-20s %s %-20s%n",
                    parent.groupId() + ":" + parent.artifactId(),
                    parent.version(),
                    arrow,
                    latestParentVersion.get());
            } else {
                 colorOutput.println("  Parent is up to date or no newer version found.", ColorOutputService.ColorType.GREEN);
            }
        }

        colorOutput.println("\n--- Summary ---", ColorOutputService.ColorType.BLUE, ColorOutputService.ColorType.BOLD);
        if (!updatesAvailable) {
            colorOutput.println("Project is up to date. No dependency or parent updates found.", ColorOutputService.ColorType.GREEN);
        } else {
            colorOutput.println("Found " + updateCount + " potential update(s).", ColorOutputService.ColorType.YELLOW);
        }
        return 0;
    }

    private Integer handleMultiModuleUpdates(List<Project> projects, Project rootProjectContext) throws Exception {
        MultiModuleDependencyCollector.DependencyReport report = dependencyCollector.collectDependencies(projects);
        List<Dependency> consolidatedDependencies = report.getConsolidatedDependencies();
        boolean anyUpdatesFound = false;
        int updateCount = 0;

        if (!consolidatedDependencies.isEmpty()) {
            colorOutput.println("\nConsolidated Dependency Updates Available:", ColorOutputService.ColorType.BLUE);
            colorOutput.printf("  %-50s %-20s %-5s %-20s %s%n", "DEPENDENCY", "CURRENT", " ", "LATEST", "AFFECTED MODULES");
            colorOutput.println("  " + "-".repeat(120));
            boolean depHeaderPrinted = false;
            for (Dependency dependency : consolidatedDependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    anyUpdatesFound = true;
                    updateCount++;
                    depHeaderPrinted = true;
                    String depCoord = dependency.groupId() + ":" + dependency.artifactId();
                    Map<String, List<String>> usageMap = report.getDependencyUsageByModule();
                    List<String> usingModules = usageMap != null ? usageMap.get(depCoord) : null;
                    String modulesList = (usingModules != null && !usingModules.isEmpty()) ? String.join(", ", usingModules) : "root/inherited";
                    String arrow = colorOutput.getUpdateArrow(dependency.version(), latestVersion.get());
                    String currentVersionDisplay = dependency.version() != null ? dependency.version() : "managed";
                    colorOutput.printf("  %-50s %-20s %s %-20s (Modules: %s)%n",
                        depCoord, currentVersionDisplay, arrow, latestVersion.get(), modulesList);
                }
            }
            if (!depHeaderPrinted) {
                 colorOutput.println("  All consolidated dependencies are up to date.", ColorOutputService.ColorType.GREEN);
            }
        } else {
             colorOutput.println("\nNo consolidated dependencies to check for updates.");
        }

        colorOutput.println("\nParent Project Updates (Per Module):", ColorOutputService.ColorType.BLUE);
        colorOutput.printf("  %-20s %-50s %-20s %-5s %-20s%n", "MODULE", "PARENT", "CURRENT", " ", "LATEST");
        colorOutput.println("  " + "-".repeat(120));
        boolean parentHeaderPrinted = false;
        boolean hasModulesWithParents = false;
        for (Project project : projects) {
            if (project.hasParent()) {
                hasModulesWithParents = true;
                Project.Parent parent = project.parent();
                Optional<String> latestParentVersion = versionService.getLatestParentVersion(parent);
                if (latestParentVersion.isPresent() && !latestParentVersion.get().equals(parent.version())) {
                    anyUpdatesFound = true;
                    updateCount++;
                    parentHeaderPrinted = true;
                    String arrow = colorOutput.getUpdateArrow(parent.version(), latestParentVersion.get());
                    colorOutput.printf("  %-20s %-50s %-20s %s %-20s%n",
                        project.artifactId(), // Module name
                        parent.groupId()+":"+parent.artifactId(), // Parent GAV
                        parent.version(), // Parent current version
                        arrow, // Colored arrow
                        latestParentVersion.get()); // Parent latest version
                }
            }
        }

        if (!parentHeaderPrinted && hasModulesWithParents) {
             colorOutput.println("  All module parents are up to date or no newer versions found.", ColorOutputService.ColorType.GREEN);
        } else if (!hasModulesWithParents) {
            colorOutput.println("  No parent projects defined in any of the modules.");
        }

        colorOutput.println("\n--- Summary ---", ColorOutputService.ColorType.BLUE, ColorOutputService.ColorType.BOLD);
        if (!anyUpdatesFound) {
            colorOutput.println("Project is up to date. No consolidated dependency or parent updates found.", ColorOutputService.ColorType.GREEN);
        } else {
            colorOutput.println("Found " + updateCount + " potential update(s) across the multi-module project.", ColorOutputService.ColorType.YELLOW);
        }

        if (report.hasVersionInconsistencies()) {
            colorOutput.println("\nREMINDER: " + report.getVersionInconsistencies().size() +
                               " inconsistent dependency version(s) identified in the analysis section." +
                               " Please review them as they might affect update decisions.", ColorOutputService.ColorType.ORANGE);
        }
        return 0;
    }
}
