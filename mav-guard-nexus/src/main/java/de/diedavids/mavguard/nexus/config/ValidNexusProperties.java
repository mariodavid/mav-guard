package de.diedavids.mavguard.nexus.config;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation for NexusProperties to ensure proper configuration based on repository type.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NexusPropertiesValidator.class)
@Documented
public @interface ValidNexusProperties {
    String message() default "Invalid Nexus properties configuration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}