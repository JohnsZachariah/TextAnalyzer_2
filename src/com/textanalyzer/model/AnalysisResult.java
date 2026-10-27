package com.textanalyzer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the comprehensive statistical and structural analysis of a text document.
 * Demonstrates:
 * - Data Encapsulation
 * - Inner Classes (Static Builder pattern - OOP Module 2)
 * - Java Collections (List, Map)
 * - Formatted output representation
 */
public class AnalysisResult {

    private final String documentId;
    private final String sourceName;
    private final String timestamp;

    // Counts
    private final int wordCount;
    private final int charCountWithSpaces;
    private final int charCountWithoutSpaces;
    private final int letterCount;
    private final int digitCount;
    private final int specialCharCount;
    private final int sentenceCount;
    private final int paragraphCount;

    // Vowels & Consonants
    private final int totalVowels;
    private final int totalConsonants;
    private final Map<Character, Integer> vowelBreakdown;

    // Extremes
    private final List<String> longestWords;
    private final int longestWordLength;
    private final List<String> shortestWords;
    private final int shortestWordLength;

    // Word Frequency & Metrics
    private final List<WordFrequency> wordFrequencies;
    private final int uniqueWordCount;
    private final double averageWordLength;
    private final double readingTimeMinutes;
    private final double fleschReadingEase;
    private final String readabilityLevel;

    private AnalysisResult(Builder builder) {
        this.documentId = builder.documentId;
        this.sourceName = builder.sourceName;
        this.timestamp = builder.timestamp;
        this.wordCount = builder.wordCount;
        this.charCountWithSpaces = builder.charCountWithSpaces;
        this.charCountWithoutSpaces = builder.charCountWithoutSpaces;
        this.letterCount = builder.letterCount;
        this.digitCount = builder.digitCount;
        this.specialCharCount = builder.specialCharCount;
        this.sentenceCount = builder.sentenceCount;
        this.paragraphCount = builder.paragraphCount;
        this.totalVowels = builder.totalVowels;
        this.totalConsonants = builder.totalConsonants;
        this.vowelBreakdown = Collections.unmodifiableMap(new HashMap<>(builder.vowelBreakdown));
        this.longestWords = Collections.unmodifiableList(new ArrayList<>(builder.longestWords));
        this.longestWordLength = builder.longestWordLength;
        this.shortestWords = Collections.unmodifiableList(new ArrayList<>(builder.shortestWords));
        this.shortestWordLength = builder.shortestWordLength;
        this.wordFrequencies = Collections.unmodifiableList(new ArrayList<>(builder.wordFrequencies));
        this.uniqueWordCount = builder.uniqueWordCount;
        this.averageWordLength = builder.averageWordLength;
        this.readingTimeMinutes = builder.readingTimeMinutes;
        this.fleschReadingEase = builder.fleschReadingEase;
        this.readabilityLevel = builder.readabilityLevel;
    }

    // Getters
    public String getDocumentId() { return documentId; }
    public String getSourceName() { return sourceName; }
    public String getTimestamp() { return timestamp; }
    public int getWordCount() { return wordCount; }
    public int getCharCountWithSpaces() { return charCountWithSpaces; }
    public int getCharCountWithoutSpaces() { return charCountWithoutSpaces; }
    public int getLetterCount() { return letterCount; }
    public int getDigitCount() { return digitCount; }
    public int getSpecialCharCount() { return specialCharCount; }
    public int getSentenceCount() { return sentenceCount; }
    public int getParagraphCount() { return paragraphCount; }
    public int getTotalVowels() { return totalVowels; }
    public int getTotalConsonants() { return totalConsonants; }
    public Map<Character, Integer> getVowelBreakdown() { return vowelBreakdown; }
    public List<String> getLongestWords() { return longestWords; }
    public int getLongestWordLength() { return longestWordLength; }
    public List<String> getShortestWords() { return shortestWords; }
    public int getShortestWordLength() { return shortestWordLength; }
    public List<WordFrequency> getWordFrequencies() { return wordFrequencies; }
    public int getUniqueWordCount() { return uniqueWordCount; }
    public double getAverageWordLength() { return averageWordLength; }
    public double getReadingTimeMinutes() { return readingTimeMinutes; }
    public double getFleschReadingEase() { return fleschReadingEase; }
    public String getReadabilityLevel() { return readabilityLevel; }

    /**
     * Inner Builder Class demonstrating the Builder Pattern and Inner Classes (OOP Module 2).
     */
    public static class Builder {
        private String documentId = "";
        private String sourceName = "";
        private String timestamp = "";
        private int wordCount;
        private int charCountWithSpaces;
        private int charCountWithoutSpaces;
        private int letterCount;
        private int digitCount;
        private int specialCharCount;
        private int sentenceCount;
        private int paragraphCount;
        private int totalVowels;
        private int totalConsonants;
        private Map<Character, Integer> vowelBreakdown = new HashMap<>();
        private List<String> longestWords = new ArrayList<>();
        private int longestWordLength;
        private List<String> shortestWords = new ArrayList<>();
        private int shortestWordLength;
        private List<WordFrequency> wordFrequencies = new ArrayList<>();
        private int uniqueWordCount;
        private double averageWordLength;
        private double readingTimeMinutes;
        private double fleschReadingEase;
        private String readabilityLevel = "N/A";

        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder sourceName(String sourceName) { this.sourceName = sourceName; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder wordCount(int count) { this.wordCount = count; return this; }
        public Builder charCountWithSpaces(int count) { this.charCountWithSpaces = count; return this; }
        public Builder charCountWithoutSpaces(int count) { this.charCountWithoutSpaces = count; return this; }
        public Builder letterCount(int count) { this.letterCount = count; return this; }
        public Builder digitCount(int count) { this.digitCount = count; return this; }
        public Builder specialCharCount(int count) { this.specialCharCount = count; return this; }
        public Builder sentenceCount(int count) { this.sentenceCount = count; return this; }
        public Builder paragraphCount(int count) { this.paragraphCount = count; return this; }
        public Builder totalVowels(int count) { this.totalVowels = count; return this; }
        public Builder totalConsonants(int count) { this.totalConsonants = count; return this; }
        public Builder vowelBreakdown(Map<Character, Integer> map) { this.vowelBreakdown = map; return this; }
        public Builder longestWords(List<String> words, int length) {
            this.longestWords = words;
            this.longestWordLength = length;
            return this;
        }
        public Builder shortestWords(List<String> words, int length) {
            this.shortestWords = words;
            this.shortestWordLength = length;
            return this;
        }
        public Builder wordFrequencies(List<WordFrequency> list) {
            this.wordFrequencies = list;
            return this;
        }
        public Builder uniqueWordCount(int count) { this.uniqueWordCount = count; return this; }
        public Builder averageWordLength(double length) { this.averageWordLength = length; return this; }
        public Builder readingTimeMinutes(double minutes) { this.readingTimeMinutes = minutes; return this; }
        public Builder fleschReadingEase(double score) { this.fleschReadingEase = score; return this; }
        public Builder readabilityLevel(String level) { this.readabilityLevel = level; return this; }

        public AnalysisResult build() {
            return new AnalysisResult(this);
        }
    }

    @Override
    public String toString() {
        return String.format("AnalysisResult [Words=%d, Chars=%d, Sentences=%d, Vowels=%d, Consonants=%d, UniqueWords=%d]",
                wordCount, charCountWithSpaces, sentenceCount, totalVowels, totalConsonants, uniqueWordCount);
    }
}
