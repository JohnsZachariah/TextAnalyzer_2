package com.textanalyzer.exception;

/**
 * Custom checked exception thrown when text analysis fails or encounters
 * invalid processing conditions.
 * Demonstrates Checked Exceptions and exception chaining (OOP Syllabus Module 3).
 */
public class TextAnalysisException extends Exception {

    private final String errorCode;

    public TextAnalysisException(String message) {
        super(message);
        this.errorCode = "ANALYSIS_ERROR";
    }

    public TextAnalysisException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TextAnalysisException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ANALYSIS_ERROR";
    }

    public TextAnalysisException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
