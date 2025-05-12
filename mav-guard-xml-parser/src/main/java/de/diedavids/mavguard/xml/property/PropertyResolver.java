package de.diedavids.mavguard.xml.property;

import de.diedavids.mavguard.xml.model.XmlProject;

/**
 * Interface for resolving Maven properties in a POM file.
 */
public interface PropertyResolver {

    /**
     * Resolves a property placeholder (e.g. ${spring.boot.version}) to its actual value.
     *
     * @param propertyPlaceholder the property placeholder to resolve
     * @param project the XML project containing property definitions
     * @return the resolved property value, or the original placeholder if resolution fails
     */
    String resolveProperty(String propertyPlaceholder, XmlProject project);

    /**
     * Checks if a string contains a Maven property placeholder.
     *
     * @param value the string to check
     * @return true if the string contains a property placeholder (${...}), false otherwise
     */
    boolean isPropertyPlaceholder(String value);
}