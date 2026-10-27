package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.model.HistoryRecord;
import com.textanalyzer.model.WordFrequency;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.SessionActivityService;
import com.textanalyzer.storage.StorageService;
import com.textanalyzer.util.JsonUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST Handler for GET, POST, and DELETE /api/history.
 * Interacts with persistent storage (JDBC SQLite database with file fallback).
 */
public class HistoryHandler extends BaseApiHandler {

    private final StorageService storageService;
    private final SessionActivityService sessionService;

    public HistoryHandler(StorageService storageService) {
        this.storageService = storageService;
        this.sessionService = SessionActivityService.getInstance();
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();

        if ("GET".equals(method)) {
            List<HistoryRecord> records = storageService.loadHistory();
            String jsonArray = JsonUtil.historyToJson(records);
            sendJson(exchange, 200, ApiResponse.ok("History loaded", jsonArray).toJson());
        } else if ("POST".equals(method)) {
            String body = readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseJsonObject(body);

            String action = (String) req.getOrDefault("action", "save");
            if ("delete".equalsIgnoreCase(action)) {
                String id = (String) req.get("id");
                if (id != null) {
                    storageService.deleteAnalysisRecord(id);
                }
                List<HistoryRecord> updated = storageService.loadHistory();
                sendJson(exchange, 200, ApiResponse.ok("Record deleted successfully", JsonUtil.historyToJson(updated)).toJson());
                return;
            } else if ("clear".equalsIgnoreCase(action)) {
                storageService.clearHistory();
                sendJson(exchange, 200, ApiResponse.ok("History cleared", "[]").toJson());
                return;
            }

            String title = (String) req.getOrDefault("title", "Saved Analysis");
            String fullText = (String) req.getOrDefault("fullText", req.getOrDefault("preview", ""));
            if (fullText == null) {
                fullText = "";
            }
            String preview = (String) req.get("preview");
            if (preview == null || preview.isBlank()) {
                preview = fullText.length() > 160 ? fullText.substring(0, 160) + "..." : fullText;
            }
            int wordCount = ((Number) req.getOrDefault("wordCount", 0)).intValue();
            int charCount = ((Number) req.getOrDefault("charCount", 0)).intValue();
            int sentenceCount = ((Number) req.getOrDefault("sentenceCount", 0)).intValue();
            int vowelCount = ((Number) req.getOrDefault("vowelCount", 0)).intValue();
            int consonantCount = ((Number) req.getOrDefault("consonantCount", 0)).intValue();
            double readingEase = ((Number) req.getOrDefault("readingEase", 0.0)).doubleValue();
            String longestWord = (String) req.getOrDefault("longestWord", "");
            double readingTimeMinutes = ((Number) req.getOrDefault("readingTimeMinutes", 0.0)).doubleValue();

            List<WordFrequency> wordFreqs = new ArrayList<>();
            Object wfObj = req.get("wordFrequencies");
            if (wfObj instanceof List<?>) {
                for (Object item : (List<?>) wfObj) {
                    if (item instanceof Map<?, ?>) {
                        Map<?, ?> m = (Map<?, ?>) item;
                        String w = String.valueOf(m.get("word"));
                        int f = m.get("frequency") instanceof Number ? ((Number) m.get("frequency")).intValue() : 1;
                        wordFreqs.add(new WordFrequency(w, f));
                    }
                }
            }

            String id = "HIST-" + System.currentTimeMillis();
            HistoryRecord record = new HistoryRecord(
                    id, title, preview, null,
                    wordCount, charCount, sentenceCount,
                    vowelCount, consonantCount, readingEase,
                    longestWord, readingTimeMinutes, wordFreqs, fullText
            );

            storageService.saveAnalysisRecord(record);
            String sessionId = req.get("sessionId") != null ? String.valueOf(req.get("sessionId")) : null;
            sessionService.log(sessionId, "SAVE", fullText, title, "{\"historyId\":\"" + JsonUtil.escape(id) + "\"}");

            List<HistoryRecord> updated = storageService.loadHistory();
            sendJson(exchange, 200, ApiResponse.ok("Record saved successfully to database", JsonUtil.historyToJson(updated)).toJson());
        } else if ("DELETE".equals(method)) {
            String query = exchange.getRequestURI().getQuery();
            String id = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 0 && "id".equalsIgnoreCase(pair[0])) {
                        id = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                        break;
                    }
                }
            }
            if (id != null && !id.isEmpty()) {
                storageService.deleteAnalysisRecord(id);
                List<HistoryRecord> updated = storageService.loadHistory();
                sendJson(exchange, 200, ApiResponse.ok("Record deleted successfully", JsonUtil.historyToJson(updated)).toJson());
            } else {
                storageService.clearHistory();
                sendJson(exchange, 200, ApiResponse.ok("History cleared", "[]").toJson());
            }
        } else {
            sendJson(exchange, 405, ApiResponse.error("Method not allowed").toJson());
        }
    }
}
