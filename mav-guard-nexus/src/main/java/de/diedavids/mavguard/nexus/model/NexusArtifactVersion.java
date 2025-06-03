package de.diedavids.mavguard.nexus.model;

import java.time.LocalDate;

/**
 * Represents a specific version of an artifact in Nexus repository.
 */
public record NexusArtifactVersion(
    String version,
    LocalDate lastModified,
    boolean isSnapshot
) {
}
