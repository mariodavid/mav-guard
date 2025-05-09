package de.diedavids.mavguard.nexus.model;

/**
 * Represents a specific version of an artifact in Nexus repository.
 */
public record NexusArtifactVersion(
    String version,
    String lastModified,
    String lastUpdated
) {
}
