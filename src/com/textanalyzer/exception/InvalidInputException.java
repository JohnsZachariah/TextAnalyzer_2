package com.textanalyzer.exception;

/**
 * Custom unchecked exception thrown for null or invalid inputs.
 * Demonstrates Unchecked Exceptions extending RuntimeException (OOP Syllabus Module 3).
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
