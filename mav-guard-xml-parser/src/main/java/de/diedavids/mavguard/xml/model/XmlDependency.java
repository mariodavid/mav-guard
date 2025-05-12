package de.diedavids.mavguard.xml.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * XML model class for Maven dependency with groupId, artifactId, and version.
 * This class is used for XML parsing only and is mapped to the domain model.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlDependency {

    @XmlElement(name = "groupId", namespace = "http://maven.apache.org/POM/4.0.0")
    private String groupId;

    @XmlElement(name = "artifactId", namespace = "http://maven.apache.org/POM/4.0.0")
    private String artifactId;

    @XmlElement(name = "version", namespace = "http://maven.apache.org/POM/4.0.0")
    private String version;

    @XmlElement(name = "scope", namespace = "http://maven.apache.org/POM/4.0.0")
    private String scope;

    @XmlElement(name = "optional", namespace = "http://maven.apache.org/POM/4.0.0")
    private Boolean optional;

    @XmlElement(name = "type", namespace = "http://maven.apache.org/POM/4.0.0")
    private String type;

    // Not mapped to XML - transient field to store resolved version
    private transient String resolvedVersion;

    /**
     * Default constructor required by JAXB.
     */
    public XmlDependency() {
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

    /**
     * Gets the resolved version after property resolution has been applied.
     * If no property resolution has been done, returns the original version.
     *
     * @return the resolved version, or the original version if no resolution was needed
     */
    public String getResolvedVersion() {
        return resolvedVersion != null ? resolvedVersion : version;
    }

    /**
     * Sets the resolved version.
     * This method should only be used by the property resolver.
     *
     * @param resolvedVersion the resolved version
     */
    public void setResolvedVersion(String resolvedVersion) {
        this.resolvedVersion = resolvedVersion;
    }

    /**
     * Converts this XML model to a domain model Dependency.
     *
     * @return a new Dependency instance with the same data
     */
    public de.diedavids.mavguard.model.Dependency toDomainModel() {
        // Use the resolved version if available, otherwise use the original version
        String effectiveVersion = resolvedVersion != null ? resolvedVersion : version;

        return new de.diedavids.mavguard.model.Dependency(
            groupId,
            artifactId,
            effectiveVersion,
            scope,
            optional,
            type
        );
    }

    /**
     * Creates a copy of this dependency with resolved version.
     *
     * @param resolvedVersion the resolved version
     * @return a new XmlDependency instance with the resolved version
     */
    public XmlDependency withResolvedVersion(String resolvedVersion) {
        XmlDependency copy = new XmlDependency();
        copy.groupId = this.groupId;
        copy.artifactId = this.artifactId;
        copy.version = this.version;
        copy.scope = this.scope;
        copy.optional = this.optional;
        copy.type = this.type;
        copy.resolvedVersion = resolvedVersion;
        return copy;
    }
}