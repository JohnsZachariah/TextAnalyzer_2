package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.core.DictionaryService;
import com.textanalyzer.model.DictionaryEntry;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.SessionActivityService;
import com.textanalyzer.util.JsonUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * REST API Handler for Free Dictionary lookups at /api/dictionary.
 * Supports both GET (/api/dictionary?word=...) and POST ({ "word": "..." }).
 */
public class DictionaryHandler extends BaseApiHandler {

    private final DictionaryService dictionaryService;
    private final SessionActivityService sessionService;

    public DictionaryHandler() {
        this(DictionaryService.getInstance());
    }

    public DictionaryHandler(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
        this.sessionService = SessionActivityService.getInstance();
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();
        String word = null;
        String sessionId = null;

        if ("GET".equals(method)) {
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 0) continue;
                    String key = pair[0];
                    String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                    if ("word".equalsIgnoreCase(key)) word = value;
                    else if ("sessionId".equalsIgnoreCase(key)) sessionId = value;
                }
            }
        } else if ("POST".equals(method)) {
            String body = readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseJsonObject(body);
            word = (String) req.get("word");
            if (req.get("sessionId") != null) sessionId = String.valueOf(req.get("sessionId"));
        } else {
            sendJson(exchange, 405, ApiResponse.error("Method not allowed").toJson());
            return;
        }

        if (word == null || word.trim().isEmpty()) {
            sendJson(exchange, 400, ApiResponse.error("Parameter 'word' is required.").toJson());
            return;
        }

        try {
            DictionaryEntry entry = dictionaryService.lookupWord(word.trim());
            String firstDef = "";
            if (!entry.getMeanings().isEmpty() && !entry.getMeanings().get(0).getDefinitions().isEmpty()) {
                firstDef = entry.getMeanings().get(0).getDefinitions().get(0).getDefinition();
            }
            sessionService.log(
                    sessionId,
                    "DICTIONARY",
                    entry.getWord(),
                    firstDef,
                    "{\"fromCache\":" + entry.isFromCache() + "}"
            );
            sendJson(exchange, 200, ApiResponse.ok("Definition retrieved successfully", entry.toJson()).toJson());
        } catch (Exception e) {
            sendJson(exchange, 404, ApiResponse.error(e.getMessage()).toJson());
        }
    }
}
