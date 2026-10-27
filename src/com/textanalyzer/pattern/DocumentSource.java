package com.textanalyzer.pattern;

import com.textanalyzer.exception.TextAnalysisException;
import com.textanalyzer.model.TextDocument;

/**
 * Target interface for loading documents from different inputs.
 * Part of the Adapter Design Pattern (OOP Syllabus Module 3).
 */
public interface DocumentSource {

    /**
     * Loads and returns a TextDocument ready for analysis.
     * @return Loaded TextDocument.
     * @throws TextAnalysisException If loading fails.
     */
    TextDocument loadDocument() throws TextAnalysisException;
}
