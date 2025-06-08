package de.diedavids.mavguard;

import de.diedavids.mavguard.commands.AnalyzeCommand;
import de.diedavids.mavguard.commands.CheckUpdatesCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
@CommandLine.Command(
    name = "mav-guard",
    subcommands = {AnalyzeCommand.class, CheckUpdatesCommand.class},
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Maven Guard CLI tool"
)
public class MavGuardApplication implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MavGuardApplication.class);

    public static void main(String[] args) {
        log.atInfo()
            .addKeyValue("args", String.join(" ", args))
            .log("Starting MavGuard application");
        int exitCode = SpringApplication.exit(SpringApplication.run(MavGuardApplication.class, args));
        System.exit(exitCode);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MavGuardApplication app, IFactory factory) {
        return args -> {
            log.atDebug()
                .addKeyValue("commandArgs", String.join(" ", args))
                .log("Executing command line runner");
            int exitCode = new CommandLine(app, factory).execute(args);
            if (exitCode != 0) {
                log.atDebug()
                    .addKeyValue("exitCode", exitCode)
                    .log("Command execution completed with non-zero exit code");
                System.exit(exitCode);
            } else {
                log.atDebug()
                    .log("Command execution completed successfully");
            }
        };
    }

    // Used by Picocli when the command is run without subcommands
    private CommandLine commandLine;

    // Store the factory for use in run()
    private final IFactory factory;

    public MavGuardApplication(IFactory factory) {
        this.factory = factory;
    }

    @Override
    public void run() {
        // This method is called when no subcommand is specified.
        // Display the help message for the main command.
        if (commandLine == null) {
            // Initialize CommandLine here if not already, using the factory
            commandLine = new CommandLine(this, factory);
        }
        commandLine.usage(System.out);
    }
}
