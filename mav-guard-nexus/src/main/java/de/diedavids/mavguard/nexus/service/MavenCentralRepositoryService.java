package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.nexus.client.NexusClient;
import de.diedavids.mavguard.nexus.config.RepositoryType;
import de.diedavids.mavguard.nexus.model.MavenMetadata;
import de.diedavids.mavguard.nexus.model.NexusArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository service implementation for Maven Central.
 */
@Component
public class MavenCentralRepositoryService implements RepositoryService {

    private static final Logger log = LoggerFactory.getLogger(MavenCentralRepositoryService.class);

    private final NexusClient nexusClient;

    public MavenCentralRepositoryService(NexusClient nexusClient) {
        this.nexusClient = nexusClient;
    }

    @Override
    public List<NexusArtifactVersion> getAvailableVersions(Dependency dependency) {
        log.atDebug()
            .addKeyValue("dependency", dependency.groupId() + ":" + dependency.artifactId())
            .log("Fetching available versions from Maven Central");
        
        try {
            // Convert dots to slashes in groupId for URL path
            String groupIdPath = dependency.groupId().replace('.', '/');
            
            // For Maven Central, use simplified URL structure
            MavenMetadata metadata = nexusClient.getMavenMetadataSimple(
                    groupIdPath,
                    dependency.artifactId());
            
            // Convert versions to NexusArtifactVersion objects
            List<NexusArtifactVersion> versions = metadata.getVersions().stream()
                    .map(version -> new NexusArtifactVersion(
                            version,
                            LocalDate.now(), // Maven Central doesn't provide last modified dates in metadata
                            false // Not a snapshot
                    ))
                    .sorted(Comparator.comparing(NexusArtifactVersion::version).reversed())
                    .toList();
            
            log.atDebug()
                .addKeyValue("dependency", dependency.groupId() + ":" + dependency.artifactId())
                .addKeyValue("versionCount", versions.size())
                .log("Successfully fetched versions from Maven Central");
                
            return versions;
        } catch (Exception e) {
            log.atError()
                .addKeyValue("dependency", dependency.groupId() + ":" + dependency.artifactId())
                .log("Error fetching versions from Maven Central: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<NexusArtifactVersion> getAvailableParentVersions(de.diedavids.mavguard.model.Project.Parent parent) {
        log.atDebug()
            .addKeyValue("parent", parent.getCoordinates())
            .log("Fetching available parent versions from Maven Central");
        
        try {
            // Convert dots to slashes in groupId for URL path
            String groupIdPath = parent.groupId().replace('.', '/');
            
            // For Maven Central, use simplified URL structure
            MavenMetadata metadata = nexusClient.getMavenMetadataSimple(
                    groupIdPath,
                    parent.artifactId());
            
            // Convert versions to NexusArtifactVersion objects
            List<NexusArtifactVersion> versions = metadata.getVersions().stream()
                    .map(version -> new NexusArtifactVersion(
                            version,
                            LocalDate.now(), // Maven Central doesn't provide last modified dates in metadata
                            false // Not a snapshot
                    ))
                    .sorted(Comparator.comparing(NexusArtifactVersion::version).reversed())
                    .toList();
            
            log.atDebug()
                .addKeyValue("parent", parent.getCoordinates())
                .addKeyValue("versionCount", versions.size())
                .log("Successfully fetched parent versions from Maven Central");
                
            return versions;
        } catch (Exception e) {
            log.atError()
                .addKeyValue("parent", parent.getCoordinates())
                .log("Error fetching parent versions from Maven Central: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getRepositoryType() {
        return RepositoryType.MAVEN_CENTRAL.name();
    }
}