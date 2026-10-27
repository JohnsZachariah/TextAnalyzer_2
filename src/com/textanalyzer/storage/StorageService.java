package com.textanalyzer.storage;

import com.textanalyzer.exception.StorageException;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.HistoryRecord;
import java.io.File;
import java.util.List;

/**
 * Interface defining persistent storage operations.
 * Demonstrates:
 * - Interface-based architecture (OOP Module 3)
 * - Data Persistence requirement (OOP Syllabus Module 4)
 * - Checked Exception handling
 */
public interface StorageService {

    /**
     * Saves an analysis result into persistent storage history.
     */
    void saveAnalysisRecord(HistoryRecord record) throws StorageException;

    /**
     * Loads all persistent analysis history records.
     */
    List<HistoryRecord> loadHistory() throws StorageException;

    /**
     * Deletes an analysis record by its identifier.
     */
    default boolean deleteAnalysisRecord(String id) throws StorageException {
        return false;
    }

    /**
     * Clears all saved analysis records.
     */
    default void clearHistory() throws StorageException {}

    /**
     * Exports and persists an analysis report file to disk.
     * @param result The analysis result.
     * @param format "TEXT" or "JSON".
     * @return The saved File reference.
     */
    File saveReportFile(AnalysisResult result, String format) throws StorageException;
}
