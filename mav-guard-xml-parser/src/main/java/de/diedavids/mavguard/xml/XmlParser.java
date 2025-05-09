package de.diedavids.mavguard.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;

/**
 * Utility class for parsing XML files using JAXB.
 */
public class XmlParser {

    /**
     * Parses an XML file into the specified class type.
     *
     * @param xmlFile the XML file to parse
     * @param type the class type to parse into
     * @return the parsed object
     * @throws JAXBException if there is an error during parsing
     */
    public <T> T parseXmlFile(File xmlFile, Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return type.cast(unmarshaller.unmarshal(xmlFile));
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
        JAXBContext jaxbContext = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return type.cast(unmarshaller.unmarshal(inputStream));
    }
}