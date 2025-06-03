package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.nexus.service.DependencyVersionService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Test implementation of DependencyVersionService for parent POM integration tests.
 * This extends TestDependencyVersionService to add parent-specific test data.
 */
@Service
@Primary
@Profile("mock-parent-test")
public class TestParentVersionService extends TestDependencyVersionService {
    
    private final Map<String, List<String>> parentVersionDatabase = new HashMap<>();
    private final Map<String, String> latestParentVersions = new HashMap<>();
    
    public TestParentVersionService() {
        super();
        
        // Initialize parent-specific test data
        addParentVersionData("org.springframework.boot:spring-boot-starter-parent", 
            Arrays.asList("3.2.0", "3.1.5", "3.0.11", "2.7.8", "2.7.5", "2.7.0", "2.6.15"),
            "3.2.0");
            
        // Local parent - no updates available
        addParentVersionData("com.example:multi-module-parent",
            Arrays.asList("1.0.0"),
            "1.0.0");
    }
    
    private void addParentVersionData(String coordinates, List<String> versions, String latest) {
        parentVersionDatabase.put(coordinates, versions);
        latestParentVersions.put(coordinates, latest);
    }
    
    @Override
    public List<String> getAvailableParentVersions(Project.Parent parent) {
        String key = parent.groupId() + ":" + parent.artifactId();
        return parentVersionDatabase.getOrDefault(key, Collections.emptyList());
    }
    
    @Override
    public Optional<String> getLatestParentVersion(Project.Parent parent) {
        String key = parent.groupId() + ":" + parent.artifactId();
        
        // For test purposes, return newer versions only for specific cases
        if ("org.springframework.boot".equals(parent.groupId()) && 
            "spring-boot-starter-parent".equals(parent.artifactId()) &&
            "2.7.0".equals(parent.version())) {
            return Optional.of("2.7.8");
        }
        
        // For all other cases, return the current version (no update needed)
        return Optional.ofNullable(parent.version());
    }
    
    @Override
    public boolean hasNewerParentVersion(Project.Parent parent) {
        return getLatestParentVersion(parent)
                .map(latest -> !latest.equals(parent.version()))
                .orElse(false);
    }
}