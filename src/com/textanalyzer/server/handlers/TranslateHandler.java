package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.core.TranslationService;
import com.textanalyzer.core.TranslationService.TranslationResult;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.SessionActivityService;
import com.textanalyzer.util.JsonUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * REST API Handler for English to Hindi/Malayalam translation at /api/translate.
 */
public class TranslateHandler extends BaseApiHandler {

    private final TranslationService translationService;
    private final SessionActivityService sessionService;

    public TranslateHandler() {
        this(TranslationService.getInstance());
    }

    public TranslateHandler(TranslationService translationService) {
        this.translationService = translationService;
        this.sessionService = SessionActivityService.getInstance();
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();
        String text = null;
        String targetLang = "hi";
        String sessionId = null;

        if ("GET".equals(method)) {
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 0) continue;
                    String key = pair[0];
                    String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                    if ("text".equalsIgnoreCase(key)) text = value;
                    else if ("targetLang".equalsIgnoreCase(key) || "lang".equalsIgnoreCase(key)) targetLang = value;
                    else if ("sessionId".equalsIgnoreCase(key)) sessionId = value;
                }
            }
        } else if ("POST".equals(method)) {
            String body = readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseJsonObject(body);
            text = (String) req.get("text");
            if (req.get("targetLang") != null) targetLang = String.valueOf(req.get("targetLang"));
            else if (req.get("lang") != null) targetLang = String.valueOf(req.get("lang"));
            if (req.get("sessionId") != null) sessionId = String.valueOf(req.get("sessionId"));
        } else {
            sendJson(exchange, 405, ApiResponse.error("Method not allowed").toJson());
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            sendJson(exchange, 400, ApiResponse.error("Parameter 'text' is required for translation.").toJson());
            return;
        }

        try {
            TranslationResult result = translationService.translate(text, targetLang);
            sessionService.log(
                    sessionId,
                    "TRANSLATE",
                    result.getSourceText(),
                    result.getTranslatedText(),
                    "{\"targetLang\":\"" + JsonUtil.escape(result.getTargetLang()) + "\",\"fromCache\":"
                            + result.isFromCache() + "}"
            );
            sendJson(exchange, 200, ApiResponse.ok("Translation succeeded", result.toJson()).toJson());
        } catch (Exception e) {
            sendJson(exchange, 500, ApiResponse.error(e.getMessage()).toJson());
        }
    }
}
