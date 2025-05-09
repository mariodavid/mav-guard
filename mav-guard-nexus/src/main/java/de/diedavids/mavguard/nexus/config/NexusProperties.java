package de.diedavids.mavguard.nexus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Nexus repository connection.
 */
@ConfigurationProperties(prefix = "mavguard.nexus")
public record NexusProperties(
    String baseUrl,
    String username,
    String password,
    String repository,
    Integer connectionTimeout,
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
}