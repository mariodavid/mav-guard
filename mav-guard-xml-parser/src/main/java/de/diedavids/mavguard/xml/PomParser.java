package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.xml.model.XmlProject;
import jakarta.xml.bind.JAXBException;

import java.io.File;
import java.io.InputStream;

/**
 * Parser for Maven POM files.
 */
public class PomParser implements PomFileProcessor {

    private final XmlParser xmlParser;

    public PomParser() {
        this.xmlParser = new XmlParser();
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
        return xmlProject.toDomainModel();
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
