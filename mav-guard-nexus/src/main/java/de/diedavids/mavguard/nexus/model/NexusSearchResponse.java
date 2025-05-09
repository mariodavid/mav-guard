package de.diedavids.mavguard.nexus.model;

import java.util.List;

/**
 * Represents the response from the Nexus search API.
 */
public record NexusSearchResponse(
    List<NexusArtifactVersion> items,
    Integer totalCount
) {
}
