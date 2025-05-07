package de.diedavids.mavguard.model;

import java.util.Collections;
import java.util.List;

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
        Build build
) {

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
                all = new java.util.ArrayList<>(all);
                all.addAll(managed);
            }
        }
        return all;
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
}
