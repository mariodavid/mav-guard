#!/bin/bash

# validate-asciidoc-docs - macOS kompatible Version
# Exit Code 0: Alles aktuell | Exit Code 1: Patch auf stdout

set -e

check_claude_installed() {
    if ! command -v claude &> /dev/null; then
        echo "ERROR: Claude Code ist nicht installiert!" >&2
        echo "Installation mit: npm install -g @anthropic-ai/claude-code" >&2
        exit 2
    fi
}

check_api_key() {
    if [ -z "$ANTHROPIC_API_KEY" ]; then
        if [ -f ".env" ]; then
            set -a
            source .env 2>/dev/null || true
            set +a
        fi

        if [ -z "$ANTHROPIC_API_KEY" ]; then
            echo "ERROR: ANTHROPIC_API_KEY nicht gefunden!" >&2
            echo "Setze: export ANTHROPIC_API_KEY=your_key_here" >&2
            exit 2
        fi
    fi
}

get_adoc_files() {
    local mode="$1"
    case $mode in
        "git-diff")
            git diff --name-only HEAD~1..HEAD | grep -E '\.(adoc|asciidoc)$' || true
            ;;
        "all")
            find . -name "*.adoc" -o -name "*.asciidoc" | grep -v ".git" || true
            ;;
        *)
            echo "$mode"
            ;;
    esac
}

validate_and_fix_files() {
    local temp_dir=$(mktemp -d)
    local has_changes=false
    local temp_files_list="$temp_dir/files.txt"

    # Alle Dateien in temp file sammeln
    printf '%s\n' "$@" > "$temp_files_list"

    # Für jede Datei einzeln validieren
    while IFS= read -r file; do
        [ -z "$file" ] && continue
        [ ! -f "$file" ] && continue

        # Claude Prompt für Validierung UND Korrektur
        local prompt="Analysiere die AsciiDoc-Datei '$file' und korrigiere alle veralteten Code-Beispiele.

AUFGABE:
1. Identifiziere alle [source,java], [source,xml], [source,yaml], [source,properties] Code-Blöcke
2. Vergleiche jeden Code-Block mit der aktuellen Implementierung im Projekt
3. Falls Code-Beispiele veraltet sind: Korrigiere sie basierend auf dem aktuellen Code

WICHTIG:
- Falls ALLE Code-Beispiele aktuell sind: Antworte nur mit 'CODE_LISTINGS_UP_TO_DATE'
- Falls Korrekturen nötig sind: Gib die korrigierte VOLLSTÄNDIGE Datei aus (ohne zusätzliche Erklärungen)
- Behalte das gesamte AsciiDoc-Format bei (Überschriften, Text, etc.)
- Ändere nur die veralteten Code-Blöcke, alles andere bleibt gleich"

        # Claude ausführen
        local temp_file="$temp_dir/$(basename "$file")"
        if claude -p "$prompt" --output-format text > "$temp_file" 2>/dev/null; then

            # Prüfen ob Änderungen nötig sind
            if grep -q "CODE_LISTINGS_UP_TO_DATE" "$temp_file"; then
                # Keine Änderungen nötig für diese Datei
                continue
            else
                # Datei wurde korrigiert
                has_changes=true

                # Backup der Original-Datei
                cp "$file" "$file.backup"

                # Korrigierte Version übernehmen
                cp "$temp_file" "$file"
            fi
        else
            echo "ERROR: Claude Validierung für $file fehlgeschlagen" >&2
            cleanup_all "$temp_files_list"
            rm -rf "$temp_dir"
            exit 2
        fi
    done < "$temp_files_list"

    # Git Patch generieren falls Änderungen vorhanden
    if [ "$has_changes" = true ]; then
        # Geänderte Dateien zu Git Stage hinzufügen
        while IFS= read -r file; do
            [ -n "$file" ] && [ -f "$file" ] && git add "$file" 2>/dev/null || true
        done < "$temp_files_list"

        if git diff --cached --quiet; then
            # Keine Änderungen in Git
            echo "CODE_LISTINGS_UP_TO_DATE"
            cleanup_all "$temp_files_list"
            rm -rf "$temp_dir"
            exit 0
        else
            # Patch ausgeben
            git diff --cached

            # Restore original files
            restore_all "$temp_files_list"
            reset_git_stage "$temp_files_list"

            rm -rf "$temp_dir"
            exit 1
        fi
    else
        echo "CODE_LISTINGS_UP_TO_DATE"
        rm -rf "$temp_dir"
        exit 0
    fi
}

