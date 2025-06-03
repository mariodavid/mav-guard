package de.diedavids.mavguard.nexus.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for NexusProperties that ensures proper configuration based on repository type.
 */
public class NexusPropertiesValidator implements ConstraintValidator<ValidNexusProperties, NexusProperties> {

    @Override
    public boolean isValid(NexusProperties properties, ConstraintValidatorContext context) {
        if (properties == null) {
            return true; // Let @NotNull handle this
        }

        // For nexus type, username, password and repository are required
        if (properties.isNexus()) {
            boolean valid = true;
            context.disableDefaultConstraintViolation();

            if (properties.username() == null || properties.username().isBlank()) {
                context.buildConstraintViolationWithTemplate("Username is required for Nexus repository type")
                        .addPropertyNode("username")
                        .addConstraintViolation();
                valid = false;
            }

            if (properties.password() == null || properties.password().isBlank()) {
                context.buildConstraintViolationWithTemplate("Password is required for Nexus repository type")
                        .addPropertyNode("password")
                        .addConstraintViolation();
                valid = false;
            }

            if (properties.repository() == null || properties.repository().isBlank()) {
                context.buildConstraintViolationWithTemplate("Repository name is required for Nexus repository type")
                        .addPropertyNode("repository")
                        .addConstraintViolation();
                valid = false;
            }

            return valid;
        }

        // For maven-central type, no additional validation needed
        return true;
    }
}