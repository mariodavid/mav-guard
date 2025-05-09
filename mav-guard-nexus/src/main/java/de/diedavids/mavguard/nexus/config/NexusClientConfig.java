package de.diedavids.mavguard.nexus.config;

import de.diedavids.mavguard.nexus.client.NexusClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuration for Nexus HTTP client.
 */
@Configuration
@EnableConfigurationProperties(NexusProperties.class)
public class NexusClientConfig {

    /**
     * Creates a Nexus HTTP client using Spring 6 HTTP Interface with RestClient.
     * 
     * @param properties the Nexus properties
     * @return a configured Nexus client
     */
    @Bean
    public NexusClient nexusClient(NexusProperties properties) {
        // Create request factory with timeouts
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectionTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        
        // Create rest client with base URL and timeouts
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeaders(headers -> headers.add("Accept", "application/json"))
                .requestFactory(requestFactory);
        
        // Add basic auth if credentials are provided
        if (properties.username() != null && properties.password() != null) {
            builder.defaultHeaders(headers -> 
                headers.setBasicAuth(properties.username(), properties.password()));
        }
        
        RestClient restClient = builder.build();
        
        // Create HttpServiceProxyFactory using the RestClient via adapter
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        
        HttpServiceProxyFactory serviceFactory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        // Create the NexusClient instance
        return serviceFactory.createClient(NexusClient.class);
    }
}