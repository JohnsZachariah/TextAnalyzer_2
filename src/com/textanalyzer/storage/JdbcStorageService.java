package com.textanalyzer.storage;

import com.textanalyzer.exception.StorageException;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.HistoryRecord;
import com.textanalyzer.model.WordFrequency;
import com.textanalyzer.pattern.ReportExporter;
import com.textanalyzer.pattern.ReportExporterFactory;
import com.textanalyzer.pattern.StorageManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of StorageService using local SQLite Database via JDBC.
 * Fulfills:
 * - Module 3: Interfaces and Polymorphism
 * - Module 4: SQL Database Connectivity (JDBC PreparedStatement, ResultSet, Statement)
 * - Module 4: Relational 1-to-N Mapping (Analyses -> AnalysisWords)
 * - ACID Transactions: Manual Commit & Rollback with try-with-resources
 * - PreparedStatement Batch processing for word frequencies
 * - Fault tolerance: Graceful fallback to FileStorageService if JDBC driver is unavailable
 */
public class JdbcStorageService implements StorageService {

    private final DatabaseManager databaseManager;
    private final FileStorageService fileFallback;

    public JdbcStorageService() {
        this.databaseManager = DatabaseManager.getInstance();
        this.fileFallback = new FileStorageService();
    }

    @Override
    public synchronized void saveAnalysisRecord(HistoryRecord record) throws StorageException {
        if (record == null) {
            throw new StorageException("Cannot save null history record.");
        }

        if (!databaseManager.isAvailable()) {
            fileFallback.saveAnalysisRecord(record);
            return;
        }

        String insertAnalysisSql = "INSERT OR REPLACE INTO analyses " +
                "(id, title, preview, full_text, word_count, char_count, sentence_count, " +
                "vowel_count, consonant_count, reading_ease, longest_word, reading_time_minutes, saved_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertWordSql = "INSERT INTO analysis_words (analysis_id, word, frequency) VALUES (?, ?, ?)";

        try (Connection conn = databaseManager.getConnection()) {
            // Demonstrate ACID Transaction: Disable auto-commit
            conn.setAutoCommit(false);
            try {
                // 1. Insert parent analysis record
                try (PreparedStatement ps = conn.prepareStatement(insertAnalysisSql)) {
                    ps.setString(1, record.getId());
                    ps.setString(2, record.getTitle() != null ? record.getTitle() : "Untitled Analysis");
                    ps.setString(3, record.getPreview() != null ? record.getPreview() : "");
                    ps.setString(4, record.getFullText() != null ? record.getFullText() : "");
                    ps.setInt(5, record.getWordCount());
                    ps.setInt(6, record.getCharCount());
                    ps.setInt(7, record.getSentenceCount());
                    ps.setInt(8, record.getVowelCount());
                    ps.setInt(9, record.getConsonantCount());
                    ps.setDouble(10, record.getReadingEase());
                    ps.setString(11, record.getLongestWord() != null ? record.getLongestWord() : "");
                    ps.setDouble(12, record.getReadingTimeMinutes());
                    ps.setString(13, record.getSavedAt() != null ? record.getSavedAt() :
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                    ps.executeUpdate();
                }

                // 2. Clear old word frequencies for this ID if re-saving
                try (PreparedStatement delPs = conn.prepareStatement("DELETE FROM analysis_words WHERE analysis_id = ?")) {
                    delPs.setString(1, record.getId());
                    delPs.executeUpdate();
                }

                // 3. Insert child word frequencies via PreparedStatement Batch Processing
                if (record.getWordFrequencies() != null && !record.getWordFrequencies().isEmpty()) {
                    try (PreparedStatement psWords = conn.prepareStatement(insertWordSql)) {
                        for (WordFrequency wf : record.getWordFrequencies()) {
                            psWords.setString(1, record.getId());
                            psWords.setString(2, wf.getWord());
                            psWords.setInt(3, wf.getFrequency());
                            psWords.addBatch();
                        }
                        psWords.executeBatch();
                    }
                }

                // Explicit ACID Transaction Commit
                conn.commit();

                // Keep local JSON file in sync for convenience
                try {
                    fileFallback.saveAnalysisRecord(record);
                } catch (Exception ignored) {}

            } catch (SQLException e) {
                // ACID Transaction Rollback on error
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("[JdbcStorageService] SQL Save error, falling back to file: " + e.getMessage());
            fileFallback.saveAnalysisRecord(record);
        }
    }

    @Override
    public synchronized List<HistoryRecord> loadHistory() throws StorageException {
        if (!databaseManager.isAvailable()) {
            return fileFallback.loadHistory();
        }

        String sql = "SELECT id, title, preview, full_text, word_count, char_count, sentence_count, " +
                "vowel_count, consonant_count, reading_ease, longest_word, reading_time_minutes, saved_at " +
                "FROM analyses ORDER BY rowid DESC LIMIT 50";

        List<HistoryRecord> records = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String preview = rs.getString("preview");
                String fullText = rs.getString("full_text");
                int wordCount = rs.getInt("word_count");
                int charCount = rs.getInt("char_count");
                int sentenceCount = rs.getInt("sentence_count");
                int vowelCount = rs.getInt("vowel_count");
                int consonantCount = rs.getInt("consonant_count");
                double readingEase = rs.getDouble("reading_ease");
                String longestWord = rs.getString("longest_word");
                double readingTime = rs.getDouble("reading_time_minutes");
                String savedAt = rs.getString("saved_at");

                records.add(new HistoryRecord(
                        id, title, preview, savedAt,
                        wordCount, charCount, sentenceCount,
                        vowelCount, consonantCount, readingEase,
                        longestWord, readingTime, null, fullText
                ));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcStorageService] SQL Query error, falling back to file: " + e.getMessage());
            return fileFallback.loadHistory();
        }

        // If database was empty but file history exists, import file history into database!
        if (records.isEmpty()) {
            List<HistoryRecord> fromFile = fileFallback.loadHistory();
            if (!fromFile.isEmpty()) {
                for (HistoryRecord r : fromFile) {
                    try {
                        saveAnalysisRecord(r);
                    } catch (Exception ignored) {}
                }
                return fromFile;
            }
        }

        return records;
    }

