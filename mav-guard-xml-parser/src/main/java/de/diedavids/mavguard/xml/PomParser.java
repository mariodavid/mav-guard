package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.xml.model.XmlDependency;
import de.diedavids.mavguard.xml.model.XmlProject;
import de.diedavids.mavguard.xml.property.MavenPropertyResolver;
import de.diedavids.mavguard.xml.property.PropertyResolver;
import jakarta.xml.bind.JAXBException;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Parser for Maven POM files.
 */
public class PomParser implements PomFileProcessor {

    private final XmlParser xmlParser;
    private final PropertyResolver propertyResolver;

    public PomParser() {
        this.xmlParser = new XmlParser();
        this.propertyResolver = new MavenPropertyResolver();
    }

    /**
     * Parses a POM file.
     *
     * @param pomFile the POM file to parse
     * @return the parsed Project object
     * @throws JAXBException if there is an error during parsing
     * @throws IllegalArgumentException if pomFile is null or doesn't exist
     */
    @Override
    public Project parsePomFile(File pomFile) throws JAXBException {
        validateFile(pomFile);
        XmlProject xmlProject = xmlParser.parseXmlFile(pomFile, XmlProject.class);
        resolvePropertyPlaceholders(xmlProject);
        return xmlProject.toDomainModel();
    }

    /**
     * Parses a POM file from an input stream.
     *
     * @param inputStream the input stream containing the POM XML
     * @return the parsed Project object
     * @throws JAXBException if there is an error during parsing
     * @throws IllegalArgumentException if inputStream is null
     */
    @Override
    public Project parsePomStream(InputStream inputStream) throws JAXBException {
        validateInputStream(inputStream);
        XmlProject xmlProject = xmlParser.parseXmlStream(inputStream, XmlProject.class);
        resolvePropertyPlaceholders(xmlProject);
        return xmlProject.toDomainModel();
    }

    /**
     * Resolves property placeholders in dependency versions.
     *
     * @param project the XmlProject containing dependencies and properties
     */
    private void resolvePropertyPlaceholders(XmlProject project) {
        // Resolve properties in direct dependencies
        List<XmlDependency> dependencies = project.getDependencies();
        for (XmlDependency dependency : dependencies) {
            resolvePropertyPlaceholdersInDependency(dependency, project);
        }

        // Resolve properties in managed dependencies if they exist
        if (project.getDependencyManagement() != null) {
            List<XmlDependency> managedDependencies = project.getDependencyManagement().getDependencies();
            for (XmlDependency dependency : managedDependencies) {
                resolvePropertyPlaceholdersInDependency(dependency, project);
            }
        }
    }

    /**
     * Resolves property placeholders in a dependency's version.
     *
     * @param dependency the dependency to process
     * @param project the XmlProject containing property definitions
     */
    private void resolvePropertyPlaceholdersInDependency(XmlDependency dependency, XmlProject project) {
        String version = dependency.getVersion();
        if (version != null && propertyResolver.isPropertyPlaceholder(version)) {
            String resolvedVersion = propertyResolver.resolveProperty(version, project);
            dependency.setResolvedVersion(resolvedVersion);
        }
    }

    private void validateFile(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("POM file must not be null and must exist");
        }
    }

    private void validateInputStream(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream must not be null");
        }
    }
}
