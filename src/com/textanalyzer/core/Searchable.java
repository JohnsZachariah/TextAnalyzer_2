package com.textanalyzer.core;

import com.textanalyzer.model.SearchResult;

/**
 * Interface defining search functionality within text.
 * Demonstrates Interface Segregation Principle.
 */
public interface Searchable {

    /**
     * Searches for a target query with default case-insensitive settings.
     */
    SearchResult search(String text, String query);

    /**
     * Overloaded search method supporting case sensitivity and whole word matching.
     */
    SearchResult search(String text, String query, boolean matchCase, boolean wholeWord);
}
