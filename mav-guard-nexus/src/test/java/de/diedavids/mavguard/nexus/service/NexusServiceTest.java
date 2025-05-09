package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.nexus.client.NexusClient;
import de.diedavids.mavguard.nexus.config.NexusProperties;
import de.diedavids.mavguard.nexus.model.MavenMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NexusServiceTest {

    @Mock
    private NexusClient nexusClient;

    @Mock
    private NexusProperties properties;

    @InjectMocks
    private NexusService nexusService;

    private Dependency testDependency;
    private MavenMetadata testMetadata;

    @BeforeEach
    void setUp() {
        testDependency = new Dependency(
                "org.springframework.boot",
                "spring-boot-starter",
                "3.1.0",
                "compile",
                false,
                "jar"
        );

        // Create test metadata with versions
        testMetadata = new MavenMetadata();
        MavenMetadata.Versioning versioning = new MavenMetadata.Versioning();
        versioning.setLatest("3.2.0");
        versioning.setRelease("3.2.0");
        versioning.setVersions(List.of("3.2.0", "3.1.5", "3.1.0"));
        testMetadata.setVersioning(versioning);

        when(properties.repository()).thenReturn("private");
    }

    @Test
    void shouldReturnAvailableVersions() {
        // Given
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter")))
                .thenReturn(testMetadata);

        // When
        List<String> result = nexusService.getAvailableVersions(testDependency);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("3.2.0", "3.1.5", "3.1.0");
    }

    @Test
    void shouldReturnLatestVersion() {
        // Given
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter")))
                .thenReturn(testMetadata);

        // When
        Optional<String> latestVersion = nexusService.getLatestVersion(testDependency);

        // Then
        assertThat(latestVersion).isPresent();
        assertThat(latestVersion.get()).isEqualTo("3.2.0");
        assertThat(nexusService.hasNewerVersion(testDependency)).isTrue();
    }
    
    @Test
    void shouldNotReportNewerVersionWhenCurrentIsLatest() {
        // Given
        testDependency = new Dependency(
                "org.springframework.boot",
                "spring-boot-starter",
                "3.2.0", // Same as latest
                "compile",
                false,
                "jar"
        );
        
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter")))
                .thenReturn(testMetadata);

        // When/Then
        assertThat(nexusService.hasNewerVersion(testDependency)).isFalse();
    }

    @Test
    void shouldHandleEmptyMetadata() {
        // Given
        MavenMetadata emptyMetadata = new MavenMetadata();
        MavenMetadata.Versioning emptyVersioning = new MavenMetadata.Versioning();
        emptyVersioning.setVersions(List.of());
        emptyMetadata.setVersioning(emptyVersioning);
        
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter")))
                .thenReturn(emptyMetadata);

        // When
        List<String> result = nexusService.getAvailableVersions(testDependency);

        // Then
        assertThat(result).isEmpty();
        assertThat(nexusService.getLatestVersion(testDependency)).isEmpty();
        assertThat(nexusService.hasNewerVersion(testDependency)).isFalse();
    }

    @Test
    void shouldHandleExceptions() {
        // Given
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter")))
                .thenThrow(new RuntimeException("Connection error"));

        // When
        List<String> result = nexusService.getAvailableVersions(testDependency);

        // Then
        assertThat(result).isEmpty();
        assertThat(nexusService.getLatestVersion(testDependency)).isEmpty();
        assertThat(nexusService.hasNewerVersion(testDependency)).isFalse();
    }
}