restore_all() {
    local files_list="$1"
    while IFS= read -r file; do
        [ -n "$file" ] && [ -f "$file.backup" ] && mv "$file.backup" "$file"
    done < "$files_list"
}

cleanup_all() {
    local files_list="$1"
    while IFS= read -r file; do
        [ -n "$file" ] && [ -f "$file.backup" ] && rm "$file.backup"
    done < "$files_list"
}

reset_git_stage() {
    local files_list="$1"
    while IFS= read -r file; do
        [ -n "$file" ] && git reset HEAD "$file" 2>/dev/null || true
    done < "$files_list"
}

main() {
    local mode="all"
    local target_files=""
    local temp_targets=$(mktemp)

    # Parameter parsen
    while [[ $# -gt 0 ]]; do
        case $1 in
            --git-diff)
                mode="git-diff"
                shift
                ;;
            --all)
                mode="all"
                shift
                ;;
            -h|--help)
                echo "Usage: validate-asciidoc-docs [OPTIONS] [FILES...]"
                echo ""
                echo "Optionen:"
                echo "  --git-diff    Nur geänderte .adoc Dateien"
                echo "  --all         Alle .adoc Dateien (default)"
                echo "  FILES...      Spezifische Dateien"
                echo ""
                echo "Exit Codes:"
                echo "  0: Code listings up to date"
                echo "  1: Git patch auf stdout"
                echo "  2: Fehler"
                rm -f "$temp_targets"
                exit 0
                ;;
            -*)
                echo "ERROR: Unbekannte Option: $1" >&2
                rm -f "$temp_targets"
                exit 2
                ;;
            *)
                echo "$1" >> "$temp_targets"
                shift
                ;;
        esac
    done

    # Checks
    check_claude_installed
    check_api_key

    # Dateien sammeln
    if [ -s "$temp_targets" ]; then
        # Spezifische Dateien wurden angegeben
        target_files="$temp_targets"
    else
        # Modus-basierte Sammlung
        get_adoc_files "$mode" > "$temp_targets"
    fi

    # Prüfen ob Dateien gefunden wurden
    if [ ! -s "$temp_targets" ]; then
        echo "CODE_LISTINGS_UP_TO_DATE"
        rm -f "$temp_targets"
        exit 0
    fi

    # Git Check wenn git-diff Modus
    if [ "$mode" = "git-diff" ] && ! git rev-parse --git-dir > /dev/null 2>&1; then
        echo "ERROR: Nicht in einem Git Repository!" >&2
        rm -f "$temp_targets"
        exit 2
    fi

    # Alle gefundenen Dateien an validate_and_fix_files übergeben
    local files_args=""
    while IFS= read -r file; do
        [ -n "$file" ] && files_args="$files_args \"$file\""
    done < "$temp_targets"

    rm -f "$temp_targets"

    # Validierung ausführen
    eval "validate_and_fix_files $files_args"
}

# Cleanup on exit
cleanup_on_exit() {
    # Versuche alle .backup Dateien zu restoren
    find . -name "*.backup" -type f 2>/dev/null | while read -r backup; do
        original="${backup%.backup}"
        [ -f "$original" ] && mv "$backup" "$original"
    done 2>/dev/null || true
}

trap cleanup_on_exit EXIT

main "$@"