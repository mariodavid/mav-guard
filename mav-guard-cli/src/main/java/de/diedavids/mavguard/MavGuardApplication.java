package de.diedavids.mavguard;

import de.diedavids.mavguard.commands.DependencyCommands;
import de.diedavids.mavguard.commands.XmlParserCommands;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
@CommandLine.Command(
    name = "mav-guard",
    subcommands = {XmlParserCommands.class, DependencyCommands.class},
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Maven Guard CLI tool"
)
public class MavGuardApplication implements Runnable {

    public static void main(String[] args) {
        int exitCode = SpringApplication.exit(SpringApplication.run(MavGuardApplication.class, args));
        System.exit(exitCode);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MavGuardApplication app, IFactory factory) {
        return args -> {
            int exitCode = new CommandLine(app, factory).execute(args);
            if (exitCode != 0) {
                System.exit(exitCode);
            }
        };
    }

    @Override
    public void run() {
        // This method is called when no subcommand is specified
        // We can show help or a welcome message here
        System.out.println("Welcome to Maven Guard CLI!");
        System.out.println("Use --help to see available commands.");
    }
}
