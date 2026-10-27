package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.storage.DatabaseManager;
import com.textanalyzer.util.JsonUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API Handler for Local SQL Database & JDBC Studio operations at /api/database/*.
 * Supports:
 * - High-level database status & JDBC metadata inspection
 * - Table schema exploration and row browsing
 * - Live interactive SQL SELECT query console
 * - One-click SQL Dump export (data/textanalyzer_dump.sql) and direct file download
 * - CSV export for all database tables (data/exports/*.csv) and direct file download
 * - Database backup creation (data/backups/textanalyzer_backup_*.db)
 * - Demo records seeding with transaction and batch processing
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
        String path = exchange.getRequestURI().getPath();

        if ("GET".equals(method)) {
            handleGet(exchange, path);
        } else if ("POST".equals(method)) {
            handlePost(exchange, path);
        } else {
            sendJson(exchange, 405, ApiResponse.error("Method not allowed").toJson());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws Exception {
        if (path.endsWith("/tables")) {
            // Return all table schemas and counts
            List<String> tables = databaseManager.getManagedTables();
            List<Map<String, Object>> list = new ArrayList<>();
            for (String t : tables) {
                Map<String, Object> tInfo = new LinkedHashMap<>();
                tInfo.put("tableName", t);
                try {
                    tInfo.put("schema", databaseManager.getTableSchema(t));
                    Map<String, Object> data = databaseManager.getTableData(t, 1, 0);
                    tInfo.put("totalRows", data.get("totalRows"));
                } catch (Exception e) {
                    tInfo.put("error", e.getMessage());
                }
                list.add(tInfo);
            }
            sendJson(exchange, 200, ApiResponse.ok("Tables schema", mapToJson(Map.of("tables", list))).toJson());

        } else if (path.endsWith("/table")) {
            // Query parameters: name, limit, offset
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            String tableName = q.getOrDefault("name", "analyses");
            int limit = parseInt(q.get("limit"), 50);
            int offset = parseInt(q.get("offset"), 0);

            try {
                Map<String, Object> data = databaseManager.getTableData(tableName, limit, offset);
                sendJson(exchange, 200, ApiResponse.ok("Table data for " + tableName, mapToJson(data)).toJson());
            } catch (IllegalArgumentException | SQLException e) {
                sendJson(exchange, 400, ApiResponse.error("Error querying table: " + e.getMessage()).toJson());
            }

        } else if (path.endsWith("/download-dump")) {
            // Serve data/textanalyzer_dump.sql directly to browser
            File dump = databaseManager.generateSqlDump();
            serveFileDownload(exchange, dump, "textanalyzer_dump.sql", "application/sql");

        } else if (path.endsWith("/download-csv")) {
            // Serve exported CSV for table
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            String tableName = q.getOrDefault("table", "analyses");
            File csv = new File(DatabaseManager.EXPORTS_DIR, tableName + ".csv");
            if (!csv.exists()) {
                databaseManager.exportAllTablesToCsv();
            }
            if (csv.exists()) {
                serveFileDownload(exchange, csv, tableName + ".csv", "text/csv");
            } else {
                sendJson(exchange, 404, ApiResponse.error("CSV file not found for table " + tableName).toJson());
            }

        } else {
            // Standard /api/database status
            Map<String, Object> stats = databaseManager.getDatabaseStats();
            sendJson(exchange, 200, ApiResponse.ok("Database status", mapToJson(stats)).toJson());
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws Exception {
        if (path.endsWith("/query")) {
            // Live interactive SQL query execution
            String body = readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseJsonObject(body);
            String query = (String) req.get("query");
            int maxRows = req.get("maxRows") instanceof Number ? ((Number) req.get("maxRows")).intValue() : 50;

            try {
                Map<String, Object> res = databaseManager.executeCustomQuery(query, maxRows);
                sendJson(exchange, 200, ApiResponse.ok("Query executed successfully", mapToJson(res)).toJson());
            } catch (Exception e) {
                sendJson(exchange, 200, ApiResponse.error("SQL Error: " + e.getMessage()).toJson());
            }

        } else if (path.endsWith("/dump")) {
            // Generate SQL dump file on disk
            try {
                File dumpFile = databaseManager.generateSqlDump();
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("filePath", DatabaseManager.DUMP_FILE);
                res.put("absolutePath", dumpFile.getAbsolutePath());
                res.put("fileSizeKb", Math.round((dumpFile.length() / 1024.0) * 10.0) / 10.0);
                res.put("generatedAt", LocalDateTime.now().toString());
                sendJson(exchange, 200, ApiResponse.ok("SQL Dump generated successfully", mapToJson(res)).toJson());
            } catch (Exception e) {
                sendJson(exchange, 500, ApiResponse.error("Failed to generate SQL dump: " + e.getMessage()).toJson());
            }

        } else if (path.endsWith("/export-csv")) {
            // Export all tables to CSV
            try {
                List<File> files = databaseManager.exportAllTablesToCsv();
                List<Map<String, Object>> filesMeta = new ArrayList<>();
                for (File f : files) {
                    filesMeta.add(Map.of(
                            "fileName", f.getName(),
                            "path", f.getPath(),
                            "sizeBytes", f.length()
                    ));
                }
                sendJson(exchange, 200, ApiResponse.ok("Tables exported to CSV", mapToJson(Map.of(
                        "directory", DatabaseManager.EXPORTS_DIR,
                        "files", filesMeta
                ))).toJson());
            } catch (Exception e) {
                sendJson(exchange, 500, ApiResponse.error("Failed to export tables to CSV: " + e.getMessage()).toJson());
            }

        } else if (path.endsWith("/backup")) {
            // Create physical DB backup file
            try {
                File backup = databaseManager.createDatabaseBackup();
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("backupFile", backup.getName());
                res.put("path", backup.getPath());
                res.put("absolutePath", backup.getAbsolutePath());
                res.put("sizeBytes", backup.length());
                sendJson(exchange, 200, ApiResponse.ok("Database backup created", mapToJson(res)).toJson());
            } catch (Exception e) {
                sendJson(exchange, 500, ApiResponse.error("Failed to create backup: " + e.getMessage()).toJson());
            }

        } else if (path.endsWith("/seed")) {
            // Seed realistic demo records
            try {
                databaseManager.seedDemoData();
                Map<String, Object> stats = databaseManager.getDatabaseStats();
                sendJson(exchange, 200, ApiResponse.ok("Demo data seeded successfully", mapToJson(stats)).toJson());
            } catch (Exception e) {
                sendJson(exchange, 500, ApiResponse.error("Failed to seed demo data: " + e.getMessage()).toJson());
            }

        } else if (path.endsWith("/clear")) {
            // Clear table
            String body = readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseJsonObject(body);
            String table = (String) req.getOrDefault("table", "analyses");
            try {
                databaseManager.clearTable(table);
                sendJson(exchange, 200, ApiResponse.ok("Table " + table + " cleared successfully", "{}").toJson());
            } catch (Exception e) {
                sendJson(exchange, 400, ApiResponse.error("Failed to clear table: " + e.getMessage()).toJson());
            }

        } else {
            sendJson(exchange, 400, ApiResponse.error("Unknown database action").toJson());
        }
    }

    private void serveFileDownload(HttpExchange exchange, File file, String downloadName, String contentType) throws Exception {
        if (!file.exists()) {
            sendJson(exchange, 404, ApiResponse.error("File not found on disk").toJson());
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + downloadName + "\"");
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(file.length()));
        exchange.sendResponseHeaders(200, file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = fis.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
            os.flush();
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 0) {
                String k = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String v = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                map.put(k, v);
            }
        }
        return map;
    }

    private int parseInt(String val, int dflt) {
        if (val == null || val.isBlank()) return dflt;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return dflt;
        }
    }

    @SuppressWarnings("unchecked")
    private String mapToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Number || obj instanceof Boolean) {
            return String.valueOf(obj);
        }
        if (obj instanceof String) {
            return "\"" + JsonUtil.escape((String) obj) + "\"";
        }
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(mapToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(JsonUtil.escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(mapToJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        return "\"" + JsonUtil.escape(String.valueOf(obj)) + "\"";
    }
}
