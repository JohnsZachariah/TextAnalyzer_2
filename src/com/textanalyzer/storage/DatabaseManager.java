package com.textanalyzer.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton database manager handling SQLite local database operations via JDBC.
 * Fulfills:
 * - Module 3: Singleton Pattern with Thread-Safe Double-Checked Locking
 * - Module 4: Data Persistence using SQL and JDBC Connectivity
 * - Robust error handling with graceful fallback
 */
public class DatabaseManager {

    private static volatile DatabaseManager instance;
    private static final String DB_DIR = "data";
    private static final String DB_FILE = "data/textanalyzer.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    private boolean driverAvailable = false;

    private DatabaseManager() {
        init();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    private void init() {
        try {
            Path dir = Paths.get(DB_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Class.forName("org.sqlite.JDBC");
            driverAvailable = true;
            createTablesIfNotExist();
            System.out.println("[DatabaseManager] SQLite JDBC Driver loaded and connected to: " + DB_FILE);
        } catch (ClassNotFoundException e) {
            driverAvailable = false;
            System.err.println("[DatabaseManager] SQLite JDBC driver not found on classpath: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Failed to initialize SQLite database: " + e.getMessage());
        }
    }

    public boolean isAvailable() {
        return driverAvailable;
    }

    public Connection getConnection() throws SQLException {
        if (!driverAvailable) {
            throw new SQLException("SQLite JDBC Driver is not available on classpath.");
        }
        return DriverManager.getConnection(JDBC_URL);
    }

    private void createTablesIfNotExist() {
        String createAnalysesTable = "CREATE TABLE IF NOT EXISTS analyses (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "preview TEXT, " +
                "word_count INTEGER, " +
                "char_count INTEGER, " +
                "sentence_count INTEGER, " +
                "saved_at TEXT" +
                ");";

        String createDictionaryTable = "CREATE TABLE IF NOT EXISTS dictionary_cache (" +
                "word TEXT PRIMARY KEY, " +
                "phonetic TEXT, " +
                "definition_json TEXT, " +
                "cached_at TEXT" +
                ");";

        String createTranslationTable = "CREATE TABLE IF NOT EXISTS translation_cache (" +
                "source_hash TEXT NOT NULL, " +
                "source_text TEXT, " +
                "target_lang TEXT NOT NULL, " +
                "translated_text TEXT, " +
                "created_at TEXT, " +
                "PRIMARY KEY (source_hash, target_lang)" +
                ");";

        String createSessionsTable = "CREATE TABLE IF NOT EXISTS sessions (" +
                "id TEXT PRIMARY KEY, " +
                "created_at TEXT NOT NULL, " +
                "last_active_at TEXT NOT NULL" +
                ");";

        String createActivitiesTable = "CREATE TABLE IF NOT EXISTS session_activities (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "session_id TEXT NOT NULL, " +
                "activity_type TEXT NOT NULL, " +
                "source_text TEXT, " +
                "result_text TEXT, " +
                "extra_json TEXT, " +
                "created_at TEXT NOT NULL, " +
                "FOREIGN KEY(session_id) REFERENCES sessions(id)" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createAnalysesTable);
            stmt.execute(createDictionaryTable);
            stmt.execute(createTranslationTable);
            migrateTranslationCacheIfNeeded(stmt);
            stmt.execute(createSessionsTable);
            stmt.execute(createActivitiesTable);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_activity_session ON session_activities(session_id, id DESC)");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Table creation warning: " + e.getMessage());
        }
    }

    private void migrateTranslationCacheIfNeeded(Statement stmt) {
        try {
            String ddl = null;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT sql FROM sqlite_master WHERE type='table' AND name='translation_cache'")) {
                if (rs.next()) {
                    ddl = rs.getString("sql");
                }
            }
            if (ddl != null && ddl.contains("source_hash TEXT PRIMARY KEY")) {
                stmt.execute("ALTER TABLE translation_cache RENAME TO translation_cache_old");
                stmt.execute("CREATE TABLE translation_cache (" +
                        "source_hash TEXT NOT NULL, " +
                        "source_text TEXT, " +
                        "target_lang TEXT NOT NULL, " +
                        "translated_text TEXT, " +
                        "created_at TEXT, " +
                        "PRIMARY KEY (source_hash, target_lang))");
                stmt.execute("INSERT OR IGNORE INTO translation_cache " +
                        "(source_hash, source_text, target_lang, translated_text, created_at) " +
                        "SELECT source_hash, source_text, target_lang, translated_text, created_at " +
                        "FROM translation_cache_old");
                stmt.execute("DROP TABLE translation_cache_old");
                System.out.println("[DatabaseManager] Migrated translation_cache to composite primary key.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] translation_cache migration warning: " + e.getMessage());
        }
    }

    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("driverAvailable", driverAvailable);
        stats.put("databaseType", "SQLite (Local File)");
        stats.put("databaseFile", DB_FILE);
        stats.put("jdbcUrl", JDBC_URL);

        File f = new File(DB_FILE);
        stats.put("fileSizeKb", f.exists() ? f.length() / 1024 : 0);

        if (!driverAvailable) {
            stats.put("error", "JDBC driver not loaded");
            return stats;
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM analyses")) {
                stats.put("analysesCount", rs.next() ? rs.getInt("c") : 0);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM dictionary_cache")) {
                stats.put("cachedWordsCount", rs.next() ? rs.getInt("c") : 0);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM translation_cache")) {
                stats.put("cachedTranslationsCount", rs.next() ? rs.getInt("c") : 0);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM sessions")) {
                stats.put("sessionsCount", rs.next() ? rs.getInt("c") : 0);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM session_activities")) {
                stats.put("activitiesCount", rs.next() ? rs.getInt("c") : 0);
            }
            stats.put("status", "ONLINE");
        } catch (Exception e) {
            stats.put("status", "ERROR");
            stats.put("error", e.getMessage());
        }
        return stats;
    }

    public void clearTable(String tableName) throws SQLException {
        if (!driverAvailable) return;
        if (!"analyses".equals(tableName)
                && !"dictionary_cache".equals(tableName)
                && !"translation_cache".equals(tableName)
                && !"sessions".equals(tableName)
                && !"session_activities".equals(tableName)) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + tableName);
        }
    }
}
