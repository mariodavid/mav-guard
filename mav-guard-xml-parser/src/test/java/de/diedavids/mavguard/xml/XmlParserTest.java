package de.diedavids.mavguard.xml;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class XmlParserTest {

    @Test
    void testParseXmlFile(@TempDir Path tempDir) throws JAXBException, IOException {
        // Create a temporary XML file
        File xmlFile = tempDir.resolve("test.xml").toFile();
        Files.writeString(xmlFile.toPath(), "<test-data><value>test</value><number>42</number></test-data>");

        // Parse the XML file
        XmlParser parser = new XmlParser();
        TestData result = parser.parseXmlFile(xmlFile, TestData.class);

        // Verify the result
        assertThat(result.getValue()).isEqualTo("test");
        assertThat(result.getNumber()).isEqualTo(42);
    }

    @Test
    void testParseXmlStream() throws JAXBException {
        // Create a test XML string
        String xml = "<test-data><value>test</value><number>42</number></test-data>";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());

        // Parse the XML stream
        XmlParser parser = new XmlParser();
        TestData result = parser.parseXmlStream(inputStream, TestData.class);

        // Verify the result
        assertThat(result.getValue()).isEqualTo("test");
        assertThat(result.getNumber()).isEqualTo(42);
    }

    @XmlRootElement(name = "test-data")
    @XmlAccessorType(XmlAccessType.FIELD)
    static class TestData {
        @XmlElement
        private String value;

        @XmlElement
        private int number;

        public String getValue() {
            return value;
        }

        public int getNumber() {
            return number;
        }
    }
}
