package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.xml.model.XmlDependency;
import de.diedavids.mavguard.xml.model.XmlProject;
import de.diedavids.mavguard.xml.property.MavenPropertyResolver;
import de.diedavids.mavguard.xml.property.PropertyResolver;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for Maven POM files.
 */
public class PomParser implements PomFileProcessor {

    private static final Logger log = LoggerFactory.getLogger(PomParser.class);

    private final XmlParser xmlParser;
    private final PropertyResolver propertyResolver;

    public PomParser() {
        this(new XmlParser());
    }

    public PomParser(XmlParser xmlParser) {
        this.xmlParser = xmlParser;
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
        log.atDebug()
            .addKeyValue("pomFile", pomFile.getAbsolutePath())
            .log("Starting POM file parsing");
        
        validateFile(pomFile);
        
        try {
            XmlProject xmlProject = xmlParser.parseXmlFile(pomFile, XmlProject.class);
            xmlProject.setRelativePath(pomFile.getAbsolutePath());
            resolvePropertyPlaceholders(xmlProject);
            Project result = xmlProject.toDomainModel();
            
            log.atInfo()
                .addKeyValue("projectCoordinates", result.getCoordinates())
                .addKeyValue("dependencyCount", result.getAllDependencies().size())
                .addKeyValue("pomFile", pomFile.getAbsolutePath())
                .log("POM file parsed successfully");
                
            return result;
        } catch (JAXBException e) {
            log.atError()
                .addKeyValue("pomFile", pomFile.getAbsolutePath())
                .log("Failed to parse POM file: {}", e.getMessage(), e);
            throw e;
        }
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
        log.atDebug()
            .log("Starting POM stream parsing");
        
        validateInputStream(inputStream);
        
        try {
            XmlProject xmlProject = xmlParser.parseXmlStream(inputStream, XmlProject.class);
            resolvePropertyPlaceholders(xmlProject);
            Project result = xmlProject.toDomainModel();
            
            log.atInfo()
                .addKeyValue("projectCoordinates", result.getCoordinates())
                .addKeyValue("dependencyCount", result.getAllDependencies().size())
                .log("POM stream parsed successfully");
                
            return result;
        } catch (JAXBException e) {
            log.atError()
                .log("Failed to parse POM stream: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Parses a multi-module Maven project, including the parent POM and all module POMs.
     * This method processes the entire project hierarchy, resolving dependencies and properties
     * according to Maven's inheritance rules.
     *
     * @param rootPomFile the root POM file (parent POM) to parse
     * @return a list of all parsed Project objects (parent and modules)
     * @throws JAXBException if there is an error during parsing
     * @throws IllegalArgumentException if rootPomFile is null or doesn't exist
     */
    @Override
    public List<Project> parseMultiModuleProject(File rootPomFile) throws JAXBException {
        log.atDebug()
            .addKeyValue("rootPomFile", rootPomFile.getAbsolutePath())
            .log("Starting multi-module project parsing");
        
        validateFile(rootPomFile);
        List<Project> projects = new ArrayList<>();
        Map<String, XmlProject> processedProjects = new HashMap<>();
        
        try {
            // Parse the root POM
            XmlProject rootProject = parseAndProcessProject(rootPomFile, processedProjects);
            
            log.atDebug()
                .addKeyValue("moduleCount", processedProjects.size())
                .log("Parsed all modules, processing relationships");
            
            // Process parent-child relationships
            processParentChildRelationships(processedProjects);
            
            // Resolve property placeholders in all projects
            for (XmlProject project : processedProjects.values()) {
                resolvePropertyPlaceholders(project);
            }
            
            // Convert XML projects to domain models
            for (XmlProject project : processedProjects.values()) {
                projects.add(project.toDomainModel());
            }
            
            log.atInfo()
                .addKeyValue("moduleCount", projects.size())
                .addKeyValue("rootPomFile", rootPomFile.getAbsolutePath())
                .log("Multi-module project parsed successfully");
            
            return projects;
        } catch (JAXBException e) {
            log.atError()
                .addKeyValue("rootPomFile", rootPomFile.getAbsolutePath())
                .log("Failed to parse multi-module project: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Parses a project and all its modules recursively.
     *
     * @param pomFile the POM file to parse
     * @param processedProjects map of already processed projects (to avoid duplicates)
     * @return the parsed XML project
     * @throws JAXBException if there is an error during parsing
     */
    private XmlProject parseAndProcessProject(File pomFile, Map<String, XmlProject> processedProjects) throws JAXBException {
        // Parse the POM file
        XmlProject project = xmlParser.parseXmlFile(pomFile, XmlProject.class);
        project.setRelativePath(pomFile.getAbsolutePath());
        
        // Add to processed projects
        String projectKey = getProjectKey(project);
        if (processedProjects.containsKey(projectKey)) {
            return processedProjects.get(projectKey);
        }
        processedProjects.put(projectKey, project);
        
        // Parse each module if this is a multi-module project
        List<String> modules = project.getModules();
        if (modules != null && !modules.isEmpty()) {
            // Handle case when file has no parent directory (e.g., when just "pom.xml" is specified)
            Path parentDir;
            if (pomFile.getParentFile() == null) {
                // Use current working directory when no parent directory is available
                parentDir = Paths.get("").toAbsolutePath();
            } else {
                parentDir = pomFile.getParentFile().toPath();
            }
            
            for (String modulePath : modules) {
                // Construct the path to the module's POM file
                Path modulePomPath = parentDir.resolve(Paths.get(modulePath, "pom.xml"));
                File modulePomFile = modulePomPath.toFile();
                
                if (modulePomFile.exists()) {
                    try {
                        XmlProject moduleProject = parseAndProcessProject(modulePomFile, processedProjects);
                    } catch (JAXBException e) {
                        // Log error but continue with other modules
                        log.atWarn()
                            .addKeyValue("modulePath", modulePath)
                            .log("Error parsing module, continuing with other modules: {}", e.getMessage(), e);
                    }
                } else {
                    log.atWarn()
                        .addKeyValue("modulePomPath", modulePomPath)
                        .log("Module POM file not found, skipping");
                }
            }
        }
        
        return project;
    }

    /**
     * Processes parent-child relationships between projects.
     *
     * @param projectMap map of all parsed projects
     */
    private void processParentChildRelationships(Map<String, XmlProject> projectMap) {
        for (XmlProject project : new ArrayList<>(projectMap.values())) {
            XmlProject.XmlParent parent = project.getParent();
            if (parent != null) {
                String parentKey = parent.getGroupId() + ":" + parent.getArtifactId() + ":" + parent.getVersion();
                XmlProject parentProject = projectMap.get(parentKey);
                
                if (parentProject != null) {
                    // Set the parent-child relationship
                    project.setParentProject(parentProject);
                } else {
                    // Try to load the parent POM if it's not already in our map
                    try {
                        File projectFile = new File(project.getRelativePath());
                        // Handle case when file has no parent directory
                        File projectDir;
                        if (projectFile.getParentFile() == null) {
                            // Use current working directory if no parent directory is available
                            projectDir = Paths.get("").toAbsolutePath().toFile();
                        } else {
                            projectDir = projectFile.getParentFile();
                        }
                        String relativePath = parent.getRelativePath();
                        File parentPomFile = null; // Declare parentPomFile here to be in scope for catch block
                        
                        // Better handling of parent relative path resolution
                        if (relativePath != null && (relativePath.equals("../..")
                                || relativePath.equals("../..\\")
                                || relativePath.equals("..\\.."))) { // ensure relativePath is not null before .equals
                            // Special case for parent reference to root project
                            String parentGroupId = parent.getGroupId();
                            String parentArtifactId = parent.getArtifactId();
                            String parentVersion = parent.getVersion();
                            
                            // Skip trying to load an actual file, just create a virtual parent
                            XmlProject virtualParent = new XmlProject();
                            virtualParent.setGroupId(parentGroupId);
                            virtualParent.setArtifactId(parentArtifactId);
                            virtualParent.setVersion(parentVersion);
                            virtualParent.setRelativePath("virtual-parent");
                            
                            String virtualParentKey = getProjectKey(virtualParent);
                            projectMap.put(virtualParentKey, virtualParent);
                            project.setParentProject(virtualParent);
                            
                            // Skip further processing for this parent
                            continue;
                        } else {
                            if (relativePath == null || relativePath.isEmpty()) {
                                parentPomFile = null; // Indicates not found via relative path or not specified
                            } else {
                                parentPomFile = new File(projectDir, relativePath);
                                if (!parentPomFile.exists() && !relativePath.endsWith("pom.xml")) {
                                    parentPomFile = new File(projectDir, relativePath + "/pom.xml");
                                }
                            }
                        }

                        if (parentPomFile != null && parentPomFile.exists() && parentPomFile.isFile()) {
                            XmlProject loadedParent = xmlParser.parseXmlFile(parentPomFile, XmlProject.class);
                            loadedParent.setRelativePath(parentPomFile.getAbsolutePath());
                            
                            String loadedParentKey = getProjectKey(loadedParent);
                            // Avoid overwriting an already processed project (e.g. root loaded via modules)
                            if (!projectMap.containsKey(loadedParentKey)) {
                                projectMap.put(loadedParentKey, loadedParent);
                            } else {
                                loadedParent = projectMap.get(loadedParentKey); // use existing one
                            }
                            
                            // Set the parent-child relationship
                            project.setParentProject(loadedParent);
                        } else if (relativePath != null && !relativePath.isEmpty() && (parentPomFile == null || !parentPomFile.exists())) {
                            // Only log error if relativePath was specified but not found
                            log.atWarn()
                                .addKeyValue("relativePath", relativePath)
                                .addKeyValue("project", getProjectKey(project))
                                .log("Parent POM specified by relativePath not found");
                        }
                        // If parentPomFile is null (empty relativePath) or not a file, silently skip loading.
                        // It's assumed to be an external parent or handled by other means (e.g. already in projectMap).
                    } catch (JAXBException e) { // Catch specific parsing errors
                        // Log an error if the parent POM cannot be parsed
                        log.atError()
                            .addKeyValue("project", getProjectKey(project))
                            .log("Error parsing parent POM: {}", e.getMessage(), e);
                    } catch (Exception e) { // Catch other unexpected errors during parent loading
                        log.atError()
                            .addKeyValue("project", getProjectKey(project))
                            .log("Unexpected error loading parent POM: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Gets a unique key for a project.
     *
     * @param project the XML project
     * @return a key in the format "groupId:artifactId:version"
     */
    private String getProjectKey(XmlProject project) {
        String groupId = project.getEffectiveGroupId();
        String artifactId = project.getArtifactId();
        String version = project.getEffectiveVersion();
        return groupId + ":" + artifactId + ":" + version;
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
            
            log.atDebug()
                .addKeyValue("dependency", dependency.getGroupId() + ":" + dependency.getArtifactId())
                .addKeyValue("originalVersion", version)
                .addKeyValue("resolvedVersion", resolvedVersion)
                .log("Property placeholder resolved in dependency");
        }
    }

    private void validateFile(File file) {
        if (file == null || !file.exists()) {
            log.atError()
                .addKeyValue("file", file != null ? file.getAbsolutePath() : "null")
                .log("POM file validation failed");
            throw new IllegalArgumentException("POM file must not be null and must exist");
        }
    }

    private void validateInputStream(InputStream inputStream) {
        if (inputStream == null) {
            log.atError()
                .log("POM input stream validation failed - stream is null");
            throw new IllegalArgumentException("Input stream must not be null");
        }
    }
}
