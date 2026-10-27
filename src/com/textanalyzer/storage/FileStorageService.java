package com.textanalyzer.storage;

import com.textanalyzer.exception.StorageException;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.HistoryRecord;
import com.textanalyzer.pattern.ReportExporter;
import com.textanalyzer.pattern.ReportExporterFactory;
import com.textanalyzer.pattern.StorageManager;
import com.textanalyzer.util.JsonUtil;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete implementation of StorageService handling file-based persistent storage.
 * Demonstrates:
 * - Java File I/O (BufferedReader, BufferedWriter, FileReader, FileWriter - Project Spec 1.4)
 * - Singleton pattern usage (StorageManager.getInstance())
 * - Exception Handling with custom StorageException (OOP Module 3)
 * - Thread-safe synchronization on shared storage file
 */
public class FileStorageService implements StorageService {

    private final StorageManager storageManager;

    public FileStorageService() {
        this.storageManager = StorageManager.getInstance();
    }

    @Override
    public synchronized void saveAnalysisRecord(HistoryRecord record) throws StorageException {
        if (record == null) {
            throw new StorageException("Cannot save null history record.");
        }

        List<HistoryRecord> records = loadHistory();
        // Insert new record at the top
        records.add(0, record);

        // Cap history to 50 records
        if (records.size() > 50) {
            records = records.subList(0, 50);
        }

        File historyFile = storageManager.getHistoryFile();
        String json = JsonUtil.historyToJson(records);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile, StandardCharsets.UTF_8))) {
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            throw new StorageException("Failed to persist history record to " + historyFile.getAbsolutePath(), e);
        }
    }

    @Override
    public synchronized List<HistoryRecord> loadHistory() throws StorageException {
        File historyFile = storageManager.getHistoryFile();
        if (!historyFile.exists()) {
            return new ArrayList<>();
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read history from " + historyFile.getAbsolutePath(), e);
        }

        String json = content.toString().trim();
        if (json.isEmpty() || !json.startsWith("[")) {
            return new ArrayList<>();
        }

        // Parse records from JSON array
        List<HistoryRecord> list = new ArrayList<>();
        try {
            // Strip brackets
            String inner = json.substring(1, json.length() - 1).trim();
            if (!inner.isEmpty()) {
                String[] items = inner.split("(?<=\\}),\\s*(?=\\{)");
                for (String item : items) {
                    var map = JsonUtil.parseJsonObject(item);
                    String id = (String) map.getOrDefault("id", "REC-" + System.currentTimeMillis());
                    String title = (String) map.getOrDefault("title", "Untitled");
                    String preview = (String) map.getOrDefault("preview", "");
                    String fullText = (String) map.getOrDefault("fullText", preview);
                    String savedAt = (String) map.getOrDefault("savedAt", "");
                    int wordCount = ((Number) map.getOrDefault("wordCount", 0)).intValue();
                    int charCount = ((Number) map.getOrDefault("charCount", 0)).intValue();
                    int sentenceCount = ((Number) map.getOrDefault("sentenceCount", 0)).intValue();

                    list.add(new HistoryRecord(id, title, preview, savedAt, wordCount, charCount, sentenceCount,
                            0, 0, 0.0, "", 0.0, null, fullText));
                }
            }
        } catch (Exception ex) {
            System.err.println("Notice: Error parsing history items: " + ex.getMessage());
        }

        return list;
    }

    @Override
    public synchronized boolean deleteAnalysisRecord(String id) throws StorageException {
        if (id == null) return false;
        List<HistoryRecord> records = loadHistory();
        boolean removed = records.removeIf(r -> id.equals(r.getId()));
        if (removed) {
            File historyFile = storageManager.getHistoryFile();
            String json = JsonUtil.historyToJson(records);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile, StandardCharsets.UTF_8))) {
                writer.write(json);
                writer.flush();
            } catch (IOException e) {
                throw new StorageException("Failed to persist history after deletion", e);
            }
        }
        return removed;
    }

    @Override
    public synchronized void clearHistory() throws StorageException {
        File historyFile = storageManager.getHistoryFile();
        if (historyFile.exists()) {
            historyFile.delete();
        }
    }

    @Override
    public File saveReportFile(AnalysisResult result, String format) throws StorageException {
        if (result == null) {
            throw new StorageException("Cannot export null analysis result.");
        }

        ReportExporter exporter = ReportExporterFactory.getExporter(format);
        String reportContent = exporter.export(result);

        String filename = String.format("analysis_report_%s%s",
                result.getDocumentId().replaceAll("[^a-zA-Z0-9_-]", "_"),
                exporter.getFileExtension());

        Path targetPath = storageManager.getReportsDirectory().resolve(filename);
        File targetFile = targetPath.toFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.write(reportContent);
            writer.flush();
            return targetFile;
        } catch (IOException e) {
            throw new StorageException("Failed to save report file: " + targetFile.getAbsolutePath(), e);
        }
    }
}