    /**
     * Loads top word frequencies for an analysis using SQL JOIN/child query.
     */
    public synchronized List<WordFrequency> loadWordFrequencies(String analysisId) {
        List<WordFrequency> list = new ArrayList<>();
        if (!databaseManager.isAvailable() || analysisId == null) {
            return list;
        }
        String sql = "SELECT word, frequency FROM analysis_words WHERE analysis_id = ? ORDER BY frequency DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, analysisId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WordFrequency(rs.getString("word"), rs.getInt("frequency")));
                }
            }
        } catch (SQLException e) {
            System.err.println("[JdbcStorageService] Failed to load word frequencies: " + e.getMessage());
        }
        return list;
    }

    @Override
    public synchronized boolean deleteAnalysisRecord(String id) throws StorageException {
        if (id == null || id.trim().isEmpty()) return false;

        boolean deleted = false;
        if (databaseManager.isAvailable()) {
            try (Connection conn = databaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Child table deletion
                    try (PreparedStatement psWords = conn.prepareStatement("DELETE FROM analysis_words WHERE analysis_id = ?")) {
                        psWords.setString(1, id);
                        psWords.executeUpdate();
                    }
                    // Parent table deletion
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM analyses WHERE id = ?")) {
                        ps.setString(1, id);
                        deleted = ps.executeUpdate() > 0;
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("[JdbcStorageService] Failed to delete from SQL DB: " + e.getMessage());
            }
        }

        fileFallback.deleteAnalysisRecord(id);
        return deleted;
    }

    @Override
    public synchronized void clearHistory() throws StorageException {
        if (databaseManager.isAvailable()) {
            try {
                databaseManager.clearTable("analysis_words");
                databaseManager.clearTable("analyses");
            } catch (SQLException e) {
                throw new StorageException("Failed to clear SQL database table: " + e.getMessage(), e);
            }
        }
        fileFallback.clearHistory();
    }

    @Override
    public File saveReportFile(AnalysisResult result, String format) throws StorageException {
        if (result == null) {
            throw new StorageException("Cannot generate report for null analysis result.");
        }

        ReportExporter exporter = ReportExporterFactory.getExporter(format);
        String formattedReport = exporter.export(result);

        String ext = exporter.getFileExtension();
        String filename = String.format("report_%s_%d.%s",
                result.getDocumentId(),
                System.currentTimeMillis(),
                ext);

        File reportsDir = StorageManager.getInstance().getReportsDirectory().toFile();
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        File targetFile = new File(reportsDir, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.write(formattedReport);
            writer.flush();
            return targetFile;
        } catch (IOException e) {
            throw new StorageException("Failed to write report file to disk: " + targetFile.getAbsolutePath(), e);
        }
    }
}
