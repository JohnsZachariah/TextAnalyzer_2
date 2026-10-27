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
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, ApiResponse.error("Only GET is allowed on /api/sessions").toJson());
            return;
        }

        String sessionId = null;
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 0 && "sessionId".equalsIgnoreCase(pair[0])) {
                    sessionId = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                }
            }
        }

        List<SessionActivity> activities = sessionService.listActivities(sessionId, 80);
        String payload = "{\"sessions\":" + sessionService.sessionsToJson()
                + ",\"activities\":" + sessionService.activitiesToJson(activities) + "}";
        sendJson(exchange, 200, ApiResponse.ok("Session activity loaded", payload).toJson());
    }
}
