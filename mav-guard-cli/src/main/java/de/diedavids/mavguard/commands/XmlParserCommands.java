package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.xml.XmlParser;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Shell commands for XML parsing operations.
 */
@ShellComponent
public class XmlParserCommands {

    private final XmlParser xmlParser;

    public XmlParserCommands() {
        this.xmlParser = new XmlParser();
    }

    @ShellMethod(key = "parse-xml", value = "Parse an XML file")
    public String parseXml(@ShellOption(help = "Path to the XML file") String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "File not found: " + filePath;
            }

            // For demonstration purposes, we'll try to parse as SimpleXmlData
            // In a real application, you might want to determine the type dynamically
            SimpleXmlData data = xmlParser.parseXmlFile(file, SimpleXmlData.class);
            return "Successfully parsed XML file. Content: " + data.toString();
        } catch (JAXBException e) {
            return "Error parsing XML: " + e.getMessage();
        }
    }

    @ShellMethod(key = "parse-xml-stream", value = "Parse an XML file using stream")
    public String parseXmlStream(@ShellOption(help = "Path to the XML file") String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "File not found: " + filePath;
            }

            FileInputStream inputStream = new FileInputStream(file);
            SimpleXmlData data = xmlParser.parseXmlStream(inputStream, SimpleXmlData.class);
            return "Successfully parsed XML stream. Content: " + data.toString();
        } catch (JAXBException | FileNotFoundException e) {
            return "Error parsing XML: " + e.getMessage();
        }
    }

    /**
     * Simple data class for XML parsing demonstration.
     */
    @XmlRootElement(name = "data")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SimpleXmlData {
        @XmlElement
        private String name;

        @XmlElement
        private String value;

        @Override
        public String toString() {
            return "SimpleXmlData{name='" + name + "', value='" + value + "'}";
        }
    }
}