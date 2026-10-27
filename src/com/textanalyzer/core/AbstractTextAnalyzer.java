package com.textanalyzer.core;

import com.textanalyzer.exception.InvalidInputException;
import com.textanalyzer.exception.TextAnalysisException;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.TextDocument;

/**
 * Abstract base class for Text Analyzers.
 * Demonstrates:
 * - Abstract Classes and Interfaces
 * - Inheritance & Superclass state
 * - Protected members and constructor initialization
 * - Method Overloading
 * - Template Method Pattern
 */
public abstract class AbstractTextAnalyzer implements TextAnalyzer {

    protected final String analyzerName;
    protected final String version;
    protected long totalProcessedCharacters;

    /**
     * Default constructor chaining to parameterized constructor.
     * Demonstrates 'this' keyword and constructor chaining.
     */
    public AbstractTextAnalyzer() {
        this("Core Text Analyzer", "1.0.0");
    }

    /**
     * Parameterized constructor called via super(...) in subclasses.
     */
    public AbstractTextAnalyzer(String analyzerName, String version) {
        this.analyzerName = analyzerName != null ? analyzerName : "Standard Analyzer";
        this.version = version != null ? version : "1.0";
        this.totalProcessedCharacters = 0;
    }

    public String getAnalyzerName() {
        return analyzerName;
    }

    public String getVersion() {
        return version;
    }

    public long getTotalProcessedCharacters() {
        return totalProcessedCharacters;
    }

    /**
     * Validates document input before processing.
     * Protected helper method demonstrating access control and unchecked exceptions.
     */
    protected void validateDocument(TextDocument document) {
        if (document == null) {
            throw new InvalidInputException("TextDocument must not be null.");
        }
    }

    /**
     * Overloaded implementation from TextAnalyzer interface.
     * Demonstrates Method Overloading and Adapter/Delegation.
     */
    @Override
    public AnalysisResult analyze(String text) throws TextAnalysisException {
        if (text == null) {
            throw new InvalidInputException("Input text cannot be null.");
        }
        return analyze(new TextDocument(text));
    }

    /**
     * Abstract method to be implemented by concrete subclasses.
     * Demonstrates Dynamic Method Dispatch.
     */
    @Override
    public abstract AnalysisResult analyze(TextDocument document) throws TextAnalysisException;
}
