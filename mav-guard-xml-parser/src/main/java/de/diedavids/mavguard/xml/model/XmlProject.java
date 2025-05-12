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

    @XmlElement(name = "properties", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlProperties properties;

    @XmlElementWrapper(name = "dependencies", namespace = "http://maven.apache.org/POM/4.0.0")
    @XmlElement(name = "dependency", namespace = "http://maven.apache.org/POM/4.0.0")
    private List<XmlDependency> dependencies;

    @XmlElement(name = "dependencyManagement", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlDependencyManagement dependencyManagement;

    @XmlElement(name = "build", namespace = "http://maven.apache.org/POM/4.0.0")
    private XmlBuild build;

    public XmlProject() {
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

    public String getPackaging() {
        return packaging;
    }

    public String getName() {
        return name;
    }

    /**
     * Gets all properties defined in the project.
     *
     * @return a map of property names to property values
     */
    public Map<String, String> getProperties() {
        return properties != null ? properties.getPropertyMap() : Collections.emptyMap();
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
        Map<String, String> propertiesMap = getProperties();

        return new Project(groupId, artifactId, version, packaging, name, domainDependencies, domainDependencyManagement, domainBuild, propertiesMap);
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
