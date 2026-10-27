package com.textanalyzer.core;

import com.textanalyzer.model.CaseType;

/**
 * Interface defining text transformation operations (Find & Replace, Case Conversion).
 * Demonstrates Interface Segregation Principle.
 */
public interface Transformable {

    /**
     * Replaces occurrences of target with replacement string.
     */
    String findAndReplace(String text, String target, String replacement, boolean matchCase);

    /**
     * Converts text to the specified case format.
     */
    String convertCase(String text, CaseType caseType);
}
