package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import de.diedavids.mavguard.xml.MultiModuleDependencyCollector;
import de.diedavids.mavguard.xml.PomFileProcessor;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Command line interface for dependency operations.
 */
@Component
@Command(
    name = "dependencies",
    description = "Dependency operations",
    mixinStandardHelpOptions = true,
    subcommands = {
        DependencyCommands.CheckUpdatesCommand.class
    }
)
public class DependencyCommands {

    private final PomFileProcessor pomParser;
    private final DependencyVersionService versionService;
    private final MultiModuleDependencyCollector dependencyCollector;

    public DependencyCommands(PomFileProcessor pomParser, DependencyVersionService versionService) {
        this.pomParser = pomParser;
        this.versionService = versionService;
        this.dependencyCollector = new MultiModuleDependencyCollector();
    }

    /**
     * Command to check for dependency updates.
     */
    @Component
    @Command(name = "check-updates", description = "Check for dependency updates", mixinStandardHelpOptions = true)
    public class CheckUpdatesCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Path to the POM file")
        private String filePath;
        
        @Option(names = "--multi-module", description = "Parse as a multi-module project")
        private boolean multiModule = false;

        @Override
        public Integer call() {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("File not found: " + filePath);
                    return 1;
                }

                if (multiModule) {
                    return handleMultiModuleUpdates(file);
                } else {
                    return handleSingleModuleUpdates(file);
                }
            
            } catch (Exception e) {
                System.err.println("Error checking for updates: " + e.getMessage());
                return 1;
            }
        }
        
        /**
         * Handles update checking for a single-module project.
         *
         * @param file the POM file to analyze
         * @return exit code (0 for success, non-zero for failure)
         * @throws Exception if there is an error during parsing or analysis
         */
        private Integer handleSingleModuleUpdates(File file) throws Exception {
            Project project = pomParser.parsePomFile(file);
            List<Dependency> dependencies = project.getAllDependencies();
            
            if (dependencies.isEmpty()) {
                System.out.println("No dependencies found in POM file.");
                return 0;
            }

            System.out.println("Checking for updates for dependencies in " + project.groupId() + ":" + project.artifactId() + ":" + project.version());
            System.out.println("-----------------------------------------------------");
            
            boolean updatesAvailable = false;
            
            for (Dependency dependency : dependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    updatesAvailable = true;
                    System.out.printf("%-40s %10s -> %10s%n", 
                        dependency.groupId() + ":" + dependency.artifactId(),
                        dependency.version(),
                        latestVersion.get());
                }
            }
            
            if (!updatesAvailable) {
                System.out.println("All dependencies are up to date.");
            }
            
            return 0;
        }
        
        /**
         * Handles update checking for a multi-module project.
         *
         * @param file the root POM file to analyze
         * @return exit code (0 for success, non-zero for failure)
         * @throws Exception if there is an error during parsing or analysis
         */
        private Integer handleMultiModuleUpdates(File file) throws Exception {
            List<Project> projects = pomParser.parseMultiModuleProject(file);
            if (projects.isEmpty()) {
                System.out.println("No projects found in multi-module project.");
                return 1;
            }
            
            MultiModuleDependencyCollector.DependencyReport report = dependencyCollector.collectDependencies(projects);
            List<Dependency> consolidatedDependencies = report.getConsolidatedDependencies();
            
            if (consolidatedDependencies.isEmpty()) {
                System.out.println("No dependencies found in multi-module project.");
                return 0;
            }
            
            // Print project summary
            Project rootProject = projects.stream()
                .filter(p -> !p.hasParent())
                .findFirst()
                .orElse(projects.get(0));
                
            System.out.println("Checking for updates for dependencies in multi-module project: " + 
                rootProject.groupId() + ":" + rootProject.artifactId() + ":" + rootProject.version());
            System.out.println("Contains " + projects.size() + " modules with " + consolidatedDependencies.size() + " unique dependencies");
            System.out.println("-----------------------------------------------------");
            
            // Check for updates for all consolidated dependencies
            boolean updatesAvailable = false;
            
            for (Dependency dependency : consolidatedDependencies) {
                Optional<String> latestVersion = versionService.getLatestVersion(dependency);
                
                if (latestVersion.isPresent() && !latestVersion.get().equals(dependency.version())) {
                    updatesAvailable = true;
                    
                    // Get modules using this dependency
                    String depCoord = dependency.groupId() + ":" + dependency.artifactId();
                    List<String> usingModules = report.getDependencyUsageByModule().get(depCoord);
                    String modulesList = usingModules != null ? String.join(", ", usingModules) : "unknown";
                    
                    System.out.printf("%-40s %10s -> %10s %s%n", 
                        depCoord,
                        dependency.version(),
                        latestVersion.get(),
                        "[" + modulesList + "]");
                }
            }
            
            if (!updatesAvailable) {
                System.out.println("All dependencies are up to date.");
            }
            
            // Print version inconsistency warnings if any exist
            if (report.hasVersionInconsistencies()) {
                System.out.println("\nWARNING: Found inconsistent dependency versions:");
                for (MultiModuleDependencyCollector.VersionInconsistency inconsistency : report.getVersionInconsistencies()) {
                    System.out.println(inconsistency.toString());
                }
            }
            
            return 0;
        }
    }
}