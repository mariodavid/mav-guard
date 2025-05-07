package de.diedavids.mavguard.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

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
     * Converts this XML model to a domain model Dependency.
     *
     * @return a new Dependency instance with the same data
     */
    public de.diedavids.mavguard.model.Dependency toDomainModel() {
        return new de.diedavids.mavguard.model.Dependency(
            groupId, 
            artifactId, 
            version, 
            scope, 
            optional, 
            type
        );
    }
}