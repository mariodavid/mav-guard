package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for dependency version information.
 */
@Service
public interface DependencyVersionService {

    /**
     * Gets all available versions for a dependency.
     *
     * @param dependency the dependency to get versions for
     * @return a list of available versions, sorted with newest first
     */
    List<String> getAvailableVersions(Dependency dependency);

    /**
     * Gets the latest version for a dependency.
     *
     * @param dependency the dependency to get the latest version for
     * @return the latest version, or empty if none found
     */
    default Optional<String> getLatestVersion(Dependency dependency) {
        List<String> versions = getAvailableVersions(dependency);
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(0));
    }
    
    /**
     * Checks if a newer version is available for a dependency.
     * 
     * @param dependency the dependency to check
     * @return true if a newer version is available, false otherwise
     */
    default boolean hasNewerVersion(Dependency dependency) {
        return getLatestVersion(dependency)
                .map(latest -> !latest.equals(dependency.version()))
                .orElse(false);
    }
    
    /**
     * Gets all available versions for a parent.
     *
     * @param parent the parent to get versions for
     * @return a list of available versions, sorted with newest first
     */
    List<String> getAvailableParentVersions(Project.Parent parent);

    /**
     * Gets the latest version for a parent.
     *
     * @param parent the parent to get the latest version for
     * @return the latest version, or empty if none found
     */
    default Optional<String> getLatestParentVersion(Project.Parent parent) {
        List<String> versions = getAvailableParentVersions(parent);
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(0));
    }
    
    /**
     * Checks if a newer version is available for a parent.
     * 
     * @param parent the parent to check
     * @return true if a newer version is available, false otherwise
     */
    default boolean hasNewerParentVersion(Project.Parent parent) {
        return getLatestParentVersion(parent)
                .map(latest -> !latest.equals(parent.version()))
                .orElse(false);
    }
}