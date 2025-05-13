package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
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

/**
 * Command line interface for XML parsing operations.
 */
@Component
@Command(
    name = "xml", 
    description = "XML parsing operations", 
    mixinStandardHelpOptions = true,
    subcommands = {
        XmlParserCommands.ParsePomCommand.class,
        XmlParserCommands.ExtractDependenciesCommand.class,
        XmlParserCommands.AnalyzeMultiModuleCommand.class
    }
)
public class XmlParserCommands {

    private final PomParser pomParser;
    private final MultiModuleDependencyCollector dependencyCollector;

    public XmlParserCommands(PomParser pomParser) {
        this.pomParser = pomParser;
        this.dependencyCollector = new MultiModuleDependencyCollector();
    }

    /**
     * Command to parse a Maven POM file.
     */
    @Component
    @Command(name = "parse-pom", description = "Parse a Maven POM file", mixinStandardHelpOptions = true)
    public class ParsePomCommand implements Callable<Integer> {

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
                System.out.printf("Successfully parsed POM file: %s:%s:%s%n", 
                        project.groupId(), project.artifactId(), project.version());
                return 0;
            } catch (JAXBException e) {
                System.err.println("Error parsing POM: " + e.getMessage());
                return 1;
            }
        }
    }

    /**
     * Command to extract dependencies from a Maven POM file.
     */
    @Component
    @Command(name = "extract-dependencies", description = "Extract dependencies from a Maven POM file", mixinStandardHelpOptions = true)
    public class ExtractDependenciesCommand implements Callable<Integer> {

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
                    return handleMultiModuleExtraction(file);
                } else {
                    return handleSingleModuleExtraction(file);
                }
            } catch (JAXBException e) {
                System.err.println("Error extracting dependencies: " + e.getMessage());
                return 1;
            }
        }

        private Integer handleSingleModuleExtraction(File file) throws JAXBException {
            Project project = pomParser.parsePomFile(file);
            List<Dependency> dependencies = project.getAllDependencies();
            if (dependencies.isEmpty()) {
                System.out.println("No dependencies found in POM file.");
                return 0;
            }

            System.out.println("Dependencies found in POM file:");
            for (Dependency dependency : dependencies) {
                System.out.println("- " + dependency);
            }
            return 0;
        }

        private Integer handleMultiModuleExtraction(File file) throws JAXBException {
            List<Project> projects = pomParser.parseMultiModuleProject(file);
            if (projects.isEmpty()) {
                System.out.println("No projects found in multi-module project.");
                return 1;
            }

            System.out.println("Multi-module project with " + projects.size() + " modules:");
            for (Project project : projects) {
                System.out.printf("- %s:%s:%s%n", project.groupId(), project.artifactId(), project.version());
            }

            MultiModuleDependencyCollector.DependencyReport report = dependencyCollector.collectDependencies(projects);
            List<Dependency> dependencies = report.getConsolidatedDependencies();
            
            System.out.println("\nConsolidated dependencies across all modules:");
            for (Dependency dependency : dependencies) {
                System.out.println("- " + dependency);
            }

            if (report.hasVersionInconsistencies()) {
                System.out.println("\nWARNING: Found inconsistent dependency versions:");
                for (MultiModuleDependencyCollector.VersionInconsistency inconsistency : report.getVersionInconsistencies()) {
                    System.out.println(inconsistency.toString());
                }
            }

            return 0;
        }
    }

    /**
     * Command to analyze a multi-module Maven project.
     */
    @Component
    @Command(name = "analyze-multi-module", description = "Analyze a multi-module Maven project", mixinStandardHelpOptions = true)
    public class AnalyzeMultiModuleCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Path to the root POM file")
        private String filePath;

        @Option(names = "--check-inconsistencies", description = "Check for version inconsistencies only")
        private boolean checkInconsistencies = false;

        @Option(names = "--detailed-usage", description = "Show detailed dependency usage by module")
        private boolean detailedUsage = false;

        @Override
        public Integer call() {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("File not found: " + filePath);
                    return 1;
                }

                List<Project> projects = pomParser.parseMultiModuleProject(file);
                if (projects.isEmpty()) {
                    System.out.println("No projects found in multi-module project.");
                    return 1;
                }

                MultiModuleDependencyCollector.DependencyReport report = dependencyCollector.collectDependencies(projects);
                
                // Print summary
                System.out.println(report.getSummary());
                
                // Show detailed dependency usage by module if requested
                if (detailedUsage) {
                    System.out.println("Dependency Usage by Module:");
                    System.out.println("---------------------------");
                    
                    Map<String, List<String>> usageMap = report.getDependencyUsageByModule();
                    for (Map.Entry<String, List<String>> entry : usageMap.entrySet()) {
                        String depCoord = entry.getKey();
                        List<String> modules = entry.getValue();
                        
                        System.out.println(depCoord + " - Used in modules: " + String.join(", ", modules));
                    }
                }
                
                // Return non-zero exit code if inconsistencies are found and we're checking for them
                if (checkInconsistencies && report.hasVersionInconsistencies()) {
                    return 1;
                }
                
                return 0;
            } catch (JAXBException e) {
                System.err.println("Error analyzing multi-module project: " + e.getMessage());
                return 1;
            }
        }
    }
}
