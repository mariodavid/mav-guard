package de.diedavids.mavguard.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StartupLoggingService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupLoggingService.class);

    @Override
    public void run(ApplicationArguments args) {
        logStartupInformation(args);
    }

    private void logStartupInformation(ApplicationArguments args) {
        logger.info("========================================");
        logger.info("Starting mav-guard CLI application");
        logger.info("========================================");

        logUsername();
        logWorkingDirectory();
        logCliArguments(args);
        logEnvironmentVariables();

        logger.info("========================================");
        logger.info("Startup logging complete");
        logger.info("========================================");
    }

    private void logUsername() {
        try {
            String username = System.getProperty("user.name", "unknown");
            logger.info("Starting mav-guard for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to retrieve username: {}", e.getMessage());
        }
    }

    private void logWorkingDirectory() {
        try {
            String workingDir = System.getProperty("user.dir", "unknown");
            logger.info("Working directory: {}", workingDir);
        } catch (Exception e) {
            logger.error("Failed to retrieve working directory: {}", e.getMessage());
        }
    }

    private void logCliArguments(ApplicationArguments args) {
        try {
            String[] sourceArgs = args.getSourceArgs();
            if (sourceArgs != null && sourceArgs.length > 0) {
                String argsString = String.join(" ", sourceArgs);
                logger.info("CLI arguments provided: {}", argsString);
            } else {
                logger.info("CLI arguments provided: none");
            }

            // Log option names if any
            if (!args.getOptionNames().isEmpty()) {
                logger.info("Options detected: {}", args.getOptionNames());
            }

            // Log non-option arguments if any
            if (!args.getNonOptionArgs().isEmpty()) {
                logger.info("Non-option arguments: {}", args.getNonOptionArgs());
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve CLI arguments: {}", e.getMessage());
        }
    }

    private void logEnvironmentVariables() {
        try {
            Map<String, String> env = System.getenv();
            
            // Log specific important environment variables
            logEnvironmentVariable(env, "USER");
            logEnvironmentVariable(env, "USERNAME"); // Windows alternative
            logEnvironmentVariable(env, "PATH");
            logEnvironmentVariable(env, "JAVA_HOME");
            logEnvironmentVariable(env, "MAVEN_HOME");
            logEnvironmentVariable(env, "M2_HOME");
            
            // Log any mav-guard specific environment variables
            String mavGuardEnvVars = env.entrySet().stream()
                    .filter(entry -> entry.getKey().toUpperCase().contains("MAV_GUARD") || 
                                   entry.getKey().toUpperCase().contains("MAVGUARD"))
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(", "));
            
            if (!mavGuardEnvVars.isEmpty()) {
                logger.info("Mav-guard specific environment variables: {}", mavGuardEnvVars);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve environment variables: {}", e.getMessage());
        }
    }

    private void logEnvironmentVariable(Map<String, String> env, String varName) {
        String value = env.get(varName);
        if (value != null && !value.isEmpty()) {
            if ("PATH".equals(varName)) {
                // For PATH, just log that it's set (it's usually very long)
                logger.info("Environment: {} is set ({} entries)", varName, value.split(":").length);
            } else {
                logger.info("Environment: {}={}", varName, value);
            }
        }
    }
}