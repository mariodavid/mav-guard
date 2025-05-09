package de.diedavids.mavguard.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
@Test
public @interface LocalOnlyTest {
    String value() default "";
}