package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.nexus.config.NexusProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for creating the appropriate repository service based on configuration.
 */
@Component
public class RepositoryServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(RepositoryServiceFactory.class);

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
        
        log.atDebug()
            .addKeyValue("configuredType", configuredType)
            .addKeyValue("availableServices", repositoryServices.size())
            .log("Creating repository service");
        
        RepositoryService service = repositoryServices.stream()
                .filter(s -> s.getRepositoryType().equals(configuredType))
                .findFirst()
                .orElseThrow(() -> {
                    log.atError()
                        .addKeyValue("configuredType", configuredType)
                        .log("No repository service found for configured type");
                    return new IllegalStateException(
                            "No repository service found for type: " + configuredType);
                });
        
        log.atInfo()
            .addKeyValue("repositoryType", configuredType)
            .addKeyValue("serviceClass", service.getClass().getSimpleName())
            .log("Repository service created successfully");
            
        return service;
    }
}