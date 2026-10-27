package com.textanalyzer.core;

import com.textanalyzer.exception.TextAnalysisException;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.TextDocument;

/**
 * Interface defining the contract for text analysis operations.
 * Demonstrates:
 * - Interface Definition (OOP Module 3)
 * - Method Overloading in Interface (OOP Module 2)
 * - Checked Exception declaration (OOP Module 3)
 * - Interface Segregation Principle (SOLID - OOP Module 4)
 */
public interface TextAnalyzer {

    /**
     * Performs a comprehensive analysis of a TextDocument.
     * @param document The document to analyze.
     * @return The statistical and lexical AnalysisResult.
     * @throws TextAnalysisException If analysis fails or input is invalid.
     */
    AnalysisResult analyze(TextDocument document) throws TextAnalysisException;

    /**
     * Overloaded convenience method to analyze a raw string.
     * @param text Raw input string.
     * @return The statistical and lexical AnalysisResult.
     * @throws TextAnalysisException If analysis fails or input is invalid.
     */
    AnalysisResult analyze(String text) throws TextAnalysisException;
}
