package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Project;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.InputStream;

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
}
