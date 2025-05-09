package de.diedavids.mavguard.xml.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * XML model class for Maven plugin dependency with groupId, artifactId, and version.
 * This class is used for XML parsing only and is mapped to the domain model.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlPluginDependency {

    @XmlElement(name = "groupId", namespace = "http://maven.apache.org/POM/4.0.0")
    private String groupId;

    @XmlElement(name = "artifactId", namespace = "http://maven.apache.org/POM/4.0.0")
    private String artifactId;

    @XmlElement(name = "version", namespace = "http://maven.apache.org/POM/4.0.0")
    private String version;

    /**
     * Default constructor required by JAXB.
     */
    public XmlPluginDependency() {
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
     * Converts this XML model to a domain model PluginDependency.
     *
     * @return a new PluginDependency instance with the same data
     */
    public de.diedavids.mavguard.model.PluginDependency toDomainModel() {
        return new de.diedavids.mavguard.model.PluginDependency(
            groupId,
            artifactId,
            version
        );
    }
}