package de.diedavids.mavguard.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class XmlParserTest {

    private static final String TEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><testData><name>Test Name</name><value>42</value></testData>";

    @Test
    void shouldParseXmlStream_withValidXml() throws JAXBException {
        // Given
        InputStream inputStream = new ByteArrayInputStream(TEST_XML.getBytes(StandardCharsets.UTF_8));
        XmlParser xmlParser = new XmlParser();

        // When
        TestData result = xmlParser.parseXmlStream(inputStream, TestData.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Name");
        assertThat(result.value()).isEqualTo(42);
    }

    @Test
    void shouldParseXmlFile_withValidXml(@TempDir Path tempDir) throws JAXBException, IOException {
        // Given
        Path xmlFile = tempDir.resolve("test.xml");
        Files.writeString(xmlFile, TEST_XML);
        XmlParser xmlParser = new XmlParser();

        // When
        TestData result = xmlParser.parseXmlFile(xmlFile.toFile(), TestData.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Name");
        assertThat(result.value()).isEqualTo(42);
    }

    @XmlRootElement(name = "testData")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestData {
        @XmlElement
        private String name;

        @XmlElement
        private int value;

        // Default constructor required by JAXB
        public TestData() {
        }

        // Constructor for immutability
        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String name() {
            return name;
        }

        public int value() {
            return value;
        }
    }
}
