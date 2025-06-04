package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Test implementation of DependencyVersionService for integration tests.
 * This replaces mocking with a real implementation that returns predefined test data.
 */
@Service
@Profile("mock-test")
public class TestDependencyVersionService implements DependencyVersionService {
    
    private final Map<String, List<String>> versionDatabase = new HashMap<>();
    private final Map<String, String> latestVersions = new HashMap<>();
    
    public TestDependencyVersionService() {
        // Initialize test data
        // Spring Boot versions
        addVersionData("org.springframework.boot:spring-boot-starter", 
            Arrays.asList("3.2.0", "3.1.5", "3.0.11", "2.7.5", "2.7.0", "2.6.15"),
            "3.2.0");
            
        // Spring Framework versions
        addVersionData("org.springframework:spring-core",
            Arrays.asList("6.1.0", "6.0.13", "5.3.30", "5.3.25", "5.3.10", "5.2.25"),
            "6.1.0");
            
        addVersionData("org.springframework:spring-context",
            Arrays.asList("6.1.0", "6.0.13", "5.3.30", "5.3.25", "5.3.10", "5.2.25"),
            "6.1.0");
            
        // SLF4J versions
        addVersionData("org.slf4j:slf4j-api",
            Arrays.asList("2.0.9", "1.7.36", "1.7.32", "1.7.30", "1.7.25"),
            "2.0.9");
            
        // Apache Commons versions
        addVersionData("org.apache.commons:commons-lang3",
            Arrays.asList("3.14.0", "3.13.0", "3.12.0", "3.11", "3.10"),
            "3.14.0");
            
        addVersionData("org.apache.commons:commons-collections4",
            Arrays.asList("4.5.0-M1", "4.4", "4.3", "4.2", "4.1"),
            "4.4"); // Latest stable, not milestone
            
        // Local module - no updates available
        addVersionData("com.example:module-a",
            Arrays.asList("1.0.0"),
            "1.0.0");
    }
    
    private void addVersionData(String coordinates, List<String> versions, String latest) {
        versionDatabase.put(coordinates, versions);
        latestVersions.put(coordinates, latest);
    }
    
    @Override
    public List<String> getAvailableVersions(Dependency dependency) {
        String key = dependency.groupId() + ":" + dependency.artifactId();
        return versionDatabase.getOrDefault(key, Collections.emptyList());
    }
    
    @Override
    public Optional<String> getLatestVersion(Dependency dependency) {
        String key = dependency.groupId() + ":" + dependency.artifactId();
        
        // For test purposes, return newer versions only for specific cases
        if ("org.springframework.boot".equals(dependency.groupId()) && 
            "spring-boot-starter".equals(dependency.artifactId()) &&
            "2.7.0".equals(dependency.version())) {
            return Optional.of("2.7.5");
        }
        
        if ("org.springframework".equals(dependency.groupId()) &&
            ("spring-core".equals(dependency.artifactId()) || "spring-context".equals(dependency.artifactId())) &&
            "5.3.10".equals(dependency.version())) {
            return Optional.of("5.3.25");
        }
        
        if ("org.slf4j".equals(dependency.groupId()) &&
            "slf4j-api".equals(dependency.artifactId()) &&
            ("1.7.30".equals(dependency.version()) || "1.7.32".equals(dependency.version()))) {
            return Optional.of("1.7.36");
        }
        
        // For all other cases, return the current version (no update needed)
        return Optional.ofNullable(dependency.version());
    }
    
    @Override
    public List<String> getAvailableParentVersions(Project.Parent parent) {
        String key = parent.groupId() + ":" + parent.artifactId();
        return versionDatabase.getOrDefault(key, Collections.emptyList());
    }
    
    @Override
    public Optional<String> getLatestParentVersion(Project.Parent parent) {
        String key = parent.groupId() + ":" + parent.artifactId();
        return Optional.ofNullable(latestVersions.get(key));
    }
}