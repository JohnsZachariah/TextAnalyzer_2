package com.textanalyzer.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a saved analysis entry stored permanently in the SQL database and disk.
 * Demonstrates:
 * - Data Persistence Model (OOP Syllabus Module 4)
 * - Encapsulation and Immutable Object Design
 * - Relational Mapping: Contains associated top word frequencies for 1-to-N database storage
 */
public class HistoryRecord {

    private final String id;
    private final String title;
    private final String preview;
    private final String fullText;
    private final String savedAt;
    private final int wordCount;
    private final int charCount;
    private final int sentenceCount;

    // Enhanced metrics for relational database persistence
    private final int vowelCount;
    private final int consonantCount;
    private final double readingEase;
    private final String longestWord;
    private final double readingTimeMinutes;
    private final List<WordFrequency> wordFrequencies;

    public HistoryRecord(String id, String title, String preview, int wordCount, int charCount, int sentenceCount) {
        this(id, title, preview, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                wordCount, charCount, sentenceCount, 0, 0, 0.0, "", 0.0, null, preview);
    }

    public HistoryRecord(String id, String title, String preview, String savedAt, int wordCount, int charCount, int sentenceCount) {
        this(id, title, preview, savedAt, wordCount, charCount, sentenceCount, 0, 0, 0.0, "", 0.0, null, preview);
    }

    public HistoryRecord(String id, String title, String preview, String savedAt,
                         int wordCount, int charCount, int sentenceCount,
                         int vowelCount, int consonantCount, double readingEase,
                         String longestWord, double readingTimeMinutes,
                         List<WordFrequency> wordFrequencies) {
        this(id, title, preview, savedAt, wordCount, charCount, sentenceCount,
                vowelCount, consonantCount, readingEase, longestWord, readingTimeMinutes, wordFrequencies, preview);
    }

    public HistoryRecord(String id, String title, String preview, String savedAt,
                         int wordCount, int charCount, int sentenceCount,
                         int vowelCount, int consonantCount, double readingEase,
                         String longestWord, double readingTimeMinutes,
                         List<WordFrequency> wordFrequencies, String fullText) {
        this.id = id != null ? id : "HIST-" + System.currentTimeMillis();
        this.title = title != null ? title : "Saved Analysis";
        this.preview = preview != null ? preview : "";
        this.fullText = (fullText != null && !fullText.isEmpty()) ? fullText : this.preview;
        this.savedAt = savedAt != null ? savedAt : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.wordCount = wordCount;
        this.charCount = charCount;
        this.sentenceCount = sentenceCount;
        this.vowelCount = vowelCount;
        this.consonantCount = consonantCount;
        this.readingEase = readingEase;
        this.longestWord = longestWord != null ? longestWord : "";
        this.readingTimeMinutes = readingTimeMinutes;
        this.wordFrequencies = wordFrequencies != null
                ? Collections.unmodifiableList(new ArrayList<>(wordFrequencies))
                : Collections.emptyList();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getPreview() { return preview; }
    public String getFullText() { return fullText; }
    public String getSavedAt() { return savedAt; }
    public int getWordCount() { return wordCount; }
    public int getCharCount() { return charCount; }
    public int getSentenceCount() { return sentenceCount; }
    public int getVowelCount() { return vowelCount; }
    public int getConsonantCount() { return consonantCount; }
    public double getReadingEase() { return readingEase; }
    public String getLongestWord() { return longestWord; }
    public double getReadingTimeMinutes() { return readingTimeMinutes; }
    public List<WordFrequency> getWordFrequencies() { return wordFrequencies; }
}
