package de.diedavids.mavguard.xml.property;

import de.diedavids.mavguard.xml.model.XmlProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of PropertyResolver that resolves Maven property placeholders.
 */
public class MavenPropertyResolver implements PropertyResolver {

    private static final Logger log = LoggerFactory.getLogger(MavenPropertyResolver.class);
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final int MAX_RESOLUTION_DEPTH = 10; // Prevent infinite recursion

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveProperty(String value, XmlProject project) {
        if (value == null || project == null || !isPropertyPlaceholder(value)) {
            return value;
        }

        return resolvePropertyInternal(value, project, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPropertyPlaceholder(String value) {
        return value != null && PROPERTY_PATTERN.matcher(value).find();
    }

    /**
     * Internal method to resolve properties with depth tracking to prevent infinite recursion.
     *
     * @param value the value containing property placeholders
     * @param project the XML project
     * @param depth current resolution depth
     * @return the resolved value
     */
    private String resolvePropertyInternal(String value, XmlProject project, int depth) {
        if (depth > MAX_RESOLUTION_DEPTH) {
            return value; // Prevent infinite recursion
        }

        Matcher matcher = PROPERTY_PATTERN.matcher(value);
        if (!matcher.find()) {
            return value;
        }

        // If the entire string is a property placeholder, resolve it directly
        if (matcher.start() == 0 && matcher.end() == value.length()) {
            String propertyName = matcher.group(1);

            // Handle nested property placeholder first if present
            if (propertyName.contains("${")) {
                Matcher nestedMatcher = PROPERTY_PATTERN.matcher(propertyName);
                while (nestedMatcher.find()) {
                    String nestedProp = nestedMatcher.group(1);
                    String nestedValue = lookupProperty(nestedProp, project, depth + 1);
                    if (nestedValue != null) {
                        String before = propertyName.substring(0, nestedMatcher.start());
                        String after = propertyName.substring(nestedMatcher.end());
                        propertyName = before + nestedValue + after;
                        // Refresh matcher with new property name
                        nestedMatcher = PROPERTY_PATTERN.matcher(propertyName);
                    }
                }
            }

            String propertyValue = lookupProperty(propertyName, project, depth);
            return propertyValue != null ? propertyValue : value;
        }

        // If the string contains multiple placeholders or text mixed with placeholders,
        // perform replacement for each placeholder
        StringBuffer result = new StringBuffer();
        matcher.reset();

        while (matcher.find()) {
            String propertyName = matcher.group(1);

            // Handle nested placeholders
            if (propertyName.contains("${")) {
                Matcher nestedMatcher = PROPERTY_PATTERN.matcher(propertyName);
                while (nestedMatcher.find()) {
                    String nestedProp = nestedMatcher.group(1);
                    String nestedValue = lookupProperty(nestedProp, project, depth + 1);
                    if (nestedValue != null) {
                        String before = propertyName.substring(0, nestedMatcher.start());
                        String after = propertyName.substring(nestedMatcher.end());
                        propertyName = before + nestedValue + after;
                        // Refresh matcher with new property name
                        nestedMatcher = PROPERTY_PATTERN.matcher(propertyName);
                    }
                }
            }

            String replacement = lookupProperty(propertyName, project, depth);

            // If property not found, keep the original placeholder
            if (replacement == null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Looks up a property in the project's properties section.
     *
     * @param propertyName the name of the property to look up
     * @param project the XML project
     * @param depth current resolution depth
     * @return the property value, or null if not found
     */
    private String lookupProperty(String propertyName, XmlProject project, int depth) {
        // Handle nested property references like ${project.${another.property}.version}
        if (isPropertyPlaceholder(propertyName)) {
            String resolvedPropertyName = resolvePropertyInternal(propertyName, project, depth + 1);
            // For nested complex properties like ${project.${artifact}.version}
            // which become ${project.example-project.version}, we can't fully resolve them
            // So we return them as is
            if (resolvedPropertyName.startsWith("${project.") && resolvedPropertyName.contains("}")) {
                return resolvedPropertyName;
            }
            propertyName = resolvedPropertyName;
        }

        // Split the property name by dots to handle structured properties
        // like "project.version" or "project.build.finalName"
        String[] parts = propertyName.split("\\.");

        // Special handling for project.* properties
        if (parts.length > 1 && "project".equals(parts[0])) {
            String value = getProjectProperty(parts, project);
            if (value != null) {
                return value;
            }
        }

        // Look up the property in the properties section
        Map<String, String> properties = project.getProperties();
        if (properties == null) {
            log.atDebug()
                .addKeyValue("propertyName", propertyName)
                .log("No properties section found in project");
            return null;
        }

        String propertyValue = properties.get(propertyName);
        
        if (propertyValue == null) {
            log.atDebug()
                .addKeyValue("propertyName", propertyName)
                .log("Property not found in project properties");
        }

        // If property value contains other property references, resolve them recursively
        if (propertyValue != null && isPropertyPlaceholder(propertyValue)) {
            propertyValue = resolvePropertyInternal(propertyValue, project, depth + 1);
        }

        return propertyValue;
    }

    /**
     * Gets a project property, such as project.version, project.groupId, etc.
     *
     * @param parts the property name split by dots
     * @param project the XML project
     * @return the property value, or null if not found
     */
    private String getProjectProperty(String[] parts, XmlProject project) {
        if (parts.length < 2) {
            return null;
        }

        String propertyType = parts[1];

        // Handle basic project properties
        if ("version".equals(propertyType)) {
            return project.getVersion();
        } else if ("groupId".equals(propertyType)) {
            return project.getGroupId();
        } else if ("artifactId".equals(propertyType)) {
            return project.getArtifactId();
        } else if ("name".equals(propertyType)) {
            return project.getName();
        } else if ("packaging".equals(propertyType)) {
            return project.getPackaging();
        }

        // Complex nested properties are not supported in this implementation
        // Additional properties could be added as needed

        return null;
    }
}