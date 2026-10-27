package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.model.SessionActivity;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.SessionActivityService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST API for JDBC-backed sessions and activity logs at /api/sessions.
 */
public class SessionHandler extends BaseApiHandler {

    private final SessionActivityService sessionService;

    public SessionHandler() {
        this.sessionService = SessionActivityService.getInstance();
    }

    public SessionHandler(SessionActivityService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("GET".equals(method)) {
            String sessionId = readSessionId(exchange.getRequestURI().getQuery());
            List<SessionActivity> activities = sessionService.listActivities(sessionId, 80);
            String payload = "{\"sessions\":" + sessionService.sessionsToJson()
                    + ",\"activities\":" + sessionService.activitiesToJson(activities) + "}";
            sendJson(exchange, 200, ApiResponse.ok("Session activity loaded", payload).toJson());
            return;
        }

        if ("POST".equals(method) || "DELETE".equals(method)) {
            String sessionId = readSessionId(exchange.getRequestURI().getQuery());
            if ("POST".equals(method)) {
                java.util.Map<String, Object> req = com.textanalyzer.util.JsonUtil.parseJsonObject(readRequestBody(exchange));
                if (sessionId == null || sessionId.isBlank()) {
                    Object sid = req.get("sessionId");
                    sessionId = sid != null ? String.valueOf(sid) : null;
                }
                String action = String.valueOf(req.getOrDefault("action", "clear"));
                if (!"clear".equalsIgnoreCase(action)) {
                    sendJson(exchange, 400, ApiResponse.error("Unsupported session action").toJson());
                    return;
                }
            }
            int removed = sessionService.clearSessionActivities(sessionId);
            String payload = "{\"cleared\":" + removed + ",\"sessionId\":\""
                    + com.textanalyzer.util.JsonUtil.escape(sessionId != null ? sessionId : "") + "\"}";
            sendJson(exchange, 200, ApiResponse.ok("Session history cleared", payload).toJson());
            return;
        }

        sendJson(exchange, 405, ApiResponse.error("GET, POST, and DELETE are allowed on /api/sessions").toJson());
    }

    private static String readSessionId(String query) {
        if (query == null) {
            return null;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 0 && "sessionId".equalsIgnoreCase(pair[0])) {
                return pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            }
        }
        return null;
    }
}
