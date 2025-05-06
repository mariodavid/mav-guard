package de.diedavids.mavguard.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
     * @param clazz the class type to parse into
     * @param <T> the type of the class
     * @return the parsed object
     * @throws JAXBException if there is an error during parsing
     */
    public <T> T parseXmlFile(File xmlFile, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return clazz.cast(unmarshaller.unmarshal(xmlFile));
    }

    /**
     * Parses an XML input stream into the specified class type.
     *
     * @param inputStream the XML input stream to parse
     * @param clazz the class type to parse into
     * @param <T> the type of the class
     * @return the parsed object
     * @throws JAXBException if there is an error during parsing
     */
    public <T> T parseXmlStream(InputStream inputStream, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return clazz.cast(unmarshaller.unmarshal(inputStream));
    }
}