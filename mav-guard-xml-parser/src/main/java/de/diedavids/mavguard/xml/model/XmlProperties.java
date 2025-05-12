package de.diedavids.mavguard.xml.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML model class for Maven properties section.
 * This class handles Maven properties which are represented as custom XML elements.
 */
@XmlRootElement(name = "properties", namespace = "http://maven.apache.org/POM/4.0.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlProperties {

    // Use XmlAnyElement to capture all property elements
    @XmlAnyElement
    private List<Element> propertyElements;

    /**
     * Default constructor required by JAXB.
     */
    public XmlProperties() {
        this.propertyElements = new ArrayList<>();
    }

    /**
     * Gets the property elements.
     *
     * @return the list of XML property elements
     */
    public List<Element> getPropertyElements() {
        return propertyElements != null ? propertyElements : new ArrayList<>();
    }

    /**
     * Converts the XML property elements to a property map.
     *
     * @return a map of property names to property values
     */
    public Map<String, String> getPropertyMap() {
        Map<String, String> propertyMap = new HashMap<>();
        
        if (propertyElements != null) {
            for (Element element : propertyElements) {
                String propertyName = element.getLocalName();
                String propertyValue = element.getTextContent();
                
                if (propertyName != null && !propertyName.isEmpty()) {
                    propertyMap.put(propertyName, propertyValue);
                }
            }
        }
        
        return propertyMap;
    }
}