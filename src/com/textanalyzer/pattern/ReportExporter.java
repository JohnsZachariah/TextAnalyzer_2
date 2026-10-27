package com.textanalyzer.pattern;

import com.textanalyzer.model.AnalysisResult;

/**
 * Strategy interface defining report export behavior.
 * Demonstrates the Strategy Design Pattern.
 */
public interface ReportExporter {

    /**
     * Formats an AnalysisResult into an exported string representation.
     */
    String export(AnalysisResult result);

    /**
     * Returns the name of the export format (e.g. "TEXT", "JSON").
     */
    String getFormatName();

    /**
     * Returns the file extension for this format (e.g. ".txt", ".json").
     */
    String getFileExtension();
}
