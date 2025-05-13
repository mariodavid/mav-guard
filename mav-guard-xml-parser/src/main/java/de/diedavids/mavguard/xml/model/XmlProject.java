package de.diedavids.mavguard.xml.model;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.PluginDependency;
import de.diedavids.mavguard.model.Project;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @XmlElement(name = "parent", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlParent parent;

    @XmlElement(name = "properties", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlProperties properties;

    @XmlElementWrapper(name = "dependencies", namespace = "http://maven.apache.org/POM/4.0.0")
    @XmlElement(name = "dependency", namespace = "http://maven.apache.org/POM/4.0.0")
    private List<XmlDependency> dependencies;

    @XmlElement(name = "dependencyManagement", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlDependencyManagement dependencyManagement;

    @XmlElement(name = "build", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlBuild build;

    @XmlElementWrapper(name = "modules", namespace = "http://maven.apache.org/POM/4.0.0")
    @XmlElement(name = "module", namespace = "http://maven.apache.org/POM/4.0.0")
    private List<String> modules;

    // Fields not mapped from XML, used during processing
    private transient XmlProject parentProject;
    private transient String relativePath;

    public XmlProject() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public XmlParent getParent() {
        return parent;
    }

    /**
     * Get the effective group ID, considering parent inheritance if needed
     * @return the effective group ID
     */
    public String getEffectiveGroupId() {
        if (groupId != null && !groupId.isEmpty()) {
            return groupId;
        } else if (parent != null) {
            return parent.getGroupId();
        }
        return null;
    }

    /**
     * Get the effective version, considering parent inheritance if needed
     * @return the effective version
     */
    public String getEffectiveVersion() {
        if (version != null && !version.isEmpty()) {
            return version;
        } else if (parent != null) {
            return parent.getVersion();
        }
        return null;
    }

    /**
     * Gets all properties defined in the project.
     *
     * @return a map of property names to property values
     */
    public Map<String, String> getProperties() {
        return properties != null ? properties.getPropertyMap() : Collections.emptyMap();
    }

    /**
     * Gets all properties including inherited ones from parent projects.
     * Child properties override parent properties if they have the same name.
     *
     * @return a combined map of property names to property values
     */
    public Map<String, String> getAllProperties() {
        Map<String, String> allProps = new HashMap<>();
        
        // Add parent properties first (if parent exists)
        if (parentProject != null) {
            allProps.putAll(parentProject.getAllProperties());
        }
        
        // Add/override with this project's properties (higher precedence)
        allProps.putAll(getProperties());
        
        return allProps;
    }

    public List<XmlDependency> getDependencies() {
        return dependencies != null ? dependencies : Collections.emptyList();
    }

    public XmlDependencyManagement getDependencyManagement() {
        return dependencyManagement;
    }

    public XmlBuild getBuild() {
        return build;
    }

    public List<String> getModules() {
        return modules != null ? modules : Collections.emptyList();
    }

    public XmlProject getParentProject() {
        return parentProject;
    }

    public void setParentProject(XmlProject parentProject) {
        this.parentProject = parentProject;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public Project toDomainModel() {
        List<Dependency> domainDependencies = dependencies != null
                ? dependencies.stream().map(XmlDependency::toDomainModel).collect(Collectors.toList())
                : Collections.emptyList();

        Project.DependencyManagement domainDependencyManagement = null;
        if (dependencyManagement != null && dependencyManagement.getDependencies() != null) {
            List<Dependency> managedDependencies = dependencyManagement.getDependencies().stream()
                    .map(XmlDependency::toDomainModel)
                    .collect(Collectors.toList());
            domainDependencyManagement = new Project.DependencyManagement(managedDependencies);
        }

        Project.Build domainBuild = null;
        if (build != null && build.getPlugins() != null) {
            List<PluginDependency> plugins = build.getPlugins().stream()
                    .map(XmlPluginDependency::toDomainModel)
                    .collect(Collectors.toList());
            domainBuild = new Project.Build(plugins);
        }

        // Convert properties to a map for the domain model
        Map<String, String> propertiesMap = getAllProperties();

        // Create the parent reference if it exists
        Project.Parent domainParent = null;
        if (parent != null) {
            domainParent = new Project.Parent(
                    parent.getGroupId(),
                    parent.getArtifactId(),
                    parent.getVersion(),
                    parent.getRelativePath()
            );
        }

        // Convert module paths to strings
        List<String> modulesList = getModules();

        // Use effective groupId and version if necessary
        String effectiveGroupId = getEffectiveGroupId();
        String effectiveVersion = getEffectiveVersion();

        return new Project(
                effectiveGroupId, 
                artifactId, 
                effectiveVersion, 
                packaging, 
                name, 
                domainDependencies, 
                domainDependencyManagement, 
                domainBuild, 
                propertiesMap,
                domainParent,
                modulesList,
                relativePath
        );
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlParent {
        @XmlElement(name = "groupId", namespace = "http://maven.apache.org/POM/4.0.0")
        private String groupId;

        @XmlElement(name = "artifactId", namespace = "http://maven.apache.org/POM/4.0.0")
        private String artifactId;

        @XmlElement(name = "version", namespace = "http://maven.apache.org/POM/4.0.0")
        private String version;

        @XmlElement(name = "relativePath", namespace = "http://maven.apache.org/POM/4.0.0")
        private String relativePath;

        public XmlParent() {
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public String getRelativePath() {
            return relativePath != null ? relativePath : "../pom.xml";
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlDependencyManagement {

        @XmlElementWrapper(name = "dependencies", namespace = "http://maven.apache.org/POM/4.0.0")
        @XmlElement(name = "dependency", namespace = "http://maven.apache.org/POM/4.0.0")
        private List<XmlDependency> dependencies;

        public List<XmlDependency> getDependencies() {
            return dependencies != null ? dependencies : Collections.emptyList();
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlBuild {

        @XmlElementWrapper(name = "plugins", namespace = "http://maven.apache.org/POM/4.0.0")
        @XmlElement(name = "plugin", namespace = "http://maven.apache.org/POM/4.0.0")
        private List<XmlPluginDependency> plugins;

        public List<XmlPluginDependency> getPlugins() {
            return plugins != null ? plugins : Collections.emptyList();
        }
    }
}
