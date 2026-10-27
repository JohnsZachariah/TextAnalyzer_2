package com.textanalyzer.pattern;

import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.util.JsonUtil;

/**
 * Concrete strategy generating a JSON-formatted analysis report.
 * Demonstrates Strategy Pattern and JsonUtil integration.
 */
public class JsonReportExporter implements ReportExporter {

    @Override
    public String export(AnalysisResult result) {
        return JsonUtil.toJson(result);
    }

    @Override
    public String getFormatName() {
        return "JSON";
    }

    @Override
    public String getFileExtension() {
        return ".json";
    }
}
