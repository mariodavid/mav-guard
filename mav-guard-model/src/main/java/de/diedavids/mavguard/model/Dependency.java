package de.diedavids.mavguard.model;

/**
 * Represents a Maven dependency with groupId, artifactId, and version.
 */
public class Dependency {

    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private Boolean optional;
    private String type;

    /**
     * Default constructor required by JAXB.
     */
    public Dependency() {
    }

    /**
     * Constructor with required fields only.
     */
    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * Full constructor.
     */
    public Dependency(String groupId, String artifactId, String version, String scope, Boolean optional, String type) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
        this.optional = optional;
        this.type = type;
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

    /**
     * Gets the scope.
     * 
     * @return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Gets the optional flag.
     * 
     * @return the optional flag
     */
    public Boolean isOptional() {
        return optional;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + 
               (scope != null ? " (scope: " + scope + ")" : "") +
                (type != null ? " (type: " + type + ")" : "") +
                (optional != null && optional ? " (optional)" : "");
    }
}
