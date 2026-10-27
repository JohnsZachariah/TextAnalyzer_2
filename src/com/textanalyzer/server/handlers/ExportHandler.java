package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.core.TextAnalyzer;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.TextDocument;
import com.textanalyzer.pattern.ReportExporter;
import com.textanalyzer.pattern.ReportExporterFactory;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.StorageService;
import com.textanalyzer.util.JsonUtil;
import java.io.File;
import java.util.Map;

/**
 * REST Handler for POST /api/export.
 * Uses ReportExporterFactory (Factory & Strategy patterns) and StorageService
 * to generate and persist downloadable reports.
 */
public class ExportHandler extends BaseApiHandler {

    private final TextAnalyzer textAnalyzer;
    private final StorageService storageService;

    public ExportHandler(TextAnalyzer textAnalyzer, StorageService storageService) {
        this.textAnalyzer = textAnalyzer;
        this.storageService = storageService;
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, ApiResponse.error("Only POST method allowed").toJson());
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseJsonObject(body);

        String text = (String) req.getOrDefault("text", "");
        String sourceName = (String) req.getOrDefault("sourceName", "Direct Input");
        String format = (String) req.getOrDefault("format", "TEXT");

        TextDocument doc = new TextDocument(text, sourceName);
        AnalysisResult result = textAnalyzer.analyze(doc);

        ReportExporter exporter = ReportExporterFactory.getExporter(format);
        String reportText = exporter.export(result);

        // Also save to disk permanently
        File savedFile = storageService.saveReportFile(result, format);

        String responseData = String.format(
                "{\"fileName\":\"%s\",\"format\":\"%s\",\"content\":\"%s\",\"savedPath\":\"%s\"}",
                JsonUtil.escape(savedFile.getName()),
                JsonUtil.escape(exporter.getFormatName()),
                JsonUtil.escape(reportText),
                JsonUtil.escape(savedFile.getAbsolutePath())
        );

        sendJson(exchange, 200, ApiResponse.ok("Report generated successfully", responseData).toJson());
    }
}
