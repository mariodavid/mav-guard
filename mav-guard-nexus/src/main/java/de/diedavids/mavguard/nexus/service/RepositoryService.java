package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.model.NexusArtifactVersion;

import java.util.List;

/**
 * Interface for repository services that can fetch artifact version information.
 */
public interface RepositoryService {
    
    /**
     * Retrieves available versions for a given dependency.
     *
     * @param dependency the dependency to check
     * @return list of available versions, or empty list if not found
     */
    List<NexusArtifactVersion> getAvailableVersions(Dependency dependency);
    
    /**
     * Retrieves available versions for a given parent.
     *
     * @param parent the parent to check
     * @return list of available versions, or empty list if not found
     */
    List<NexusArtifactVersion> getAvailableParentVersions(Project.Parent parent);
    
    /**
     * Gets the type of repository this service supports.
     *
     * @return the repository type
     */
    String getRepositoryType();
}