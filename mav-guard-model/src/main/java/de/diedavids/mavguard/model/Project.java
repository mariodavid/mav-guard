package de.diedavids.mavguard.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a Maven project (POM) with its dependencies.
 */
public record Project(
        String groupId,
        String artifactId,
        String version,
        String packaging,
        String name,
        List<Dependency> dependencies,
        DependencyManagement dependencyManagement,
        Build build,
        Map<String, String> properties,
        Parent parent,
        List<String> modules,
        String relativePath
) {
    /**
     * Constructor with minimal required parameters and defaults for optional ones.
     */
    public Project(
            String groupId,
            String artifactId,
            String version,
            String packaging,
            String name,
            List<Dependency> dependencies,
            DependencyManagement dependencyManagement,
            Build build,
            Map<String, String> properties
    ) {
        this(groupId, artifactId, version, packaging, name, dependencies, dependencyManagement, build, properties, null, Collections.emptyList(), null);
    }

    /**
     * Gets all dependencies from both the dependencies section and the dependency management section.
     *
     * @return a list of all dependencies
     */
    public List<Dependency> getAllDependencies() {
        List<Dependency> all = dependencies();
        if (dependencyManagement != null) {
            List<Dependency> managed = dependencyManagement.dependencies();
            if (!managed.isEmpty()) {
                all = new ArrayList<>(all);
                all.addAll(managed);
            }
        }
        return all;
    }

    /**
     * Checks if the project is a multi-module project (has modules defined).
     *
     * @return true if the project has modules, false otherwise
     */
    public boolean isMultiModule() {
        return modules != null && !modules.isEmpty();
    }

    /**
     * Checks if the project has a parent defined.
     *
     * @return true if the project has a parent, false otherwise
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Returns a unique identifier for this project.
     *
     * @return the project's coordinates as groupId:artifactId:version
     */
    public String getCoordinates() {
        return groupId + ":" + artifactId + ":" + version;
    }

    /**
     * Represents the dependencyManagement section of a POM.
     */
    public record DependencyManagement(List<Dependency> dependencies) {
    }

    /**
     * Represents the build section of a POM.
     */
    public record Build(List<PluginDependency> plugins) {
    }

    /**
     * Represents the parent section of a POM.
     */
    public record Parent(
            String groupId,
            String artifactId,
            String version,
            String relativePath
    ) {
        /**
         * Returns a unique identifier for this parent project.
         *
         * @return the parent's coordinates as groupId:artifactId:version
         */
        public String getCoordinates() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }
}
