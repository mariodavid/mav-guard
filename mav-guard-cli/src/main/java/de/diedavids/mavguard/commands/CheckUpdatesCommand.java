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
            // Calculate dynamic column widths for dependencies
            int maxDepLen = 0;
            int maxCurrentVerLen = 0;
            int maxLatestVerLen = 0;

            for (Dependency dependency : dependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    maxDepLen = Math.max(maxDepLen, (dependency.groupId() + ":" + dependency.artifactId()).length());
                    String currentVersionDisplay = dependency.version() != null ? dependency.version() : "managed";
                    maxCurrentVerLen = Math.max(maxCurrentVerLen, currentVersionDisplay.length());
                    maxLatestVerLen = Math.max(maxLatestVerLen, latestVersion.get().length());
                }
            }

            int minDepLen = 30;
            int minCurrentVerLen = 15;
            int minLatestVerLen = 15;

            int depColWidth = Math.max(maxDepLen, minDepLen);
            int currentVerColWidth = Math.max(maxCurrentVerLen, minCurrentVerLen);
            int latestVerColWidth = Math.max(maxLatestVerLen, minLatestVerLen);

            // Ensure column widths are at least as long as headers
            depColWidth = Math.max(depColWidth, "DEPENDENCY".length());
            currentVerColWidth = Math.max(currentVerColWidth, "CURRENT".length());
            latestVerColWidth = Math.max(latestVerColWidth, "LATEST".length());

            String format = "  %-" + depColWidth + "s %-" + currentVerColWidth + "s %-5s %-" + latestVerColWidth + "s%n";
            int totalWidth = depColWidth + currentVerColWidth + 5 + latestVerColWidth + 3 * 1; // 3 spaces between columns

            colorOutput.println("\nDependency Updates Available:", ColorOutputService.ColorType.BLUE);
            colorOutput.printf(format, "DEPENDENCY", "CURRENT", " ", "LATEST");
            colorOutput.println("  " + "-".repeat(totalWidth));
            boolean depHeaderPrinted = false;
            for (Dependency dependency : dependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    updatesAvailable = true;
                    updateCount++;
                    String arrow = colorOutput.getUpdateArrow(dependency.version(), latestVersion.get());
                    String currentVersionDisplay = dependency.version() != null ? dependency.version() : "managed";
                    colorOutput.printf(format,
                        dependency.groupId() + ":" + dependency.artifactId(),
                        currentVersionDisplay,
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

            // Calculate dynamic column widths for parent
            int maxParentLen = 0;
            int maxParentCurrentVerLen = 0;
            int maxParentLatestVerLen = 0;

            Optional<String> latestParentVersionOpt = versionService.getLatestParentVersion(parent);
            if (latestParentVersionOpt.isPresent() && !latestParentVersionOpt.get().equals(parent.version())) {
                maxParentLen = Math.max(maxParentLen, (parent.groupId() + ":" + parent.artifactId()).length());
                maxParentCurrentVerLen = Math.max(maxParentCurrentVerLen, parent.version().length());
                maxParentLatestVerLen = Math.max(maxParentLatestVerLen, latestParentVersionOpt.get().length());
            }

            int minParentLen = 30; // Same as dep for consistency or can be different
            int minParentCurrentVerLen = 15;
            int minParentLatestVerLen = 15;

            int parentColWidth = Math.max(maxParentLen, minParentLen);
            int parentCurrentVerColWidth = Math.max(maxParentCurrentVerLen, minParentCurrentVerLen);
            int parentLatestVerColWidth = Math.max(maxParentLatestVerLen, minParentLatestVerLen);

            // Ensure column widths are at least as long as headers
            parentColWidth = Math.max(parentColWidth, "PARENT".length());
            parentCurrentVerColWidth = Math.max(parentCurrentVerColWidth, "CURRENT".length());
            parentLatestVerColWidth = Math.max(parentLatestVerColWidth, "LATEST".length());

            String parentFormat = "  %-" + parentColWidth + "s %-" + parentCurrentVerColWidth + "s %-5s %-" + parentLatestVerColWidth + "s%n";
            int parentTotalWidth = parentColWidth + parentCurrentVerColWidth + 5 + parentLatestVerColWidth + 3 * 1; // 3 spaces

            colorOutput.println("\nParent Project Update (" + parent.getCoordinates() + "):", ColorOutputService.ColorType.BLUE);
            colorOutput.printf(parentFormat, "PARENT", "CURRENT", " ", "LATEST");
            colorOutput.println("  " + "-".repeat(parentTotalWidth));

            if (latestParentVersionOpt.isPresent() && !latestParentVersionOpt.get().equals(parent.version())) {
                updatesAvailable = true;
                updateCount++;
                String arrow = colorOutput.getUpdateArrow(parent.version(), latestParentVersionOpt.get());
                colorOutput.printf(parentFormat,
                    parent.groupId() + ":" + parent.artifactId(),
                    parent.version(),
                    arrow,
                    latestParentVersionOpt.get());
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
            // Calculate dynamic column widths for consolidated dependencies
            int maxDepLen = 0;
            int maxCurrentVerLen = 0;
            int maxLatestVerLen = 0;
            int maxAffectedModulesLen = 0;
            Map<String, List<String>> usageMap = report.getDependencyUsageByModule();

            for (Dependency dependency : consolidatedDependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    String depCoord = dependency.groupId() + ":" + dependency.artifactId();
                    maxDepLen = Math.max(maxDepLen, depCoord.length());
                    String currentVersionDisplay = dependency.version() != null ? dependency.version() : "managed";
                    maxCurrentVerLen = Math.max(maxCurrentVerLen, currentVersionDisplay.length());
                    maxLatestVerLen = Math.max(maxLatestVerLen, latestVersion.get().length());

                    List<String> usingModules = usageMap != null ? usageMap.get(depCoord) : null;
                    String modulesList = (usingModules != null && !usingModules.isEmpty()) ? String.join(", ", usingModules) : "root/inherited";
                    maxAffectedModulesLen = Math.max(maxAffectedModulesLen, modulesList.length());
                }
            }

            int minDepLen = 30;
            int minCurrentVerLen = 15;
            int minLatestVerLen = 15;
            int minAffectedModulesLen = 20;

            int depColWidth = Math.max(maxDepLen, minDepLen);
            int currentVerColWidth = Math.max(maxCurrentVerLen, minCurrentVerLen);
            int latestVerColWidth = Math.max(maxLatestVerLen, minLatestVerLen);
            int affectedColWidth = Math.max(maxAffectedModulesLen, minAffectedModulesLen);

            depColWidth = Math.max(depColWidth, "DEPENDENCY".length());
            currentVerColWidth = Math.max(currentVerColWidth, "CURRENT".length());
            latestVerColWidth = Math.max(latestVerColWidth, "LATEST".length());
            // Header for affected modules is "(Modules: %s)" so we take "AFFECTED MODULES" as an estimate for content.
            affectedColWidth = Math.max(affectedColWidth, "AFFECTED MODULES".length());


            String format = "  %-" + depColWidth + "s %-" + currentVerColWidth + "s %-5s %-" + latestVerColWidth + "s (Modules: %-" + affectedColWidth + "s)%n";
            // Approx width: spaces + col1 + space + col2 + space + arrow + space + col3 + space + "(Modules: " + col4 + ")"
            int totalWidth = depColWidth + currentVerColWidth + 5 + latestVerColWidth + affectedColWidth + 3 * 1 + "(Modules: )".length() + 1;


            colorOutput.println("\nConsolidated Dependency Updates Available:", ColorOutputService.ColorType.BLUE);
            colorOutput.printf(format, "DEPENDENCY", "CURRENT", " ", "LATEST", "AFFECTED MODULES");
            colorOutput.println("  " + "-".repeat(totalWidth));
            boolean depHeaderPrinted = false;
            for (Dependency dependency : consolidatedDependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    anyUpdatesFound = true;
                    updateCount++;
                    depHeaderPrinted = true;
                    String depCoord = dependency.groupId() + ":" + dependency.artifactId();
                    List<String> usingModules = usageMap != null ? usageMap.get(depCoord) : null;
                    String modulesList = (usingModules != null && !usingModules.isEmpty()) ? String.join(", ", usingModules) : "root/inherited";
                    String arrow = colorOutput.getUpdateArrow(dependency.version(), latestVersion.get());
                    String currentVersionDisplay = dependency.version() != null ? dependency.version() : "managed";
                    colorOutput.printf(format,
                        depCoord, currentVersionDisplay, arrow, latestVersion.get(), modulesList);
                }
            }
            if (!depHeaderPrinted) {
                 colorOutput.println("  All consolidated dependencies are up to date.", ColorOutputService.ColorType.GREEN);
            }
        } else {
             colorOutput.println("\nNo consolidated dependencies to check for updates.");
        }

        // Calculate dynamic column widths for parent project updates
        int maxModuleLen = 0;
        int maxParentLen = 0;
        int maxParentCurrentVerLen = 0;
        int maxParentLatestVerLen = 0;
        boolean hasModulesWithParents = false;

        for (Project project : projects) {
            if (project.hasParent()) {
                hasModulesWithParents = true;
                Project.Parent parent = project.parent();
                Optional<String> latestParentVersion = versionService.getLatestParentVersion(parent);
                if (latestParentVersion.isPresent() && !latestParentVersion.get().equals(parent.version())) {
                    maxModuleLen = Math.max(maxModuleLen, project.artifactId().length());
                    maxParentLen = Math.max(maxParentLen, (parent.groupId() + ":" + parent.artifactId()).length());
                    maxParentCurrentVerLen = Math.max(maxParentCurrentVerLen, parent.version().length());
                    maxParentLatestVerLen = Math.max(maxParentLatestVerLen, latestParentVersion.get().length());
                }
            }
        }

        int minModuleLen = 20;
        int minParentLen = 30;
        int minParentCurrentVerLen = 15;
        int minParentLatestVerLen = 15;

        int moduleColWidth = Math.max(maxModuleLen, minModuleLen);
        int parentColWidth = Math.max(maxParentLen, minParentLen);
        int parentCurrentVerColWidth = Math.max(maxParentCurrentVerLen, minParentCurrentVerLen);
        int parentLatestVerColWidth = Math.max(maxParentLatestVerLen, minParentLatestVerLen);

        moduleColWidth = Math.max(moduleColWidth, "MODULE".length());
        parentColWidth = Math.max(parentColWidth, "PARENT".length());
        parentCurrentVerColWidth = Math.max(parentCurrentVerColWidth, "CURRENT".length());
        parentLatestVerColWidth = Math.max(parentLatestVerColWidth, "LATEST".length());

        String parentFormat = "  %-" + moduleColWidth + "s %-" + parentColWidth + "s %-" + parentCurrentVerColWidth + "s %-5s %-" + parentLatestVerColWidth + "s%n";
        int parentTotalWidth = moduleColWidth + parentColWidth + parentCurrentVerColWidth + 5 + parentLatestVerColWidth + 4 * 1; // 4 spaces

        colorOutput.println("\nParent Project Updates (Per Module):", ColorOutputService.ColorType.BLUE);
        colorOutput.printf(parentFormat, "MODULE", "PARENT", "CURRENT", " ", "LATEST");
        colorOutput.println("  " + "-".repeat(parentTotalWidth));
        boolean parentHeaderPrinted = false;
        // Reset hasModulesWithParents, it was used for length calculation, now for actual logic
        hasModulesWithParents = false;

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
                    colorOutput.printf(parentFormat,
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
