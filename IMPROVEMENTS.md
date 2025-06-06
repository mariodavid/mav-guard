# MavGuard CLI Enhancements

This document outlines recent improvements to the MavGuard Command-Line Interface (CLI), focusing on enhanced usability, a more intuitive command structure, and clearer output.

## Key Changes

The MavGuard CLI has undergone a significant refactoring. Here are the highlights:

### 1. New Command Structure

The CLI now revolves around two primary commands: `analyze` and `check-updates`.

*   **`mavguard analyze <pom-path>`**
    *   **Purpose:** Provides a comprehensive overview of your Maven project (single or multi-module).
    *   **Features:**
        *   Displays project coordinates (GroupId, ArtifactId, Version).
        *   Lists all project dependencies (direct and managed for single modules; consolidated for multi-modules).
        *   Shows parent POM information.
        *   For multi-module projects, it identifies and reports any version inconsistencies for dependencies used across different modules.
        *   Supports a `--detailed-usage` option to show which module uses which dependency in a multi-module setup.

*   **`mavguard check-updates <pom-path>`**
    *   **Purpose:** Performs all the analysis of the `analyze` command and additionally checks for available updates.
    *   **Features:**
        *   Includes all output from the `analyze` command (project details, dependencies, inconsistencies).
        *   Checks for newer versions of all project dependencies.
        *   Checks for newer versions of parent POM(s).
        *   Clearly indicates available updates, showing current and latest versions.
        *   For multi-module projects, lists the modules affected by each dependency update.

### 2. Removal of `xml` Subcommand

The previous `xml` subcommand (which included actions like `parse-pom` and `extract-dependencies`) has been removed. Its functionalities have been logically integrated into the new `analyze` and `check-updates` commands, providing a more streamlined experience.

### 3. Auto-detection of Multi-Module Projects

The CLI now attempts to automatically detect if you are running commands against a single-module or a multi-module project by inspecting the POM file(s). This simplifies command usage as you typically no longer need to specify if a project is multi-module.
*   For cases where you need to explicitly control this behavior, the `--force-multi-module` flag is available for both `analyze` and `check-updates`.

### 4. Improved Output Formatting

The output generated by the commands, particularly `check-updates`, has been significantly improved for readability:
*   Clearer section headers for different types of information.
*   Aligned, columnar display for version updates, making it easier to compare current and latest versions.
*   More distinct presentation of version inconsistency warnings.

### 5. Default Help Display

Running `mavguard` (or `mavguard --help`) without any subcommands or arguments now directly displays the main help message, making it easier for users to discover available commands and options.

## Benefits

These enhancements offer several advantages:

*   **Easier to Learn:** A simpler command structure with fewer top-level commands.
*   **More Intuitive:** Command names (`analyze`, `check-updates`) clearly reflect their purpose.
*   **Streamlined Workflow:** Functionalities are grouped more logically, reducing the need to run multiple commands for related tasks.
*   **Clearer Output:** Improved formatting makes it easier to understand the analysis results and identify available updates.
*   **Simplified Usage:** Auto-detection of multi-module projects reduces boilerplate.

## Example Usage

Here are a few examples of how to use the new commands:

*   **Analyze a single POM file in the current directory:**
    ```bash
    mavguard analyze pom.xml
    ```

*   **Check for updates in a project located at a specific path:**
    ```bash
    mavguard check-updates /path/to/your/project/pom.xml
    ```

*   **Analyze a multi-module project and see detailed dependency usage by module:**
    ```bash
    mavguard analyze path/to/root/pom.xml --detailed-usage
    ```

*   **Force check-updates to treat a project as multi-module:**
    ```bash
    mavguard check-updates pom.xml --force-multi-module
    ```
We hope these improvements enhance your experience with MavGuard CLI!
