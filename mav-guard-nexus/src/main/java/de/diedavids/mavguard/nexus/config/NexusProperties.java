package de.diedavids.mavguard.nexus.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Nexus repository connection.
 */
@ConfigurationProperties(prefix = "mavguard.repository")
@Validated
@ValidNexusProperties
public record NexusProperties(
    @NotNull(message = "Repository type must not be null")
    RepositoryType type,
    
    @NotBlank(message = "Base URL must not be empty")
    @Pattern(regexp = "^(http|https)://.*", message = "Base URL must start with http:// or https://")
    String baseUrl,
    
    String username,
    
    String password,
    
    String repository,
    
    @NotNull(message = "Connection timeout must not be null")
    @Positive(message = "Connection timeout must be positive")
    Integer connectionTimeout,
    
    @NotNull(message = "Read timeout must not be null")
    @Positive(message = "Read timeout must be positive")
    Integer readTimeout
) {
    /**
     * Creates a new NexusProperties with default values.
     */
    public NexusProperties {
        if (connectionTimeout == null) {
            connectionTimeout = 5000;
        }
        if (readTimeout == null) {
            readTimeout = 10000;
        }
    }
    
    /**
     * Checks if this configuration is for Maven Central.
     */
    public boolean isMavenCentral() {
        return type == RepositoryType.MAVEN_CENTRAL;
    }
    
    /**
     * Checks if this configuration is for Nexus.
     */
    public boolean isNexus() {
        return type == RepositoryType.NEXUS;
    }
}