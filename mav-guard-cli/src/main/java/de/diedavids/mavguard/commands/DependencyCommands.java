package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import de.diedavids.mavguard.xml.PomFileProcessor;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
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

    public DependencyCommands(PomFileProcessor pomParser, DependencyVersionService versionService) {
        this.pomParser = pomParser;
        this.versionService = versionService;
    }

    /**
     * Command to check for dependency updates.
     */
    @Component
    @Command(name = "check-updates", description = "Check for dependency updates", mixinStandardHelpOptions = true)
    public class CheckUpdatesCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Path to the POM file")
        private String filePath;

        @Override
        public Integer call() {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("File not found: " + filePath);
                    return 1;
                }

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
            } catch (Exception e) {
                System.err.println("Error checking for updates: " + e.getMessage());
                return 1;
            }
        }
    }
}