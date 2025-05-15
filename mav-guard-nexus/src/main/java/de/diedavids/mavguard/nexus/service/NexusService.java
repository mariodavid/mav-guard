package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.client.NexusClient;
import de.diedavids.mavguard.nexus.config.NexusProperties;
import de.diedavids.mavguard.nexus.model.MavenMetadata;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for interacting with Nexus Repository Manager.
 */
@Service
public class NexusService implements DependencyVersionService {

    private final NexusClient nexusClient;
    private final NexusProperties properties;

    /**
     * Creates a new NexusService.
     *
     * @param nexusClient the Nexus client
     * @param properties the Nexus properties
     */
    public NexusService(NexusClient nexusClient, NexusProperties properties) {
        this.nexusClient = nexusClient;
        this.properties = properties;
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
            String repository = properties.repository();
            // Convert dots to slashes in groupId for URL path
            String groupIdPath = dependency.groupId().replace('.', '/');
            
            MavenMetadata metadata = nexusClient.getMavenMetadata(
                    repository,
                    groupIdPath,
                    dependency.artifactId());
            
            // Sort versions in descending order (newest first)
            return metadata.getVersions().stream()
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (Exception e) {
            // Log the error and return empty list
            System.err.println("Error fetching versions from Nexus: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Gets latest version for a dependency.
     *
     * @param dependency the dependency to get latest version for
     * @return the latest version, or empty if not found
     */
    @Override
    public Optional<String> getLatestVersion(Dependency dependency) {
        try {
            String repository = properties.repository();
            String groupIdPath = dependency.groupId().replace('.', '/');
            
            MavenMetadata metadata = nexusClient.getMavenMetadata(
                    repository,
                    groupIdPath,
                    dependency.artifactId());
            
            // Try to get release version first, then latest, then first from versions list
            if (metadata.getLatestReleaseVersion() != null) {
                return Optional.of(metadata.getLatestReleaseVersion());
            } else if (metadata.getLatestVersion() != null) {
                return Optional.of(metadata.getLatestVersion());
            } else {
                List<String> versions = metadata.getVersions().stream()
                        .sorted(Comparator.reverseOrder())
                        .toList();
                return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(0));
            }
        } catch (Exception e) {
            System.err.println("Error fetching latest version from Nexus: " + e.getMessage());
            return Optional.empty();
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
            String repository = properties.repository();
            // Convert dots to slashes in groupId for URL path
            String groupIdPath = parent.groupId().replace('.', '/');
            
            MavenMetadata metadata = nexusClient.getMavenMetadata(
                    repository,
                    groupIdPath,
                    parent.artifactId());
            
            // Sort versions in descending order (newest first)
            return metadata.getVersions().stream()
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (Exception e) {
            // Log the error and return empty list
            System.err.println("Error fetching parent versions from Nexus: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Gets latest version for a parent.
     *
     * @param parent the parent to get latest version for
     * @return the latest version, or empty if not found
     */
    @Override
    public Optional<String> getLatestParentVersion(Project.Parent parent) {
        try {
            String repository = properties.repository();
            String groupIdPath = parent.groupId().replace('.', '/');
            
            MavenMetadata metadata = nexusClient.getMavenMetadata(
                    repository,
                    groupIdPath,
                    parent.artifactId());
            
            // Try to get release version first, then latest, then first from versions list
            if (metadata.getLatestReleaseVersion() != null) {
                return Optional.of(metadata.getLatestReleaseVersion());
            } else if (metadata.getLatestVersion() != null) {
                return Optional.of(metadata.getLatestVersion());
            } else {
                List<String> versions = metadata.getVersions().stream()
                        .sorted(Comparator.reverseOrder())
                        .toList();
                return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(0));
            }
        } catch (Exception e) {
            System.err.println("Error fetching latest parent version from Nexus: " + e.getMessage());
            return Optional.empty();
        }
    }
}