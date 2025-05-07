package de.diedavids.mavguard.commands;

import de.diedavids.mavguard.model.Dependency;
import de.diedavids.mavguard.model.Project;
import de.diedavids.mavguard.xml.PomParser;
import de.diedavids.mavguard.xml.XmlParser;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command line interface for XML parsing operations.
 */
@Component
@Command(
    name = "xml", 
    description = "XML parsing operations", 
    mixinStandardHelpOptions = true,
    subcommands = {
        XmlParserCommands.ParsePomCommand.class,
        XmlParserCommands.ExtractDependenciesCommand.class
    }
)
public class XmlParserCommands {

    private final PomParser pomParser;

    public XmlParserCommands(PomParser pomParser) {
        this.pomParser = pomParser;
    }

    /**
     * Command to parse a Maven POM file.
     */
    @Component
    @Command(name = "parse-pom", description = "Parse a Maven POM file", mixinStandardHelpOptions = true)
    public class ParsePomCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Path to the POM file")
        private String filePath;

        @Override
        public Integer call() {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("File not found: " + filePath);
                    return 1;
                }

                Project project = pomParser.parsePomFile(file);
                System.out.printf("Successfully parsed POM file: %s:%s:%s%n", 
                        project.getGroupId(), project.getArtifactId(), project.getVersion());
                return 0;
            } catch (JAXBException e) {
                System.err.println("Error parsing POM: " + e.getMessage());
                return 1;
            }
        }
    }

    /**
     * Command to extract dependencies from a Maven POM file.
     */
    @Component
    @Command(name = "extract-dependencies", description = "Extract dependencies from a Maven POM file", mixinStandardHelpOptions = true)
    public class ExtractDependenciesCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Path to the POM file")
        private String filePath;

        @Override
        public Integer call() {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("File not found: " + filePath);
                    return 1;
                }

                Project project = pomParser.parsePomFile(file);
                List<Dependency> dependencies = project.getAllDependencies();
                if (dependencies.isEmpty()) {
                    System.out.println("No dependencies found in POM file.");
                    return 0;
                }

                System.out.println("Dependencies found in POM file:");
                for (Dependency dependency : dependencies) {
                    System.out.println("- " + dependency);
                }
                return 0;
            } catch (JAXBException e) {
                System.err.println("Error extracting dependencies: " + e.getMessage());
                return 1;
            }
        }
    }

}
