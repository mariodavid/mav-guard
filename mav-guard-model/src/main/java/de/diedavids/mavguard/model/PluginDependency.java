package de.diedavids.mavguard.model;

/**
 * Represents a Maven plugin dependency with groupId, artifactId, and version.
 */
public class PluginDependency {

    private String groupId;
    private String artifactId;
    private String version;

    /**
     * Default constructor required by JAXB.
     */
    public PluginDependency() {
    }

    /**
     * Constructor with all fields.
     */
    public PluginDependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * Gets the group ID.
     * 
     * @return the group ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Gets the artifact ID.
     * 
     * @return the artifact ID
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Gets the version.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
