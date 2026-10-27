package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.core.Transformable;
import com.textanalyzer.model.CaseType;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.util.JsonUtil;
import java.util.Map;

/**
 * REST Handler for POST /api/case-convert.
 * Supports UPPERCASE, LOWERCASE, TITLE_CASE, SENTENCE_CASE.
 */
public class CaseConvertHandler extends BaseApiHandler {

    private final Transformable transformable;

    public CaseConvertHandler(Transformable transformable) {
        this.transformable = transformable;
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
        String caseTypeStr = (String) req.getOrDefault("caseType", "LOWERCASE");

        CaseType caseType = CaseType.fromString(caseTypeStr);
        String convertedText = transformable.convertCase(text, caseType);

        String responseData = String.format("{\"convertedText\":\"%s\",\"appliedCase\":\"%s\"}",
                JsonUtil.escape(convertedText), caseType.name());
        sendJson(exchange, 200, ApiResponse.ok("Case converted successfully", responseData).toJson());
    }
}
