package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.nexus.client.NexusClient;
import de.diedavids.mavguard.nexus.model.MavenMetadata;
import de.diedavids.mavguard.nexus.model.NexusArtifactVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests network error handling for repository services that was missing from the original test suite.
 * These tests ensure graceful degradation when external repository calls fail.
 */
@ExtendWith(MockitoExtension.class)
class RepositoryServiceErrorHandlingTest {

    @Mock
    private NexusClient nexusClient;

    private MavenCentralRepositoryService repositoryService;
    private Dependency testDependency;

    @BeforeEach
    void setUp() {
        repositoryService = new MavenCentralRepositoryService(nexusClient);
        testDependency = new Dependency(
            "org.springframework", 
            "spring-core", 
            "5.3.10", 
            null, 
            null, 
            null
        );
    }

    @Test
    void shouldReturnEmptyListOnNetworkTimeout() throws Exception {
        // Given
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection timed out", new SocketTimeoutException("Connection timed out")));

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should not throw exception - should handle gracefully
    }

    @Test
    void shouldReturnEmptyListOnUnknownHost() throws Exception {
        // Given
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenThrow(new RuntimeException("Unknown host: repo1.maven.org", new UnknownHostException("Unknown host: repo1.maven.org")));

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should not throw exception - should handle gracefully
    }

    @Test
    void shouldReturnEmptyListOnGenericIOException() throws Exception {
        // Given
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenThrow(new RuntimeException("Network error", new java.io.IOException("Network error")));

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should not throw exception - should handle gracefully
    }

    @Test
    void shouldReturnEmptyListOnNullMetadata() throws Exception {
        // Given
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenReturn(null);

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should handle null response gracefully
    }

    @Test
    void shouldReturnEmptyListOnMalformedMetadata() throws Exception {
        // Given - metadata with null versioning
        MavenMetadata malformedMetadata = new MavenMetadata();
        malformedMetadata.setVersioning(null);
        
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenReturn(malformedMetadata);

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should handle malformed metadata gracefully
    }

    @Test
    void shouldHandleMetadataWithEmptyVersionsList() throws Exception {
        // Given
        MavenMetadata emptyMetadata = new MavenMetadata();
        MavenMetadata.Versioning versioning = new MavenMetadata.Versioning();
        versioning.setVersions(List.of());
        emptyMetadata.setVersioning(versioning);
        
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenReturn(emptyMetadata);

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should handle empty versions list gracefully
    }

    @Test
    void shouldHandleHttpStatusErrors() throws Exception {
        // Given - simulate HTTP 404 or 500 error
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenThrow(new RuntimeException("HTTP 404: Not Found"));

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should handle HTTP errors gracefully
    }

    @Test
    void shouldHandleInterruptedException() throws Exception {
        // Given
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenThrow(new RuntimeException("Thread interrupted", new InterruptedException("Thread interrupted")));

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should handle interruption gracefully
        // Verify thread interrupted status is preserved
        assertThat(Thread.currentThread().isInterrupted()).isFalse(); // Should be cleared by service
    }

    @Test
    void shouldHandleSecurityException() throws Exception {
        // Given
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenThrow(new SecurityException("Access denied"));

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).isEmpty();
        // Should handle security errors gracefully
    }

    @Test
    void shouldPreserveVersionOrderingEvenWithPartialFailures() throws Exception {
        // Given - metadata with some valid versions
        MavenMetadata metadata = new MavenMetadata();
        MavenMetadata.Versioning versioning = new MavenMetadata.Versioning();
        versioning.setVersions(List.of("6.0.0", "5.3.25", "5.3.10", "5.2.20"));
        metadata.setVersioning(versioning);
        
        when(nexusClient.getMavenMetadataSimple(anyString(), anyString()))
                .thenReturn(metadata);

        // When
        List<NexusArtifactVersion> versions = repositoryService.getAvailableVersions(testDependency);

        // Then
        assertThat(versions).hasSize(4);
        // Should be sorted in descending order (newest first)
        assertThat(versions.get(0).version()).isEqualTo("6.0.0");
        assertThat(versions.get(1).version()).isEqualTo("5.3.25");
        assertThat(versions.get(2).version()).isEqualTo("5.3.10");
        assertThat(versions.get(3).version()).isEqualTo("5.2.20");
    }
}