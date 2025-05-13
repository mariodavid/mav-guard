package de.diedavids.mavguard.xml;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves dependency conflicts across a multi-module project structure
 * according to Maven's "nearest wins" principle.
 */
public class DependencyConflictResolver {

    /**
     * Returns a consolidated list of dependencies from all modules with conflicts resolved.
     * 
     * @param projects List of projects (parent and modules)
     * @return A consolidated and deduplicated list of dependencies
     */
    public List<Dependency> resolveConflicts(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return new ArrayList<>();
        }

        // Organize projects by coordinates for quick lookup
        Map<String, Project> projectMap = createProjectMap(projects);
        
        // Collect all dependencies by coordinate (groupId:artifactId)
        Map<String, Map<String, Dependency>> dependenciesByModule = collectAllDependencies(projects);
        
        // Resolve conflicts and consolidate dependencies
        return resolveConflictsAndConsolidate(dependenciesByModule, projectMap);
    }

    /**
     * Creates a map of project coordinates to projects for quick lookup.
     * 
     * @param projects the list of projects
     * @return a map of project coordinates to Project objects
     */
    private Map<String, Project> createProjectMap(List<Project> projects) {
        Map<String, Project> projectMap = new HashMap<>();
        for (Project project : projects) {
            projectMap.put(project.getCoordinates(), project);
        }
        return projectMap;
    }

    /**
     * Collects all dependencies from all projects, organizing them by module and dependency coordinates.
     * 
     * @param projects the list of projects
     * @return a nested map of module coordinates -> dependency coordinates -> dependency
     */
    private Map<String, Map<String, Dependency>> collectAllDependencies(List<Project> projects) {
        Map<String, Map<String, Dependency>> dependenciesByModule = new HashMap<>();
        
        for (Project project : projects) {
            String moduleCoord = project.getCoordinates();
            Map<String, Dependency> moduleDependencies = new HashMap<>();
            
            // Add direct dependencies
            for (Dependency dependency : project.dependencies()) {
                String depCoord = dependency.groupId() + ":" + dependency.artifactId();
                moduleDependencies.put(depCoord, dependency);
            }
            
            // Add dependencies from dependencyManagement
            if (project.dependencyManagement() != null) {
                for (Dependency dependency : project.dependencyManagement().dependencies()) {
                    String depCoord = dependency.groupId() + ":" + dependency.artifactId();
                    // Only add if not already defined as a direct dependency
                    if (!moduleDependencies.containsKey(depCoord)) {
                        moduleDependencies.put(depCoord, dependency);
                    }
                }
            }
            
            dependenciesByModule.put(moduleCoord, moduleDependencies);
        }
        
        return dependenciesByModule;
    }

    /**
     * Resolves conflicts between dependencies and returns a consolidated list.
     * 
     * @param dependenciesByModule map of module coordinates to dependency maps
     * @param projectMap map of project coordinates to projects
     * @return a consolidated list of dependencies with conflicts resolved
     */
    private List<Dependency> resolveConflictsAndConsolidate(
            Map<String, Map<String, Dependency>> dependenciesByModule,
            Map<String, Project> projectMap) {
        
        // Final consolidated dependencies
        Map<String, Dependency> resolvedDependencies = new HashMap<>();
        
        // For each module, find dependencies with inheritance priority
        for (String moduleCoord : dependenciesByModule.keySet()) {
            Project project = projectMap.get(moduleCoord);
            
            // Get dependencies for this module
            Map<String, Dependency> moduleDependencies = dependenciesByModule.get(moduleCoord);
            
            // Resolve parent inheritance
            resolveParentInheritance(project, moduleDependencies, dependenciesByModule, projectMap, resolvedDependencies);
        }
        
        return new ArrayList<>(resolvedDependencies.values());
    }

    /**
     * Resolves parent inheritance for dependencies, applying the "nearest wins" rule.
     * 
     * @param project current project
     * @param moduleDependencies direct dependencies from the module
     * @param dependenciesByModule all dependencies by module
     * @param projectMap map of project coordinates to projects
     * @param resolvedDependencies map to collect resolved dependencies
     */
    private void resolveParentInheritance(
            Project project,
            Map<String, Dependency> moduleDependencies,
            Map<String, Map<String, Dependency>> dependenciesByModule,
            Map<String, Project> projectMap,
            Map<String, Dependency> resolvedDependencies) {
        
        // Process dependencies from the current module first
        for (Map.Entry<String, Dependency> entry : moduleDependencies.entrySet()) {
            String depCoord = entry.getKey();
            Dependency dependency = entry.getValue();
            
            // Apply "nearest wins" - current module dependencies take precedence
            // over parent dependencies that were already processed
            resolvedDependencies.put(depCoord, dependency);
        }
        
        // Process parent inheritances recursively
        if (project.hasParent()) {
            Project.Parent parent = project.parent();
            String parentCoords = parent.getCoordinates();
            Project parentProject = projectMap.get(parentCoords);
            
            if (parentProject != null) {
                Map<String, Dependency> parentDependencies = dependenciesByModule.get(parentCoords);
                if (parentDependencies != null) {
                    // Continue recursion with parent project
                    resolveParentInheritance(
                            parentProject,
                            parentDependencies,
                            dependenciesByModule,
                            projectMap,
                            resolvedDependencies);
                }
            }
        }
    }
}