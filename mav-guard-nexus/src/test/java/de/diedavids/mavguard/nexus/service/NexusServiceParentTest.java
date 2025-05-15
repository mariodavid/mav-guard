package de.diedavids.mavguard.nexus.service;

import de.diedavids.mavguard.model.Project;
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
class NexusServiceParentTest {

    @Mock
    private NexusClient nexusClient;

    @Mock
    private NexusProperties properties;

    @InjectMocks
    private NexusService nexusService;

    private Project.Parent testParent;
    private MavenMetadata testMetadata;

    @BeforeEach
    void setUp() {
        testParent = new Project.Parent(
                "org.springframework.boot",
                "spring-boot-starter-parent",
                "3.1.0",
                "../pom.xml"
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
    void shouldReturnAvailableParentVersions() {
        // Given
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter-parent")))
                .thenReturn(testMetadata);

        // When
        List<String> result = nexusService.getAvailableParentVersions(testParent);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("3.2.0", "3.1.5", "3.1.0");
    }

    @Test
    void shouldReturnLatestParentVersion() {
        // Given
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter-parent")))
                .thenReturn(testMetadata);

        // When
        Optional<String> latestVersion = nexusService.getLatestParentVersion(testParent);

        // Then
        assertThat(latestVersion).isPresent();
        assertThat(latestVersion.get()).isEqualTo("3.2.0");
        assertThat(nexusService.hasNewerParentVersion(testParent)).isTrue();
    }
    
    @Test
    void shouldNotReportNewerParentVersionWhenCurrentIsLatest() {
        // Given
        testParent = new Project.Parent(
                "org.springframework.boot",
                "spring-boot-starter-parent",
                "3.2.0", // Same as latest
                "../pom.xml"
        );
        
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter-parent")))
                .thenReturn(testMetadata);

        // When/Then
        assertThat(nexusService.hasNewerParentVersion(testParent)).isFalse();
    }

    @Test
    void shouldHandleEmptyMetadataForParent() {
        // Given
        MavenMetadata emptyMetadata = new MavenMetadata();
        MavenMetadata.Versioning emptyVersioning = new MavenMetadata.Versioning();
        emptyVersioning.setVersions(List.of());
        emptyMetadata.setVersioning(emptyVersioning);
        
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter-parent")))
                .thenReturn(emptyMetadata);

        // When
        List<String> result = nexusService.getAvailableParentVersions(testParent);

        // Then
        assertThat(result).isEmpty();
        assertThat(nexusService.getLatestParentVersion(testParent)).isEmpty();
        assertThat(nexusService.hasNewerParentVersion(testParent)).isFalse();
    }

    @Test
    void shouldHandleExceptionsForParent() {
        // Given
        when(nexusClient.getMavenMetadata(anyString(), eq("org/springframework/boot"), eq("spring-boot-starter-parent")))
                .thenThrow(new RuntimeException("Connection error"));

        // When
        List<String> result = nexusService.getAvailableParentVersions(testParent);

        // Then
        assertThat(result).isEmpty();
        assertThat(nexusService.getLatestParentVersion(testParent)).isEmpty();
        assertThat(nexusService.hasNewerParentVersion(testParent)).isFalse();
    }
}