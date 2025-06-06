package de.diedavids.mavguard.service;

import org.springframework.stereotype.Service;

/**
 * Service for handling color-coded terminal output with ANSI color codes.
 * Supports different color modes (auto, always, never) and graceful degradation.
 */
@Service
public class ColorOutputService {
    
    public enum ColorMode {
        AUTO, ALWAYS, NEVER
    }
    
    public enum ColorType {
        // Success/up-to-date
        GREEN("\u001B[32m"),
        // Warnings/minor updates  
        YELLOW("\u001B[33m"),
        // Errors/major updates
        RED("\u001B[31m"),
        // Version inconsistencies
        ORANGE("\u001B[38;5;214m"),
        // Headers/informational
        BLUE("\u001B[34m"),
        // Reset to default
        RESET("\u001B[0m"),
        // Bold text
        BOLD("\u001B[1m");
        
        private final String ansiCode;
        
        ColorType(String ansiCode) {
            this.ansiCode = ansiCode;
        }
        
        public String getAnsiCode() {
            return ansiCode;
        }
    }
    
    private ColorMode colorMode = ColorMode.AUTO;
    private Boolean terminalSupportsColor = null;
    
    /**
     * Set the color mode for output
     */
    public void setColorMode(ColorMode mode) {
        this.colorMode = mode;
    }
    
    /**
     * Get the current color mode
     */
    public ColorMode getColorMode() {
        return colorMode;
    }
    
    /**
     * Check if colors should be used based on current mode and terminal support
     */
    public boolean shouldUseColors() {
        // Check NO_COLOR environment variable (https://no-color.org/)
        if (System.getenv("NO_COLOR") != null && !System.getenv("NO_COLOR").isEmpty()) {
            return false;
        }
        
        switch (colorMode) {
            case ALWAYS:
                return true;
            case NEVER:
                return false;
            case AUTO:
            default:
                return isTerminalColorSupported();
        }
    }
    
    /**
     * Auto-detect if the terminal supports color output
     */
    private boolean isTerminalColorSupported() {
        if (terminalSupportsColor != null) {
            return terminalSupportsColor;
        }
        
        // First check common environment variables that indicate color support
        String term = System.getenv("TERM");
        String colorTerm = System.getenv("COLORTERM");
        
        if (colorTerm != null && !colorTerm.isEmpty()) {
            terminalSupportsColor = true;
            return true;
        }
        
        if (term != null) {
            terminalSupportsColor = term.contains("color") || 
                                   term.contains("xterm") || 
                                   term.contains("screen") ||
                                   term.equals("cygwin");
            return terminalSupportsColor;
        }
        
        // Check if we're running in a terminal (this might be null in some environments)
        // If console is null but we have TERM set, assume colors are supported
        if (System.console() == null) {
            // In non-interactive environments, still support colors if TERM suggests it
            terminalSupportsColor = (term != null && !term.isEmpty());
            return terminalSupportsColor;
        }
        
        // If we have a console and no specific TERM info, assume basic color support
        terminalSupportsColor = true;
        return true;
    }
    
    /**
     * Apply color to text if colors are enabled
     */
    public String colorize(String text, ColorType color) {
        if (!shouldUseColors()) {
            return text;
        }
        return color.getAnsiCode() + text + ColorType.RESET.getAnsiCode();
    }
    
    /**
     * Apply multiple colors to text (e.g., BOLD + GREEN)
     */
    public String colorize(String text, ColorType... colors) {
        if (!shouldUseColors()) {
            return text;
        }
        
        StringBuilder prefix = new StringBuilder();
        for (ColorType color : colors) {
            prefix.append(color.getAnsiCode());
        }
        
        return prefix.toString() + text + ColorType.RESET.getAnsiCode();
    }
    
    /**
     * Print colored text to stdout
     */
    public void println(String text, ColorType color) {
        System.out.println(colorize(text, color));
    }
    
    /**
     * Print colored text with multiple colors to stdout
     */
    public void println(String text, ColorType... colors) {
        System.out.println(colorize(text, colors));
    }
    
    /**
     * Print regular text to stdout (no color)
     */
    public void println(String text) {
        System.out.println(text);
    }
    
    /**
     * Print formatted colored text to stdout
     */
    public void printf(String format, ColorType color, Object... args) {
        System.out.printf(colorize(format, color), args);
    }
    
    /**
     * Print formatted regular text to stdout (no color)
     */
    public void printf(String format, Object... args) {
        System.out.printf(format, args);
    }
    
    /**
     * Get colored version of dependency update arrow based on update type
     */
    public String getUpdateArrow(String currentVersion, String latestVersion) {
        // Handle null current version (e.g., managed dependencies)
        if (currentVersion == null) {
            String arrow = "→";
            return shouldUseColors() ? colorize(arrow, ColorType.BLUE) : arrow;
        }
        
        if (currentVersion.equals(latestVersion)) {
            return shouldUseColors() ? colorize("✓", ColorType.GREEN) : "✓";
        }
        
        // Simple heuristic for update type based on version comparison
        String arrow = "→";
        
        if (!shouldUseColors()) {
            return arrow;
        }
        
        // Try to determine if this is a major update (very basic heuristic)
        if (isMajorUpdate(currentVersion, latestVersion)) {
            return colorize(arrow, ColorType.RED);
        } else {
            return colorize(arrow, ColorType.YELLOW);
        }
    }
    
    /**
     * Basic heuristic to determine if this is a major version update
     * This is a simple implementation and could be enhanced with proper semantic versioning
     */
    private boolean isMajorUpdate(String current, String latest) {
        // If current is null, we can't determine update type
        if (current == null) {
            return false;
        }
        
        try {
            // Extract major version numbers
            String[] currentParts = current.split("\\.");
            String[] latestParts = latest.split("\\.");
            
            if (currentParts.length > 0 && latestParts.length > 0) {
                int currentMajor = Integer.parseInt(currentParts[0]);
                int latestMajor = Integer.parseInt(latestParts[0]);
                return latestMajor > currentMajor;
            }
        } catch (NumberFormatException e) {
            // If we can't parse version numbers, assume it's a major update to be safe
            return true;
        }
        
        return false;
    }
    
    /**
     * Get colored text for dependency status
     */
    public String getDependencyStatus(String current, String latest) {
        // Handle null current version (e.g., managed dependencies)
        if (current == null) {
            return colorize("managed dependency", ColorType.BLUE);
        }
        
        if (current.equals(latest)) {
            return colorize("up-to-date", ColorType.GREEN);
        } else if (isMajorUpdate(current, latest)) {
            return colorize("major update available", ColorType.RED);
        } else {
            return colorize("update available", ColorType.YELLOW);
        }
    }
}