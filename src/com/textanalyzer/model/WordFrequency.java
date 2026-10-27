package com.textanalyzer.model;

import java.util.Objects;

/**
 * Encapsulates a word and its occurrence frequency.
 * Implements Comparable for custom sorting (by frequency descending, then alphabetically).
 * Demonstrates Encapsulation, Comparable interface, and Object method overrides.
 */
public class WordFrequency implements Comparable<WordFrequency> {

    private final String word;
    private final int count;
    private final double percentage;

    public WordFrequency(String word, int count) {
        this(word, count, 0.0);
    }

    public WordFrequency(String word, int count, double percentage) {
        this.word = word;
        this.count = count;
        this.percentage = percentage;
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    public int getFrequency() {
        return count;
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    public int compareTo(WordFrequency other) {
        // Sort primarily by count descending
        int countComparison = Integer.compare(other.count, this.count);
        if (countComparison != 0) {
            return countComparison;
        }
        // Secondarily sort alphabetically
        return this.word.compareToIgnoreCase(other.word);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordFrequency that = (WordFrequency) o;
        return count == that.count && Objects.equals(word, that.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, count);
    }

    @Override
    public String toString() {
        return String.format("%s: %d (%.2f%%)", word, count, percentage);
    }
}
