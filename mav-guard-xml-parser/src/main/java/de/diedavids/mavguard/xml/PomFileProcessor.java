package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Project;
import jakarta.xml.bind.JAXBException;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Interface defining operations for processing Maven POM files.
 */
public interface PomFileProcessor {
    /**
     * Parses a POM file from filesystem.
     *
     * @param pomFile the POM file to parse
     * @return the parsed Project object
     * @throws JAXBException if there is an error during parsing
     * @throws IllegalArgumentException if pomFile is null or doesn't exist
     */
    Project parsePomFile(File pomFile) throws JAXBException;

    /**
     * Parses a POM from an input stream.
     *
     * @param inputStream the input stream containing the POM XML
     * @return the parsed Project object
     * @throws JAXBException if there is an error during parsing
     * @throws IllegalArgumentException if inputStream is null
     */
    Project parsePomStream(InputStream inputStream) throws JAXBException;
    
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
    List<Project> parseMultiModuleProject(File rootPomFile) throws JAXBException;
}
