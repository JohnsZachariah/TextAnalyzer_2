package com.textanalyzer.storage;

import com.textanalyzer.exception.StorageException;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.HistoryRecord;
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
 * - Module 4: SOLID Open/Closed and Liskov Substitution Principles
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

        String sql = "INSERT OR REPLACE INTO analyses (id, title, preview, word_count, char_count, sentence_count, saved_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, record.getId());
            ps.setString(2, record.getTitle() != null ? record.getTitle() : "Untitled Analysis");
            ps.setString(3, record.getPreview() != null ? record.getPreview() : "");
            ps.setInt(4, record.getWordCount());
            ps.setInt(5, record.getCharCount());
            ps.setInt(6, record.getSentenceCount());
            ps.setString(7, record.getSavedAt() != null ? record.getSavedAt() :
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            ps.executeUpdate();
            // Also keep file in sync for convenience
            try {
                fileFallback.saveAnalysisRecord(record);
            } catch (Exception ignored) {}
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

        String sql = "SELECT id, title, preview, word_count, char_count, sentence_count, saved_at " +
                     "FROM analyses ORDER BY rowid DESC LIMIT 50";

        List<HistoryRecord> records = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String preview = rs.getString("preview");
                int wordCount = rs.getInt("word_count");
                int charCount = rs.getInt("char_count");
                int sentenceCount = rs.getInt("sentence_count");
                String savedAt = rs.getString("saved_at");

                records.add(new HistoryRecord(id, title, preview, savedAt, wordCount, charCount, sentenceCount));
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

    @Override
    public synchronized boolean deleteAnalysisRecord(String id) throws StorageException {
        if (id == null || id.trim().isEmpty()) return false;

        boolean deleted = false;
        if (databaseManager.isAvailable()) {
            String sql = "DELETE FROM analyses WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                deleted = ps.executeUpdate() > 0;
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
