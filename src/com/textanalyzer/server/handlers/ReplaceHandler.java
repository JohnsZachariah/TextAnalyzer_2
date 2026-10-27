package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.core.Transformable;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.SessionActivityService;
import com.textanalyzer.util.JsonUtil;
import java.util.Map;

/**
 * REST Handler for POST /api/replace.
 * Demonstrates Dependency Inversion (depends on Transformable interface).
 */
public class ReplaceHandler extends BaseApiHandler {

    private final Transformable transformable;
    private final SessionActivityService sessionService;

    public ReplaceHandler(Transformable transformable) {
        this.transformable = transformable;
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
        String target = (String) req.getOrDefault("target", "");
        String replacement = (String) req.getOrDefault("replacement", "");
        boolean matchCase = Boolean.TRUE.equals(req.get("matchCase"));
        String sessionId = req.get("sessionId") != null ? String.valueOf(req.get("sessionId")) : null;

        String updatedText = transformable.findAndReplace(text, target, replacement, matchCase);
        sessionService.log(
                sessionId,
                "REPLACE",
                target + " → " + replacement,
                updatedText,
                "{}"
        );

        String responseData = String.format("{\"updatedText\":\"%s\"}", JsonUtil.escape(updatedText));
        sendJson(exchange, 200, ApiResponse.ok("Text updated successfully", responseData).toJson());
    }
}
