package com.textanalyzer.pattern;

import com.textanalyzer.exception.StorageException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Singleton class managing access to persistent data storage.
 * Demonstrates:
 * - Singleton Design Pattern (OOP Syllabus Module 3)
 * - Thread-safe initialization (double-checked locking)
 * - Private constructor preventing external instantiation
 * - Centralized resource management
 */
public class StorageManager {

    private static volatile StorageManager instance;
    private final Path storageDirectory;
    private final Path reportsDirectory;

    /**
     * Private constructor to restrict instantiation from other classes.
     */
    private StorageManager() {
        this.storageDirectory = Paths.get("data");
        this.reportsDirectory = storageDirectory.resolve("reports");
        initializeDirectories();
    }

    /**
     * Global access point for the singleton instance with double-checked locking.
     */
    public static StorageManager getInstance() {
        if (instance == null) {
            synchronized (StorageManager.class) {
                if (instance == null) {
                    instance = new StorageManager();
                }
            }
        }
        return instance;
    }

    private void initializeDirectories() {
        try {
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
            if (!Files.exists(reportsDirectory)) {
                Files.createDirectories(reportsDirectory);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not initialize data directory: " + e.getMessage());
        }
    }

    public Path getStorageDirectory() {
        return storageDirectory;
    }

    public Path getReportsDirectory() {
        return reportsDirectory;
    }

    public File getHistoryFile() {
        return storageDirectory.resolve("history.json").toFile();
    }
}
