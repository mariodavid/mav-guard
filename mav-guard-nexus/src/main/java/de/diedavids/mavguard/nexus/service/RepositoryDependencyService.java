package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.model.NexusArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Service implementation for interacting with repository managers using strategy pattern.
 */
@Service
public class RepositoryDependencyService implements DependencyVersionService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryDependencyService.class);

    private final RepositoryServiceFactory repositoryServiceFactory;

    /**
     * Creates a new RepositoryDependencyService.
     *
     * @param repositoryServiceFactory factory for creating appropriate repository service
     */
    public RepositoryDependencyService(RepositoryServiceFactory repositoryServiceFactory) {
        this.repositoryServiceFactory = repositoryServiceFactory;
    }

    /**
     * Gets all available versions for a dependency.
     *
     * @param dependency the dependency to get versions for
     * @return a list of available versions
     */
    @Override
    public List<String> getAvailableVersions(Dependency dependency) {
        try {
            RepositoryService repositoryService = repositoryServiceFactory.createRepositoryService();
            List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(dependency);
            
            // Extract version strings and sort in descending order (newest first)
            return versions.stream()
                    .map(NexusArtifactVersion::version)
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (Exception e) {
            log.atError()
                .addKeyValue("dependency", dependency.groupId() + ":" + dependency.artifactId())
                .log("Error fetching versions from repository: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    
    /**
     * Gets all available versions for a parent.
     *
     * @param parent the parent to get versions for
     * @return a list of available versions
     */
    @Override
    public List<String> getAvailableParentVersions(Project.Parent parent) {
        try {
            RepositoryService repositoryService = repositoryServiceFactory.createRepositoryService();
            List<NexusArtifactVersion> versions = repositoryService.getAvailableParentVersions(parent);
            
            // Extract version strings and sort in descending order (newest first)
            return versions.stream()
                    .map(NexusArtifactVersion::version)
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (Exception e) {
            log.atError()
                .addKeyValue("parent", parent.getCoordinates())
                .log("Error fetching parent versions from repository: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}