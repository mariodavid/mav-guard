package de.diedavids.mavguard.config;

import de.diedavids.mavguard.xml.PomParser;
import de.diedavids.mavguard.xml.XmlParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for XML parser beans.
 */
@Configuration
public class XmlParserConfig {

    /**
     * Creates an XmlParser bean.
     *
     * @return the XmlParser instance
     */
    @Bean
    public XmlParser xmlParser() {
        return new XmlParser();
    }

    /**
     * Creates a PomParser bean.
     *
     * @param xmlParser the XML parser to use
     * @return the PomParser instance
     */
    @Bean
    public PomParser pomParser(XmlParser xmlParser) {
        return new PomParser(xmlParser);
    }
}
