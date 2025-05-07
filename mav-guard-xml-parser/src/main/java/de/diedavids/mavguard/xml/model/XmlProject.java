package de.diedavids.mavguard.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XML model class for a Maven project (POM) with its dependencies.
 * This class is used for XML parsing only and is mapped to the domain model.
 */
@XmlRootElement(name = "project", namespace = "http://maven.apache.org/POM/4.0.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlProject {

    @XmlElement(name = "groupId", namespace = "http://maven.apache.org/POM/4.0.0")
    private String groupId;

    @XmlElement(name = "artifactId", namespace = "http://maven.apache.org/POM/4.0.0")
    private String artifactId;

    @XmlElement(name = "version", namespace = "http://maven.apache.org/POM/4.0.0")
    private String version;

    @XmlElement(name = "packaging", namespace = "http://maven.apache.org/POM/4.0.0")
    private String packaging;

    @XmlElement(name = "name", namespace = "http://maven.apache.org/POM/4.0.0")
    private String name;

    @XmlElementWrapper(name = "dependencies", namespace = "http://maven.apache.org/POM/4.0.0")
    @XmlElement(name = "dependency", namespace = "http://maven.apache.org/POM/4.0.0")
    private List<XmlDependency> dependencies;

    @XmlElement(name = "dependencyManagement", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlDependencyManagement dependencyManagement;

    @XmlElement(name = "build", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlBuild build;

    /**
     * Default constructor required by JAXB.
     */
    public XmlProject() {
    }

    /**
     * Gets the project's group ID.
     * 
     * @return the group ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Gets the project's artifact ID.
     * 
     * @return the artifact ID
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Gets the project's version.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the project's packaging type.
     * 
     * @return the packaging type
     */
    public String getPackaging() {
        return packaging;
    }

    /**
     * Gets the project's name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the project's dependencies.
     * 
     * @return the list of dependencies, or an empty list if none
     */
    public List<XmlDependency> getDependencies() {
        return dependencies != null ? dependencies : Collections.emptyList();
    }

    /**
     * Gets the project's dependency management section.
     * 
     * @return the dependency management section
     */
    public XmlDependencyManagement getDependencyManagement() {
        return dependencyManagement;
    }

    /**
     * Gets the project's build section.
     * 
     * @return the build section
     */
    public XmlBuild getBuild() {
        return build;
    }

    /**
     * Converts this XML model to a domain model Project.
     *
     * @return a new Project instance with the same data
     */
    public de.diedavids.mavguard.model.Project toDomainModel() {
        try {
            de.diedavids.mavguard.model.Project project = new de.diedavids.mavguard.model.Project();
            
            // Map basic properties using reflection
            setFieldValue(project, "groupId", groupId);
            setFieldValue(project, "artifactId", artifactId);
            setFieldValue(project, "version", version);
            setFieldValue(project, "packaging", packaging);
            setFieldValue(project, "name", name);
            
            // Map dependencies
            if (dependencies != null) {
                List<de.diedavids.mavguard.model.Dependency> domainDependencies = 
                    dependencies.stream()
                        .map(XmlDependency::toDomainModel)
                        .collect(Collectors.toList());
                setFieldValue(project, "dependencies", domainDependencies);
            }
            
            // Map dependency management
            if (dependencyManagement != null) {
                de.diedavids.mavguard.model.Project.DependencyManagement domainDependencyManagement = 
                    new de.diedavids.mavguard.model.Project.DependencyManagement();
                
                if (dependencyManagement.getDependencies() != null) {
                    List<de.diedavids.mavguard.model.Dependency> domainManagedDependencies = 
                        dependencyManagement.getDependencies().stream()
                            .map(XmlDependency::toDomainModel)
                            .collect(Collectors.toList());
                    setFieldValue(domainDependencyManagement, "dependencies", domainManagedDependencies);
                }
                
                setFieldValue(project, "dependencyManagement", domainDependencyManagement);
            }
            
            // Map build
            if (build != null) {
                de.diedavids.mavguard.model.Project.Build domainBuild = 
                    new de.diedavids.mavguard.model.Project.Build();
                
                if (build.getPlugins() != null) {
                    List<de.diedavids.mavguard.model.PluginDependency> domainPlugins = 
                        build.getPlugins().stream()
                            .map(XmlPluginDependency::toDomainModel)
                            .collect(Collectors.toList());
                    setFieldValue(domainBuild, "plugins", domainPlugins);
                }
                
                setFieldValue(project, "build", domainBuild);
            }
            
            return project;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping XML model to domain model", e);
        }
    }
    
    /**
     * Sets a field value using reflection.
     *
     * @param object the object to set the field on
     * @param fieldName the name of the field
     * @param value the value to set
     * @throws Exception if there is an error setting the field
     */
    private void setFieldValue(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    /**
     * Represents the dependencyManagement section of a POM.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlDependencyManagement {

        @XmlElementWrapper(name = "dependencies", namespace = "http://maven.apache.org/POM/4.0.0")
        @XmlElement(name = "dependency", namespace = "http://maven.apache.org/POM/4.0.0")
        private List<XmlDependency> dependencies;

        /**
         * Gets the dependencies in the dependency management section.
         * 
         * @return the list of dependencies, or an empty list if none
         */
        public List<XmlDependency> getDependencies() {
            return dependencies != null ? dependencies : Collections.emptyList();
        }
    }

    /**
     * Represents the build section of a POM.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlBuild {

        @XmlElementWrapper(name = "plugins", namespace = "http://maven.apache.org/POM/4.0.0")
        @XmlElement(name = "plugin", namespace = "http://maven.apache.org/POM/4.0.0")
        private List<XmlPluginDependency> plugins;

        /**
         * Gets the plugins in the build section.
         * 
         * @return the list of plugins, or an empty list if none
         */
        public List<XmlPluginDependency> getPlugins() {
            return plugins != null ? plugins : Collections.emptyList();
        }
    }
}