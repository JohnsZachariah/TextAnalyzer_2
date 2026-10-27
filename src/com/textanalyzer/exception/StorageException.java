package com.textanalyzer.exception;

/**
 * Custom checked exception for persistent file and database storage operations.
 * Demonstrates Checked Exceptions (OOP Syllabus Module 3).
 */
public class StorageException extends Exception {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
