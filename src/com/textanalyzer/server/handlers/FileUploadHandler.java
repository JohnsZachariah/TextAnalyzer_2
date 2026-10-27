package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.core.TextAnalyzer;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.TextDocument;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.util.JsonUtil;
import java.util.Map;

/**
 * REST Handler for POST /api/upload.
 * Receives file content uploaded from the browser, creates a TextDocument,
 * and executes comprehensive text analysis.
 */
public class FileUploadHandler extends BaseApiHandler {

    private final TextAnalyzer textAnalyzer;

    public FileUploadHandler(TextAnalyzer textAnalyzer) {
        this.textAnalyzer = textAnalyzer;
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, ApiResponse.error("Only POST method allowed").toJson());
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseJsonObject(body);

        String fileName = (String) req.getOrDefault("fileName", "UploadedFile.txt");
        String content = (String) req.getOrDefault("content", "");

        TextDocument doc = new TextDocument(content, fileName);
        AnalysisResult result = textAnalyzer.analyze(doc);

        String resultJson = JsonUtil.toJson(result);
        sendJson(exchange, 200, ApiResponse.ok("File analyzed successfully: " + fileName, resultJson).toJson());
    }
}
