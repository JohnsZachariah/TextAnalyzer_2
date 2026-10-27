package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.DatabaseManager;
import com.textanalyzer.util.JsonUtil;
import java.util.Map;

/**
 * REST API Handler for Local Database inspection and statistics at /api/database.
 * Allows viewing JDBC connection status, table record counts, and database metadata.
 */
public class DatabaseHandler extends BaseApiHandler {

    private final DatabaseManager databaseManager;

    public DatabaseHandler() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public DatabaseHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();

        if ("GET".equals(method)) {
            Map<String, Object> stats = databaseManager.getDatabaseStats();
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                if (entry.getValue() instanceof Number || entry.getValue() instanceof Boolean) {
                    sb.append(entry.getValue());
                } else {
                    sb.append("\"").append(JsonUtil.escape(String.valueOf(entry.getValue()))).append("\"");
                }
            }
            sb.append("}");
            sendJson(exchange, 200, ApiResponse.ok("Database status", sb.toString()).toJson());
        } else if ("POST".equals(method)) {
            String path = exchange.getRequestURI().getPath();
            if (path.endsWith("/clear")) {
                String body = readRequestBody(exchange);
                Map<String, Object> req = JsonUtil.parseJsonObject(body);
                String table = (String) req.getOrDefault("table", "analyses");
                databaseManager.clearTable(table);
                sendJson(exchange, 200, ApiResponse.ok("Table " + table + " cleared successfully", "{}").toJson());
            } else {
                sendJson(exchange, 400, ApiResponse.error("Unknown database action").toJson());
            }
        } else {
            sendJson(exchange, 405, ApiResponse.error("Method not allowed").toJson());
        }
    }
}
