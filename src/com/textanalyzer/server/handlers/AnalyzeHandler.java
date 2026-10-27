package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.core.TextAnalyzer;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.TextDocument;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.SessionActivityService;
import com.textanalyzer.util.JsonUtil;
import java.util.Map;

/**
 * REST Handler for POST /api/analyze.
 * Demonstrates Dependency Inversion (depends on TextAnalyzer interface).
 */
public class AnalyzeHandler extends BaseApiHandler {

    private final TextAnalyzer textAnalyzer;
    private final SessionActivityService sessionService;

    public AnalyzeHandler(TextAnalyzer textAnalyzer) {
        this.textAnalyzer = textAnalyzer;
        this.sessionService = SessionActivityService.getInstance();
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
        String sessionId = req.get("sessionId") != null ? String.valueOf(req.get("sessionId")) : null;

        if (text == null) {
            text = "";
        }

        TextDocument doc = new TextDocument(text, sourceName);
        AnalysisResult result = textAnalyzer.analyze(doc);

        String resultJson = JsonUtil.toJson(result);
        sessionService.log(
                sessionId,
                "ANALYZE",
                text,
                result.getWordCount() + " words, " + result.getSentenceCount() + " sentences",
                "{\"sourceName\":\"" + JsonUtil.escape(sourceName) + "\",\"wordCount\":" + result.getWordCount() + "}"
        );
        sendJson(exchange, 200, ApiResponse.ok("Analysis completed successfully", resultJson).toJson());
    }
}
