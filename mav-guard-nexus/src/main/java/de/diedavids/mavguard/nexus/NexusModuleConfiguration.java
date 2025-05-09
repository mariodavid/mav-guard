package de.diedavids.mavguard.nexus;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Nexus module.
 * This enables the Nexus module to be imported into the main application.
 */
@Configuration
@ComponentScan("de.diedavids.mavguard.nexus")
public class NexusModuleConfiguration {
}
