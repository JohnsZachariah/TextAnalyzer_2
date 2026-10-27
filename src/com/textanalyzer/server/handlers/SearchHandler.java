package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.core.Searchable;
import com.textanalyzer.model.SearchResult;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.SessionActivityService;
import com.textanalyzer.util.JsonUtil;
import java.util.Map;

/**
 * REST Handler for POST /api/search.
 * Demonstrates Dependency Inversion (depends on Searchable interface).
 */
public class SearchHandler extends BaseApiHandler {

    private final Searchable searchable;
    private final SessionActivityService sessionService;

    public SearchHandler(Searchable searchable) {
        this.searchable = searchable;
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
        String query = (String) req.getOrDefault("query", "");
        boolean matchCase = Boolean.TRUE.equals(req.get("matchCase"));
        boolean wholeWord = Boolean.TRUE.equals(req.get("wholeWord"));
        String sessionId = req.get("sessionId") != null ? String.valueOf(req.get("sessionId")) : null;

        SearchResult result = searchable.search(text, query, matchCase, wholeWord);
        String resultJson = JsonUtil.toJson(result);
        sessionService.log(
                sessionId,
                "SEARCH",
                query,
                result.getOccurrences() + " occurrence(s)",
                "{\"found\":" + result.isFound() + "}"
        );

        sendJson(exchange, 200, ApiResponse.ok("Search completed", resultJson).toJson());
    }
}
