package de.diedavids.mavguard.xml;

import org.springframework.stereotype.Component; // Added import
import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Collects and consolidates dependencies across a multi-module project.
 */
@Component // Added annotation
public class MultiModuleDependencyCollector {

    private static final Logger log = LoggerFactory.getLogger(MultiModuleDependencyCollector.class);

    private final DependencyConflictResolver conflictResolver;

    public MultiModuleDependencyCollector() {
        this.conflictResolver = new DependencyConflictResolver();
    }

    /**
     * Returns a consolidated overview of all dependencies in a multi-module project.
     *
     * @param projects the list of projects (parent and modules)
     * @return a consolidated dependency report
     */
    public DependencyReport collectDependencies(List<Project> projects) {
        log.atDebug()
            .addKeyValue("projectCount", projects != null ? projects.size() : 0)
            .log("Starting dependency collection for multi-module project");
            
        if (projects == null || projects.isEmpty()) {
            log.atDebug()
                .log("No projects provided, returning empty dependency report");
            return new DependencyReport(new ArrayList<>(), new ArrayList<>(), new HashMap<>());
        }

        // Resolve conflicts according to Maven's "nearest wins" principle
        List<Dependency> consolidatedDependencies = conflictResolver.resolveConflicts(projects);

        // Collect modules using each dependency
        Map<String, List<String>> dependencyUsage = collectDependencyUsageByModule(projects);

        // Identify inconsistent versions across modules
        List<VersionInconsistency> versionInconsistencies = identifyVersionInconsistencies(projects);

        log.atInfo()
            .addKeyValue("projectCount", projects.size())
            .addKeyValue("consolidatedDependencies", consolidatedDependencies.size())
            .addKeyValue("versionInconsistencies", versionInconsistencies.size())
            .log("Dependency collection completed");

        return new DependencyReport(consolidatedDependencies, versionInconsistencies, dependencyUsage);
    }

    /**
     * Collects information about which modules use each dependency.
     *
     * @param projects the list of projects
     * @return a map of dependency coordinates to lists of module names
     */
    private Map<String, List<String>> collectDependencyUsageByModule(List<Project> projects) {
        Map<String, List<String>> usageMap = new HashMap<>();

        for (Project project : projects) {
            String moduleName = project.artifactId();
            
            // Add direct dependencies
            for (Dependency dependency : project.dependencies()) {
                String depCoord = dependency.groupId() + ":" + dependency.artifactId();
                usageMap.computeIfAbsent(depCoord, k -> new ArrayList<>()).add(moduleName);
            }
        }

        return usageMap;
    }

    /**
     * Identifies dependencies that have inconsistent versions across different modules.
     *
     * @param projects the list of projects
     * @return a list of version inconsistencies
     */
    private List<VersionInconsistency> identifyVersionInconsistencies(List<Project> projects) {
        // Map to track versions for each dependency across modules
        Map<String, Map<String, List<String>>> versionsByDependency = new TreeMap<>();

        for (Project project : projects) {
            String moduleName = project.artifactId();
            
            // Check direct dependencies
            for (Dependency dependency : project.dependencies()) {
                String depCoord = dependency.groupId() + ":" + dependency.artifactId();
                String version = dependency.version();
                
                if (version != null) {
                    Map<String, List<String>> versionMap = versionsByDependency.computeIfAbsent(depCoord, k -> new HashMap<>());
                    versionMap.computeIfAbsent(version, k -> new ArrayList<>()).add(moduleName);
                }
            }
        }

        // Find dependencies with inconsistent versions
        List<VersionInconsistency> inconsistencies = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<String>>> entry : versionsByDependency.entrySet()) {
            String depCoord = entry.getKey();
            Map<String, List<String>> versionMap = entry.getValue();
            
            if (versionMap.size() > 1) {
                // More than one version is being used
                inconsistencies.add(new VersionInconsistency(depCoord, versionMap));
            }
        }

        return inconsistencies;
    }

    /**
     * Represents a dependency that has inconsistent versions across modules.
     */
    public static class VersionInconsistency {
        private final String dependencyCoordinate;
        private final Map<String, List<String>> versionToModules;

        public VersionInconsistency(String dependencyCoordinate, Map<String, List<String>> versionToModules) {
            this.dependencyCoordinate = dependencyCoordinate;
            this.versionToModules = versionToModules;
        }

        public String getDependencyCoordinate() {
            return dependencyCoordinate;
        }

        public Map<String, List<String>> getVersionToModules() {
            return versionToModules;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Dependency ").append(dependencyCoordinate).append(" has inconsistent versions:\n");
            
            for (Map.Entry<String, List<String>> versionEntry : versionToModules.entrySet()) {
                sb.append("  - Version ").append(versionEntry.getKey())
                  .append(" used in modules: ").append(String.join(", ", versionEntry.getValue()))
                  .append("\n");
            }
            
            return sb.toString();
        }
    }

    /**
     * Contains the consolidated results of dependency analysis across a multi-module project.
     */
    public static class DependencyReport {
        private final List<Dependency> consolidatedDependencies;
        private final List<VersionInconsistency> versionInconsistencies;
        private final Map<String, List<String>> dependencyUsageByModule;

        public DependencyReport(
                List<Dependency> consolidatedDependencies,
                List<VersionInconsistency> versionInconsistencies,
                Map<String, List<String>> dependencyUsageByModule) {
            this.consolidatedDependencies = consolidatedDependencies;
            this.versionInconsistencies = versionInconsistencies;
            this.dependencyUsageByModule = dependencyUsageByModule;
        }

        public List<Dependency> getConsolidatedDependencies() {
            return consolidatedDependencies;
        }

        public List<VersionInconsistency> getVersionInconsistencies() {
            return versionInconsistencies;
        }

        public Map<String, List<String>> getDependencyUsageByModule() {
            return dependencyUsageByModule;
        }

        /**
         * Checks if there are any version inconsistencies in the project.
         *
         * @return true if there are inconsistencies, false otherwise
         */
        public boolean hasVersionInconsistencies() {
            return !versionInconsistencies.isEmpty();
        }

        /**
         * Gets a formatted summary of the dependency report.
         *
         * @return a formatted summary string
         */
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            
            sb.append("Dependency Report Summary:\n");
            sb.append("-------------------------\n");
            sb.append("Total unique dependencies: ").append(consolidatedDependencies.size()).append("\n\n");
            
            if (hasVersionInconsistencies()) {
                sb.append("WARNING: Found ").append(versionInconsistencies.size())
                  .append(" dependencies with inconsistent versions across modules:\n\n");
                
                for (VersionInconsistency inconsistency : versionInconsistencies) {
                    sb.append(inconsistency.toString()).append("\n");
                }
            } else {
                sb.append("All dependencies have consistent versions across modules.\n\n");
            }
            
            return sb.toString();
        }
    }
}