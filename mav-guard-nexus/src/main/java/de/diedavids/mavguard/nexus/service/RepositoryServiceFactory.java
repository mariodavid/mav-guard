package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.nexus.config.NexusProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for creating the appropriate repository service based on configuration.
 */
@Component
public class RepositoryServiceFactory {

    private final List<RepositoryService> repositoryServices;
    private final NexusProperties properties;

    public RepositoryServiceFactory(List<RepositoryService> repositoryServices, NexusProperties properties) {
        this.repositoryServices = repositoryServices;
        this.properties = properties;
    }

    /**
     * Creates the appropriate repository service based on the configured repository type.
     *
     * @return the repository service for the configured type
     * @throws IllegalStateException if no matching service is found
     */
    public RepositoryService createRepositoryService() {
        String configuredType = properties.type().name();
        
        return repositoryServices.stream()
                .filter(service -> service.getRepositoryType().equals(configuredType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No repository service found for type: " + configuredType));
    }
}