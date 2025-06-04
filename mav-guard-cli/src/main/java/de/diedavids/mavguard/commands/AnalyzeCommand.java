package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.service.ColorOutputService;
import de.diedavids.mavguard.xml.MultiModuleDependencyCollector;
import de.diedavids.mavguard.xml.PomParser;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Component
@Command(name = "analyze", description = "Analyzes a Maven POM file or multi-module project", mixinStandardHelpOptions = true)
public class AnalyzeCommand implements Callable<Integer> {

    private final PomParser pomParser;
    private final MultiModuleDependencyCollector dependencyCollector;
    private final ColorOutputService colorOutput;

    @Parameters(index = "0", description = "Path to the POM file")
    private String filePath;

    @Option(names = "--detailed-usage", description = "Show detailed dependency usage by module (for multi-module projects)")
    private boolean detailedUsage = false;

    // Attempt auto-detection, but keep flag as fallback or for explicit control if needed.
    // For now, we will try to infer, making this flag less critical for the user.
    @Option(names = "--force-multi-module", description = "Force parsing as a multi-module project (auto-detected by default)")
    private boolean forceMultiModule = false;

    @Option(names = "--color", description = "When to use colors: auto (default), always, never")
    private String colorMode = "auto";


    public AnalyzeCommand(PomParser pomParser, MultiModuleDependencyCollector dependencyCollector, ColorOutputService colorOutput) {
        this.pomParser = pomParser;
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
                // If it claims to be a multi-module project (or forced), then parse fully as multi-module
                // This will re-parse the root, but ensures all modules are loaded according to parseMultiModuleProject logic
                List<Project> allProjects = pomParser.parseMultiModuleProject(file);
                colorOutput.println("Analyzing multi-module project: " + initialProject.getCoordinates(), ColorOutputService.ColorType.BLUE, ColorOutputService.ColorType.BOLD);
                // Try to find the initialProject in the allProjects list to use as the root context
                Project rootContext = allProjects.stream()
                               .filter(p -> p.getCoordinates().equals(initialProject.getCoordinates()) &&
                                            (p.relativePath() != null && p.relativePath().equals(initialProject.relativePath())))
                               .findFirst()
                               .orElse(initialProject); // Fallback to initialProject if not found (should ideally be there)
                return analyzeMultiModule(allProjects, rootContext);
            } else {
                colorOutput.println("Analyzing single module project: " + initialProject.getCoordinates(), ColorOutputService.ColorType.BLUE, ColorOutputService.ColorType.BOLD);
                return analyzeSingleModule(initialProject);
            }
        } catch (JAXBException e) {
            System.err.println("Error parsing POM file: " + filePath);
            System.err.println("Details: " + e.getMessage());
            System.err.println("Please ensure the file is a valid Maven POM XML file and the path is correct.");
            // Consider logging stack trace for debugging: e.printStackTrace();
            return 1;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // for more detailed debugging
            return 1;
        }
    }

    private Integer analyzeSingleModule(Project project) {
        colorOutput.printf("Project: %s%n", project.groupId() + ":" + project.artifactId() + ":" + project.version());

        if (project.hasParent()) {
            Project.Parent parent = project.parent();
            colorOutput.printf("Parent: %s (%s)%n", parent.getCoordinates(), parent.getCoordinates());
        } else {
            colorOutput.println("Parent: None");
        }

        List<Dependency> dependencies = project.getAllDependencies();
        if (dependencies.isEmpty()) {
            colorOutput.println("No direct dependencies found (excluding parent/managed).");
        } else {
            colorOutput.println("\nDependencies (" + dependencies.size() + "):", ColorOutputService.ColorType.BLUE);
            for (Dependency dependency : dependencies) {
                colorOutput.println("- " + dependency);
            }
        }
        return 0;
    }

    // The rootProject parameter here is the project corresponding to the initially provided pom.xml
    private Integer analyzeMultiModule(List<Project> allProjectsInBuild, Project rootProjectContext) {
        if (allProjectsInBuild.isEmpty()) {
            colorOutput.println("No modules found in multi-module project.");
            return 1;
        }

        colorOutput.printf("Root Project Context: %s%n", rootProjectContext.getCoordinates());
        if (rootProjectContext.name() != null && !rootProjectContext.name().isEmpty()) {
             colorOutput.printf("Name: %s%n", rootProjectContext.name());
        }
        if (rootProjectContext.hasParent()) {
            Project.Parent parent = rootProjectContext.parent();
            colorOutput.printf("Root Parent: %s%n", parent.getCoordinates());
        }

        colorOutput.println("\nModules included in build (" + allProjectsInBuild.size() + "):", ColorOutputService.ColorType.BLUE);
        for(Project project : allProjectsInBuild) {
            colorOutput.printf("  - Module: %s (Path: %s)%n", project.getCoordinates(), project.relativePath());
             if (project.hasParent()) {
                Project.Parent parent = project.parent();
                 // Only print parent if it's different from the root project's parent (or if module is not root)
                if (!project.getCoordinates().equals(rootProjectContext.getCoordinates()) ||
                    (rootProjectContext.hasParent() && !parent.getCoordinates().equals(rootProjectContext.parent().getCoordinates())) ||
                    !rootProjectContext.hasParent()) {
                     colorOutput.printf("    Parent: %s%n", parent.getCoordinates());
                }
            }
        }

        MultiModuleDependencyCollector.DependencyReport report = dependencyCollector.collectDependencies(allProjectsInBuild);
        List<Dependency> consolidatedDependencies = report.getConsolidatedDependencies();

        colorOutput.println("\nConsolidated Dependencies (" + consolidatedDependencies.size() + " unique across all modules):", ColorOutputService.ColorType.BLUE);
        for (Dependency dependency : consolidatedDependencies) {
            colorOutput.println("- " + dependency);
        }

        if (report.hasVersionInconsistencies()) {
            colorOutput.println("\nWARNING: Found " + report.getVersionInconsistencies().size() + " inconsistent dependency versions:", ColorOutputService.ColorType.ORANGE, ColorOutputService.ColorType.BOLD);
            for (MultiModuleDependencyCollector.VersionInconsistency inconsistency : report.getVersionInconsistencies()) {
                colorOutput.println("  - " + inconsistency.toString(), ColorOutputService.ColorType.ORANGE);
            }
        } else {
            colorOutput.println("\nNo version inconsistencies found across modules.", ColorOutputService.ColorType.GREEN);
        }

        if (detailedUsage) {
            colorOutput.println("\nDependency Usage by Module:", ColorOutputService.ColorType.BLUE);
            colorOutput.println("---------------------------");
            Map<String, List<String>> usageMap = report.getDependencyUsageByModule();
            if (usageMap.isEmpty()) {
                colorOutput.println("No dependency usage information available or dependencies are not shared/declared in modules.");
            } else {
                for (Map.Entry<String, List<String>> entry : usageMap.entrySet()) {
                    String depCoord = entry.getKey();
                    List<String> modules = entry.getValue();
                    colorOutput.println(depCoord + " - Used/Declared in modules: " + String.join(", ", modules));
                }
            }
        }
        return 0;
    }
}
