package com.textanalyzer.pattern;

/**
 * Factory class for creating ReportExporter instances.
 * Demonstrates the Factory Method Pattern.
 */
public final class ReportExporterFactory {

    private ReportExporterFactory() {
        throw new UnsupportedOperationException("Factory class");
    }

    /**
     * Creates and returns a ReportExporter instance based on the format.
     * @param format "TEXT" or "JSON" (case-insensitive).
     * @return Concrete ReportExporter.
     */
    public static ReportExporter getExporter(String format) {
        if (format == null) {
            return new TextReportExporter();
        }
        String clean = format.trim().toUpperCase();
        if ("JSON".equals(clean)) {
            return new JsonReportExporter();
        }
        return new TextReportExporter();
    }
}
