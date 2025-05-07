package de.diedavids.mavguard.model;

/**
 * Represents a Maven plugin dependency with groupId, artifactId, and version.
 */
public record PluginDependency(
    String groupId,
    String artifactId,
    String version
) {
    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
