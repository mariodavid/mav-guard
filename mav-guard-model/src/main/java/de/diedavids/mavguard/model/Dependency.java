package de.diedavids.mavguard.model;

/**
 * Represents a Maven dependency with groupId, artifactId, and version.
 */
public record Dependency(
    String groupId,
    String artifactId,
    String version,
    String scope,
    Boolean optional,
    String type
) {
    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + 
               (scope != null ? " (scope: " + scope + ")" : "") +
               (type != null ? " (type: " + type + ")" : "") +
               (optional != null && optional ? " (optional)" : "");
    }
}
