package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.nexus.client.NexusClient;
import de.diedavids.mavguard.nexus.config.NexusProperties;
import de.diedavids.mavguard.nexus.config.RepositoryType;
import de.diedavids.mavguard.nexus.model.MavenMetadata;
import de.diedavids.mavguard.nexus.model.NexusArtifactVersion;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository service implementation for Nexus Repository Manager.
 */
@Component
public class NexusRepositoryService implements RepositoryService {

    private final NexusClient nexusClient;
    private final NexusProperties properties;

    public NexusRepositoryService(NexusClient nexusClient, NexusProperties properties) {
        this.nexusClient = nexusClient;
        this.properties = properties;
    }

    @Override
    public List<NexusArtifactVersion> getAvailableVersions(Dependency dependency) {
        try {
            // Convert dots to slashes in groupId for URL path
            String groupIdPath = dependency.groupId().replace('.', '/');
            
            // For Nexus, use repository-based URL structure
            MavenMetadata metadata = nexusClient.getMavenMetadata(
                    properties.repository(),
                    groupIdPath,
                    dependency.artifactId());
            
            // Convert versions to NexusArtifactVersion objects
            return metadata.getVersions().stream()
                    .map(version -> new NexusArtifactVersion(
                            version,
                            LocalDate.now(), // Nexus metadata doesn't include last modified dates
                            version.contains("SNAPSHOT") // Check if it's a snapshot
                    ))
                    .sorted(Comparator.comparing(NexusArtifactVersion::version).reversed())
                    .toList();
        } catch (Exception e) {
            // Log the error and return empty list
            System.err.println("Error fetching versions from Nexus: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<NexusArtifactVersion> getAvailableParentVersions(de.diedavids.mavguard.model.Project.Parent parent) {
        try {
            // Convert dots to slashes in groupId for URL path
            String groupIdPath = parent.groupId().replace('.', '/');
            
            // For Nexus, use repository-based URL structure
            MavenMetadata metadata = nexusClient.getMavenMetadata(
                    properties.repository(),
                    groupIdPath,
                    parent.artifactId());
            
            // Convert versions to NexusArtifactVersion objects
            return metadata.getVersions().stream()
                    .map(version -> new NexusArtifactVersion(
                            version,
                            LocalDate.now(), // Nexus metadata doesn't include last modified dates
                            version.contains("SNAPSHOT") // Check if it's a snapshot
                    ))
                    .sorted(Comparator.comparing(NexusArtifactVersion::version).reversed())
                    .toList();
        } catch (Exception e) {
            // Log the error and return empty list
            System.err.println("Error fetching parent versions from Nexus: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String getRepositoryType() {
        return RepositoryType.NEXUS.name();
    }
}