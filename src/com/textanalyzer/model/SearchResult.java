package com.textanalyzer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the results of a word or phrase search.
 * Demonstrates Collections usage, Encapsulation, and defensive copying.
 */
public class SearchResult {

    private final String query;
    private final boolean found;
    private final int occurrences;
    private final List<Integer> matchIndices;
    private final List<String> matchedLines;

    public SearchResult(String query, int occurrences, List<Integer> matchIndices, List<String> matchedLines) {
        this.query = query;
        this.occurrences = occurrences;
        this.found = occurrences > 0;
        this.matchIndices = matchIndices != null ? new ArrayList<>(matchIndices) : new ArrayList<>();
        this.matchedLines = matchedLines != null ? new ArrayList<>(matchedLines) : new ArrayList<>();
    }

    public static SearchResult notFound(String query) {
        return new SearchResult(query, 0, Collections.emptyList(), Collections.emptyList());
    }

    public String getQuery() {
        return query;
    }

    public boolean isFound() {
        return found;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public List<Integer> getMatchIndices() {
        return Collections.unmodifiableList(matchIndices);
    }

    public List<String> getMatchedLines() {
        return Collections.unmodifiableList(matchedLines);
    }

    @Override
    public String toString() {
        return String.format("Query: '%s' | Found: %b | Occurrences: %d", query, found, occurrences);
    }
}
