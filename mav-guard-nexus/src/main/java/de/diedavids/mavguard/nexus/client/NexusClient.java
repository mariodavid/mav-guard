package de.diedavids.mavguard.nexus.client;

import de.diedavids.mavguard.nexus.model.MavenMetadata;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * HTTP Interface for Nexus Repository Manager.
 * Uses Spring 6 HTTP Interface client with direct Maven metadata access.
 */
@HttpExchange
public interface NexusClient {

    /**
     * Gets Maven metadata for a specific artifact from a Nexus repository.
     * This method uses the Nexus repository layout with repository groups.
     *
     * @param repository the repository name (e.g., "private")
     * @param groupId the artifact group ID with slashes instead of dots (e.g., "de/faktorzehn/flow")
     * @param artifactId the artifact ID (e.g., "flow-extapi")
     * @return Maven metadata containing version information
     */
    @GetExchange("/content/groups/{repository}/{groupId}/{artifactId}/maven-metadata.xml")
    MavenMetadata getMavenMetadata(
            @PathVariable("repository") String repository,
            @PathVariable("groupId") String groupId,
            @PathVariable("artifactId") String artifactId);
            
    /**
     * Gets Maven metadata for a specific artifact using Maven Central layout.
     * This method uses the standard Maven repository layout without repository groups.
     *
     * @param groupId the artifact group ID with slashes instead of dots (e.g., "org/springframework")
     * @param artifactId the artifact ID (e.g., "spring-core")
     * @return Maven metadata containing version information
     */
    @GetExchange("/{groupId}/{artifactId}/maven-metadata.xml")
    MavenMetadata getMavenMetadataSimple(
            @PathVariable("groupId") String groupId,
            @PathVariable("artifactId") String artifactId);
}