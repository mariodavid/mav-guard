package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import de.diedavids.mavguard.xml.MultiModuleDependencyCollector;
import de.diedavids.mavguard.xml.PomParser; // Changed from PomFileProcessor
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CheckUpdatesCommand.class);

    private final PomParser pomParser;
    private final DependencyVersionService versionService;
    private final MultiModuleDependencyCollector dependencyCollector;

    @Parameters(index = "0", description = "Path to the POM file")
    private String filePath;

    @Option(names = "--force-multi-module", description = "Force parsing as a multi-module project (auto-detected by default)")
    private boolean forceMultiModule = false;

    public CheckUpdatesCommand(PomParser pomParser, DependencyVersionService versionService, MultiModuleDependencyCollector dependencyCollector) {
        // Ensure MultiModuleDependencyCollector is initialized if it's not a Spring bean by default
        this.pomParser = pomParser;
        this.versionService = versionService;
        this.dependencyCollector = dependencyCollector != null ? dependencyCollector : new MultiModuleDependencyCollector();
    }

    @Override
    public Integer call() {
        log.atInfo()
            .addKeyValue("filePath", filePath)
            .addKeyValue("forceMultiModule", forceMultiModule)
            .log("Starting check-updates command execution");
        
        File file = new File(filePath);
        if (!file.exists()) {
            log.atError()
                .addKeyValue("filePath", filePath)
                .log("POM file not found");
            System.err.println("File not found: " + filePath);
            return 1;
        }

        try {
            Project initialProject = pomParser.parsePomFile(file); // Parse the given POM first
            boolean isActuallyMultiModule = initialProject.isMultiModule() || forceMultiModule;
            
            log.atDebug()
                .addKeyValue("projectCoordinates", initialProject.getCoordinates())
                .addKeyValue("isMultiModule", isActuallyMultiModule)
                .log("Analyzed project type for update check");

            if (isActuallyMultiModule) {
                // This will re-parse the root, but ensures all modules are loaded
                List<Project> allProjects = pomParser.parseMultiModuleProject(file);
                 // Try to find the initialProject in the allProjects list to use as the root context
                Project rootContext = allProjects.stream()
                                       .filter(p -> p.getCoordinates().equals(initialProject.getCoordinates()) &&
                                                    (p.relativePath() != null && p.relativePath().equals(initialProject.relativePath())))
                                       .findFirst()
                                       .orElse(initialProject); // Fallback to initialProject

                System.out.println("--- Project Analysis (Multi-Module): " + rootContext.getCoordinates() + " ---");
                analyzeMultiModuleOutput(allProjects, rootContext); // Display analysis
                System.out.println("\n--- Update Check Results ---");
                return handleMultiModuleUpdates(allProjects, rootContext);
            } else {
                System.out.println("--- Project Analysis (Single Module): " + initialProject.getCoordinates() + " ---");
                analyzeSingleModuleOutput(initialProject); // Display analysis
                System.out.println("\n--- Update Check Results ---");
                return handleSingleModuleUpdates(initialProject);
            }
        } catch (JAXBException e) {
            log.atError()
                .addKeyValue("filePath", filePath)
                .log("Error parsing POM file: {}", e.getMessage(), e);
            System.err.println("Error parsing POM file: " + filePath);
            System.err.println("Details: " + e.getMessage());
            System.err.println("Please ensure the file is a valid Maven POM XML file and the path is correct.");
            return 1;
        } catch (Exception e) {
            log.atError()
                .addKeyValue("filePath", filePath)
                .log("Unexpected error during check-updates command: {}", e.getMessage(), e);
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Uncomment for full stack trace
            return 1;
        }
    }

    // --- Analysis Display Methods (Adapted from AnalyzeCommand) ---
    // TODO: Refactor these display methods into a shared DisplayService or similar to avoid duplication.
    private void analyzeSingleModuleOutput(Project project) {
        System.out.printf("Project: %s%n", project.getCoordinates());
        if (project.name() != null && !project.name().isEmpty()) {
             System.out.printf("  Name: %s%n", project.name());
        }
        if (project.hasParent()) {
            Project.Parent parent = project.parent();
            System.out.printf("  Parent: %s%n", parent.getCoordinates());
        } else {
            System.out.printf("  Parent: None%n");
        }
        List<Dependency> dependencies = project.getAllDependencies();
        if (dependencies.isEmpty()) {
            System.out.println("\n  No dependencies found (direct or managed).");
        } else {
            System.out.println("\n  Dependencies (" + dependencies.size() + "):");
            for (Dependency dependency : dependencies) {
                System.out.println("- " + dependency);
            }
        }
    }

    private void analyzeMultiModuleOutput(List<Project> allProjectsInBuild, Project rootProjectContext) {
        System.out.printf("Root Project: %s%n", rootProjectContext.getCoordinates());
         if (rootProjectContext.name() != null && !rootProjectContext.name().isEmpty()) {
             System.out.printf("  Name: %s%n", rootProjectContext.name());
        }
        if (rootProjectContext.hasParent()) {
            Project.Parent parent = rootProjectContext.parent();
            System.out.printf("  Root Parent: %s%n", parent.getCoordinates());
        }

        System.out.println("\n  Modules (" + allProjectsInBuild.size() + "):");
        for(Project project : allProjectsInBuild) {
            System.out.printf("  - %s (Path: %s)%n", project.getCoordinates(), project.relativePath());
        }

        MultiModuleDependencyCollector.DependencyReport report = dependencyCollector.collectDependencies(allProjectsInBuild);
        List<Dependency> consolidatedDependencies = report.getConsolidatedDependencies();
        System.out.println("\n  Consolidated Dependencies (" + consolidatedDependencies.size() + " unique):");
        for (Dependency dependency : consolidatedDependencies) {
            System.out.println("  - " + dependency);
        }

        if (report.hasVersionInconsistencies()) {
            System.out.println("\n  WARNING: Version Inconsistencies Found!");
            System.out.println("  ---------------------------------------");
            for (MultiModuleDependencyCollector.VersionInconsistency inconsistency : report.getVersionInconsistencies()) {
                System.out.println("  - " + inconsistency.toString());
            }
            System.out.println("  ---------------------------------------");
        } else {
            System.out.println("\n  No version inconsistencies found across modules.");
        }
    }
    // --- End of Analysis Display Methods ---


    private Integer handleSingleModuleUpdates(Project project) throws Exception {
        List<Dependency> dependencies = project.getAllDependencies();
        boolean updatesAvailable = false;
        int updateCount = 0;

        if (!dependencies.isEmpty()) {
            System.out.println("\nDependency Updates Available:");
            System.out.printf("  %-50s %-20s %-5s %-20s%n", "DEPENDENCY", "CURRENT", " ", "LATEST");
            System.out.println("  " + "-".repeat(97));
            boolean depHeaderPrinted = false;
            for (Dependency dependency : dependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    updatesAvailable = true;
                    updateCount++;
                    System.out.printf("  %-50s %-20s -> %-20s%n",
                        dependency.groupId() + ":" + dependency.artifactId(),
                        dependency.version(),
                        latestVersion.get());
                    depHeaderPrinted = true;
                }
            }
            if (!depHeaderPrinted) {
                 System.out.println("  All dependencies are up to date.");
            }
        } else {
            System.out.println("\nNo dependencies to check for updates.");
        }

        if (project.hasParent()) {
            Project.Parent parent = project.parent();
            System.out.println("\nParent Project Update (" + parent.getCoordinates() + "):");
            System.out.printf("  %-50s %-20s %-5s %-20s%n", "PARENT", "CURRENT", " ", "LATEST");
            System.out.println("  " + "-".repeat(97));
            Optional<String> latestParentVersion = versionService.getLatestParentVersion(parent);
            if (latestParentVersion.isPresent() && !latestParentVersion.get().equals(parent.version())) {
                updatesAvailable = true;
                updateCount++;
                System.out.printf("  %-50s %-20s -> %-20s%n",
                    parent.groupId() + ":" + parent.artifactId(),
                    parent.version(),
                    latestParentVersion.get());
            } else {
                 System.out.println("  Parent is up to date or no newer version found.");
            }
        }

        System.out.println("\n--- Summary ---");
        if (!updatesAvailable) {
            System.out.println("Project is up to date. No dependency or parent updates found.");
        } else {
            System.out.println("Found " + updateCount + " potential update(s).");
        }
        return 0;
    }

    private Integer handleMultiModuleUpdates(List<Project> projects, Project rootProjectContext) throws Exception {
        MultiModuleDependencyCollector.DependencyReport report = dependencyCollector.collectDependencies(projects);
        List<Dependency> consolidatedDependencies = report.getConsolidatedDependencies();
        boolean anyUpdatesFound = false;
        int updateCount = 0;

        if (!consolidatedDependencies.isEmpty()) {
            System.out.println("\nConsolidated Dependency Updates Available:");
            System.out.printf("  %-50s %-20s %-5s %-20s %s%n", "DEPENDENCY", "CURRENT", " ", "LATEST", "AFFECTED MODULES");
            System.out.println("  " + "-".repeat(120));
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
                    System.out.printf("  %-50s %-20s -> %-20s (Modules: %s)%n",
                        depCoord, dependency.version(), latestVersion.get(), modulesList);
                }
            }
            if (!depHeaderPrinted) {
                 System.out.println("  All consolidated dependencies are up to date.");
            }
        } else {
             System.out.println("\nNo consolidated dependencies to check for updates.");
        }

        System.out.println("\nParent Project Updates (Per Module):");
        System.out.printf("  %-20s %-50s %-20s %-5s %-20s%n", "MODULE", "PARENT", "CURRENT", " ", "LATEST");
        System.out.println("  " + "-".repeat(120));
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
                    System.out.printf("  %-20s %-50s %-20s -> %-20s%n",
                        project.artifactId(), // Module name
                        parent.groupId()+":"+parent.artifactId(), // Parent GAV
                        parent.version(), // Parent current version
                        latestParentVersion.get()); // Parent latest version
                }
            }
        }

        if (!parentHeaderPrinted && hasModulesWithParents) {
             System.out.println("  All module parents are up to date or no newer versions found.");
        } else if (!hasModulesWithParents) {
            System.out.println("  No parent projects defined in any of the modules.");
        }

        System.out.println("\n--- Summary ---");
        if (!anyUpdatesFound) {
            System.out.println("Project is up to date. No consolidated dependency or parent updates found.");
        } else {
            System.out.println("Found " + updateCount + " potential update(s) across the multi-module project.");
        }

        if (report.hasVersionInconsistencies()) {
            System.out.println("\nREMINDER: " + report.getVersionInconsistencies().size() +
                               " inconsistent dependency version(s) identified in the analysis section." +
                               " Please review them as they might affect update decisions.");
        }
        return 0;
    }
}
