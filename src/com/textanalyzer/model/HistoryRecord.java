package com.textanalyzer.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a saved analysis entry stored permanently on disk.
 * Demonstrates persistence model and object serialization attributes.
 */
public class HistoryRecord {

    private final String id;
    private final String title;
    private final String preview;
    private final String savedAt;
    private final int wordCount;
    private final int charCount;
    private final int sentenceCount;

    public HistoryRecord(String id, String title, String preview, int wordCount, int charCount, int sentenceCount) {
        this(id, title, preview, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                wordCount, charCount, sentenceCount);
    }

    public HistoryRecord(String id, String title, String preview, String savedAt, int wordCount, int charCount, int sentenceCount) {
        this.id = id;
        this.title = title;
        this.preview = preview;
        this.savedAt = savedAt;
        this.wordCount = wordCount;
        this.charCount = charCount;
        this.sentenceCount = sentenceCount;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getPreview() { return preview; }
    public String getSavedAt() { return savedAt; }
    public int getWordCount() { return wordCount; }
    public int getCharCount() { return charCount; }
    public int getSentenceCount() { return sentenceCount; }
}
