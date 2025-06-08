package de.diedavids.mavguard.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.InputStream;

/**
 * Utility class for parsing XML files using JAXB.
 */
public class XmlParser {

    private static final Logger log = LoggerFactory.getLogger(XmlParser.class);

    /**
     * Parses an XML file into the specified class type.
     *
     * @param xmlFile the XML file to parse
     * @param type the class type to parse into
     * @return the parsed object
     * @throws JAXBException if there is an error during parsing
     */
    public <T> T parseXmlFile(File xmlFile, Class<T> type) throws JAXBException {
        log.atDebug()
            .addKeyValue("xmlFile", xmlFile.getAbsolutePath())
            .addKeyValue("targetType", type.getSimpleName())
            .log("Starting XML file parsing");
            
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(type);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            T result = type.cast(unmarshaller.unmarshal(xmlFile));
            
            log.atDebug()
                .addKeyValue("xmlFile", xmlFile.getAbsolutePath())
                .addKeyValue("targetType", type.getSimpleName())
                .log("XML file parsed successfully");
                
            return result;
        } catch (JAXBException e) {
            log.atError()
                .addKeyValue("xmlFile", xmlFile.getAbsolutePath())
                .addKeyValue("targetType", type.getSimpleName())
                .log("Failed to parse XML file: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Parses an XML input stream into the specified class type.
     *
     * @param inputStream the XML input stream to parse
     * @param type the class type to parse into
     * @return the parsed object
     * @throws JAXBException if there is an error during parsing
     */
    public <T> T parseXmlStream(InputStream inputStream, Class<T> type) throws JAXBException {
        log.atDebug()
            .addKeyValue("targetType", type.getSimpleName())
            .log("Starting XML stream parsing");
            
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(type);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            T result = type.cast(unmarshaller.unmarshal(inputStream));
            
            log.atDebug()
                .addKeyValue("targetType", type.getSimpleName())
                .log("XML stream parsed successfully");
                
            return result;
        } catch (JAXBException e) {
            log.atError()
                .addKeyValue("targetType", type.getSimpleName())
                .log("Failed to parse XML stream: {}", e.getMessage(), e);
            throw e;
        }
    }
}