package de.diedavids.mavguard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColorOutputServiceTest {

    private ColorOutputService colorOutputService;

    @BeforeEach
    void setUp() {
        colorOutputService = new ColorOutputService();
    }

    @Test
    void testColorizeWithAlwaysMode() {
        colorOutputService.setColorMode(ColorOutputService.ColorMode.ALWAYS);
        String result = colorOutputService.colorize("test", ColorOutputService.ColorType.RED);
        assertThat(result).contains("\u001B[31m"); // Red ANSI code
        assertThat(result).contains("\u001B[0m");  // Reset ANSI code
        assertThat(result).contains("test");
    }

    @Test
    void testColorizeWithNeverMode() {
        colorOutputService.setColorMode(ColorOutputService.ColorMode.NEVER);
        String result = colorOutputService.colorize("test", ColorOutputService.ColorType.RED);
        assertThat(result).isEqualTo("test");
        assertThat(result).doesNotContain("\u001B[31m");
    }

    @Test
    void testGetUpdateArrowWithColors() {
        colorOutputService.setColorMode(ColorOutputService.ColorMode.ALWAYS);
        String arrow = colorOutputService.getUpdateArrow("1.0.0", "2.0.0");
        assertThat(arrow).contains("→");
        assertThat(arrow).contains("\u001B[31m"); // Red for major update
    }

    @Test
    void testGetUpdateArrowWithoutColors() {
        colorOutputService.setColorMode(ColorOutputService.ColorMode.NEVER);
        String arrow = colorOutputService.getUpdateArrow("1.0.0", "2.0.0");
        assertThat(arrow).isEqualTo("→");
        assertThat(arrow).doesNotContain("\u001B[31m");
    }

    @Test
    void testGetUpdateArrowForUpToDate() {
        colorOutputService.setColorMode(ColorOutputService.ColorMode.ALWAYS);
        String arrow = colorOutputService.getUpdateArrow("1.0.0", "1.0.0");
        assertThat(arrow).contains("✓");
        assertThat(arrow).contains("\u001B[32m"); // Green for up-to-date
    }

    @Test
    void testMajorUpdateDetection() {
        colorOutputService.setColorMode(ColorOutputService.ColorMode.ALWAYS);
        // Test major version update
        String majorArrow = colorOutputService.getUpdateArrow("1.0.0", "2.0.0");
        assertThat(majorArrow).contains("\u001B[31m"); // Red

        // Test minor/patch update
        String minorArrow = colorOutputService.getUpdateArrow("1.0.0", "1.1.0");
        assertThat(minorArrow).contains("\u001B[33m"); // Yellow
    }

    @Test
    void testColorModeEnum() {
        // Test that all enum values are properly handled
        colorOutputService.setColorMode(ColorOutputService.ColorMode.AUTO);
        assertThat(colorOutputService.getColorMode()).isEqualTo(ColorOutputService.ColorMode.AUTO);

        colorOutputService.setColorMode(ColorOutputService.ColorMode.ALWAYS);
        assertThat(colorOutputService.getColorMode()).isEqualTo(ColorOutputService.ColorMode.ALWAYS);

        colorOutputService.setColorMode(ColorOutputService.ColorMode.NEVER);
        assertThat(colorOutputService.getColorMode()).isEqualTo(ColorOutputService.ColorMode.NEVER);
    }
}