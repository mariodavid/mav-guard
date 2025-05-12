package de.diedavids.mavguard.xml.property;

import de.diedavids.mavguard.xml.model.XmlProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class MavenPropertyResolverTest {

    private MavenPropertyResolver propertyResolver;
    private XmlProject mockProject;
    private Map<String, String> properties;

    @BeforeEach
    void setUp() {
        propertyResolver = new MavenPropertyResolver();
        mockProject = Mockito.mock(XmlProject.class);
        properties = new HashMap<>();
        
        // Mock basic project properties
        when(mockProject.getGroupId()).thenReturn("com.example");
        when(mockProject.getArtifactId()).thenReturn("example-project");
        when(mockProject.getVersion()).thenReturn("1.0.0");
        when(mockProject.getName()).thenReturn("Example Project");
        when(mockProject.getPackaging()).thenReturn("jar");
        when(mockProject.getProperties()).thenReturn(properties);
    }
    
    private void addProperty(String name, String value) {
        properties.put(name, value);
    }

    @Test
    void shouldReturnOriginalValue_whenNoPlaceholder() {
        // Given
        String value = "3.2.0";
        
        // When
        String result = propertyResolver.resolveProperty(value, mockProject);
        
        // Then
        assertThat(result).isEqualTo("3.2.0");
    }
    
    @Test
    void shouldResolveSimpleProperty() {
        // Given
        addProperty("spring.boot.version", "3.2.0");
        String value = "${spring.boot.version}";
        
        // When
        String result = propertyResolver.resolveProperty(value, mockProject);
        
        // Then
        assertThat(result).isEqualTo("3.2.0");
    }
    
    @Test
    void shouldResolveProjectProperty() {
        // Given
        String value = "${project.version}";
        
        // When
        String result = propertyResolver.resolveProperty(value, mockProject);
        
        // Then
        assertThat(result).isEqualTo("1.0.0");
    }
    
    @Test
    void shouldResolveNestedProperty() {
        // Given
        addProperty("spring.version", "6.1.0");
        addProperty("spring.boot.version", "${spring.version}");
        String value = "${spring.boot.version}";
        
        // When
        String result = propertyResolver.resolveProperty(value, mockProject);
        
        // Then
        assertThat(result).isEqualTo("6.1.0");
    }
    
    @Test
    void shouldResolveMultiplePropertiesInString() {
        // Given
        addProperty("spring.version", "6.1.0");
        addProperty("spring.boot.version", "3.2.0");
        String value = "Spring: ${spring.version}, Spring Boot: ${spring.boot.version}";
        
        // When
        String result = propertyResolver.resolveProperty(value, mockProject);
        
        // Then
        assertThat(result).isEqualTo("Spring: 6.1.0, Spring Boot: 3.2.0");
    }
    
    @Test
    void shouldHandleUnknownProperty() {
        // Given
        String value = "${unknown.property}";
        
        // When
        String result = propertyResolver.resolveProperty(value, mockProject);
        
        // Then
        assertThat(result).isEqualTo("${unknown.property}");
    }
    
    @Test
    void shouldHandleNestedPropertyWithProjectReference() {
        // Given
        addProperty("artifact", "example-project");
        String value = "${project.${artifact}.version}";

        // When
        String result = propertyResolver.resolveProperty(value, mockProject);

        // Then
        // The inner property gets resolved, but our implementation doesn't support
        // fully dynamic resolution of project properties
        assertThat(result).isEqualTo(value);
    }
    
    @Test
    void shouldDetectPropertyPlaceholder() {
        // When/Then
        assertThat(propertyResolver.isPropertyPlaceholder("${spring.boot.version}")).isTrue();
        assertThat(propertyResolver.isPropertyPlaceholder("Spring ${version}")).isTrue();
        assertThat(propertyResolver.isPropertyPlaceholder("1.0.0")).isFalse();
        assertThat(propertyResolver.isPropertyPlaceholder(null)).isFalse();
    }
}