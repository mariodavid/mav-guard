package de.diedavids.mavguard.model;

import java.util.Collections;
import java.util.List;

/**
 * Represents a Maven project (POM) with its dependencies.
 */
public class Project {

    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;
    private String name;
    private List<Dependency> dependencies;
    private DependencyManagement dependencyManagement;
    private Build build;

    /**
     * Default constructor required by JAXB.
     */
    public Project() {
    }

    /**
     * Gets the project's group ID.
     * 
     * @return the group ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Gets the project's artifact ID.
     * 
     * @return the artifact ID
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Gets the project's version.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the project's packaging type.
     * 
     * @return the packaging type
     */
    public String getPackaging() {
        return packaging;
    }

    /**
     * Gets the project's name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the project's dependencies.
     * 
     * @return the list of dependencies, or an empty list if none
     */
    public List<Dependency> getDependencies() {
        return dependencies != null ? dependencies : Collections.emptyList();
    }

    /**
     * Gets the project's dependency management section.
     * 
     * @return the dependency management section
     */
    public DependencyManagement getDependencyManagement() {
        return dependencyManagement;
    }

    /**
     * Gets the project's build section.
     * 
     * @return the build section
     */
    public Build getBuild() {
        return build;
    }

    /**
     * Gets all dependencies from both the dependencies section and the dependency management section.
     * 
     * @return a list of all dependencies
     */
    public List<Dependency> getAllDependencies() {
        List<Dependency> allDependencies = getDependencies();

        if (dependencyManagement != null && dependencyManagement.getDependencies() != null) {
            allDependencies = new java.util.ArrayList<>(allDependencies);
            allDependencies.addAll(dependencyManagement.getDependencies());
        }

        return allDependencies;
    }

    /**
     * Represents the dependencyManagement section of a POM.
     */
    public static class DependencyManagement {

        private List<Dependency> dependencies;

        /**
         * Gets the dependencies in the dependency management section.
         * 
         * @return the list of dependencies, or an empty list if none
         */
        public List<Dependency> getDependencies() {
            return dependencies != null ? dependencies : Collections.emptyList();
        }
    }

    /**
     * Represents the build section of a POM.
     */
    public static class Build {

        private List<PluginDependency> plugins;

        /**
         * Gets the plugins in the build section.
         * 
         * @return the list of plugins, or an empty list if none
         */
        public List<PluginDependency> getPlugins() {
            return plugins != null ? plugins : Collections.emptyList();
        }
    }
}
