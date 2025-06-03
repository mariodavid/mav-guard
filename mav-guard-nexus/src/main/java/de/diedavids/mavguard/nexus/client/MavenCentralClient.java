package de.diedavids.mavguard.nexus.client;

import de.diedavids.mavguard.nexus.model.MavenMetadata;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * HTTP Interface for Maven Central Repository.
 */
@HttpExchange
public interface MavenCentralClient {

    /**
     * Gets Maven metadata for a specific artifact from Maven Central.
     * Maven Central uses a simpler URL structure without repository groups.
     *
     * @param groupId the artifact group ID with slashes instead of dots (e.g., "org/springframework")
     * @param artifactId the artifact ID (e.g., "spring-core")
     * @return Maven metadata containing version information
     */
    @GetExchange("/{groupId}/{artifactId}/maven-metadata.xml")
    MavenMetadata getMavenMetadata(
            @PathVariable("groupId") String groupId,
            @PathVariable("artifactId") String artifactId);
}