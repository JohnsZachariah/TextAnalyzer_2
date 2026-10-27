package com.textanalyzer.model;

/**
 * Enumeration representing supported text case transformations.
 * Demonstrates Java Enum types and switch expression handling.
 */
public enum CaseType {
    UPPERCASE("Uppercase"),
    LOWERCASE("Lowercase"),
    TITLE_CASE("Title Case"),
    SENTENCE_CASE("Sentence Case");

    private final String displayName;

    CaseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CaseType fromString(String value) {
        if (value == null) {
            return LOWERCASE;
        }
        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (CaseType type : values()) {
            if (type.name().equalsIgnoreCase(normalized) || type.displayName.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return LOWERCASE;
    }
}
