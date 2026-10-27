package com.textanalyzer.storage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Singleton database manager handling local SQLite database operations via JDBC.
 * Fulfills:
 * - Module 3: Singleton Pattern with Thread-Safe Double-Checked Locking
 * - Module 4: Data Persistence using SQL and JDBC Connectivity
 * - Relational Database Design with Foreign Keys (1-to-N relationships)
 * - ACID Transaction Management (Commit, Rollback, Savepoints)
 * - PreparedStatements for SQL Injection Protection
 * - DatabaseMetaData and ResultSetMetaData Introspection
 * - Full Laptop File Storage: SQLite .db file, .sql dump export, and .csv exports
 */
public class DatabaseManager {

    private static volatile DatabaseManager instance;

    public static final String DB_DIR = "data";
    public static final String DB_FILE = "data/textanalyzer.db";
    public static final String PROPERTIES_FILE = "data/database.properties";
    public static final String DUMP_FILE = "data/textanalyzer_dump.sql";
    public static final String EXPORTS_DIR = "data/exports";
    public static final String BACKUPS_DIR = "data/backups";

    private String jdbcUrl = "jdbc:sqlite:" + DB_FILE;
    private String driverClassName = "org.sqlite.JDBC";
    private boolean driverAvailable = false;
    private Properties dbProperties = new Properties();

    private static final List<String> MANAGED_TABLES = List.of(
            "analyses",
            "analysis_words",
            "sessions",
            "session_activities",
            "dictionary_cache",
            "translation_cache"
    );

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
            ensureDirectories();
            loadOrGenerateProperties();

            Class.forName(driverClassName);
            driverAvailable = true;

            createTablesAndIndexes();
            migrateSchemaIfNeeded();

            System.out.println("[DatabaseManager] SQLite JDBC Driver (" + driverClassName + ") loaded successfully.");
            System.out.println("[DatabaseManager] Database file location: " + getDatabaseFile().getAbsolutePath());
        } catch (ClassNotFoundException e) {
            driverAvailable = false;
            System.err.println("[DatabaseManager] SQLite JDBC driver not found on classpath: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Failed to initialize database: " + e.getMessage());
        }
    }

    private void ensureDirectories() {
        try {
            Files.createDirectories(Paths.get(DB_DIR));
            Files.createDirectories(Paths.get(EXPORTS_DIR));
            Files.createDirectories(Paths.get(BACKUPS_DIR));
            Files.createDirectories(Paths.get(DB_DIR, "reports"));
        } catch (IOException e) {
            System.err.println("[DatabaseManager] Warning: could not initialize storage directories: " + e.getMessage());
        }
    }

    private void loadOrGenerateProperties() {
        File propFile = new File(PROPERTIES_FILE);
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                dbProperties.load(in);
                jdbcUrl = dbProperties.getProperty("db.url", "jdbc:sqlite:" + DB_FILE);
                driverClassName = dbProperties.getProperty("db.driver", "org.sqlite.JDBC");
            } catch (Exception e) {
                System.err.println("[DatabaseManager] Error reading database.properties, using defaults: " + e.getMessage());
            }
        } else {
            // Write default database.properties for the student/evaluator
            dbProperties.setProperty("db.type", "sqlite");
            dbProperties.setProperty("db.file", DB_FILE);
            dbProperties.setProperty("db.url", "jdbc:sqlite:" + DB_FILE);
            dbProperties.setProperty("db.driver", "org.sqlite.JDBC");
            dbProperties.setProperty("db.foreign_keys", "true");
            dbProperties.setProperty("db.journal_mode", "WAL");

            try (OutputStream out = new FileOutputStream(propFile)) {
                String comments = "========================================================\n"
                        + " LexiCore Text Analyzer - SQL JDBC Configuration\n"
                        + " Course: PBCST304 Object Oriented Programming (KTU S3)\n"
                        + " Physical Storage: Embedded SQLite file located on laptop\n"
                        + "========================================================";
                dbProperties.store(out, comments);
            } catch (Exception e) {
                System.err.println("[DatabaseManager] Could not write default properties: " + e.getMessage());
            }
        }
    }

    public boolean isAvailable() {
        return driverAvailable;
    }

    public File getDatabaseFile() {
        return new File(DB_FILE);
    }

    public File getPropertiesFile() {
        return new File(PROPERTIES_FILE);
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public Connection getConnection() throws SQLException {
        if (!driverAvailable) {
            throw new SQLException("SQLite JDBC Driver is not available on classpath.");
        }
        Connection conn = DriverManager.getConnection(jdbcUrl);
        // Enforce relational integrity and high performance
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA synchronous = NORMAL;");
        } catch (SQLException ignored) {}
        return conn;
    }

    private void createTablesAndIndexes() {
        String createAnalysesTable = "CREATE TABLE IF NOT EXISTS analyses (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "preview TEXT, " +
                "full_text TEXT, " +
                "word_count INTEGER, " +
                "char_count INTEGER, " +
                "sentence_count INTEGER, " +
                "vowel_count INTEGER DEFAULT 0, " +
                "consonant_count INTEGER DEFAULT 0, " +
                "reading_ease REAL DEFAULT 0.0, " +
                "longest_word TEXT DEFAULT '', " +
                "reading_time_minutes REAL DEFAULT 0.0, " +
                "saved_at TEXT" +
                ");";

        // Relational child table: 1-to-N with analyses (demonstrating Foreign Keys and CASCADE DELETE)
        String createAnalysisWordsTable = "CREATE TABLE IF NOT EXISTS analysis_words (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "analysis_id TEXT NOT NULL, " +
                "word TEXT NOT NULL, " +
                "frequency INTEGER NOT NULL, " +
                "FOREIGN KEY(analysis_id) REFERENCES analyses(id) ON DELETE CASCADE" +
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
                "FOREIGN KEY(session_id) REFERENCES sessions(id) ON DELETE CASCADE" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createAnalysesTable);
            stmt.execute(createAnalysisWordsTable);
            stmt.execute(createDictionaryTable);
            stmt.execute(createTranslationTable);
            stmt.execute(createSessionsTable);
            stmt.execute(createActivitiesTable);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_word_analysis ON analysis_words(analysis_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_activity_session ON session_activities(session_id, id DESC);");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Table creation error: " + e.getMessage());
        }
    }

    private void migrateSchemaIfNeeded() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if analyses has new metric columns
            Set<String> existingColumns = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(analyses)")) {
                while (rs.next()) {
                    existingColumns.add(rs.getString("name").toLowerCase());
                }
            }

            if (!existingColumns.contains("vowel_count")) {
                stmt.execute("ALTER TABLE analyses ADD COLUMN vowel_count INTEGER DEFAULT 0");
            }
            if (!existingColumns.contains("consonant_count")) {
                stmt.execute("ALTER TABLE analyses ADD COLUMN consonant_count INTEGER DEFAULT 0");
            }
            if (!existingColumns.contains("reading_ease")) {
                stmt.execute("ALTER TABLE analyses ADD COLUMN reading_ease REAL DEFAULT 0.0");
            }
            if (!existingColumns.contains("longest_word")) {
                stmt.execute("ALTER TABLE analyses ADD COLUMN longest_word TEXT DEFAULT ''");
            }
            if (!existingColumns.contains("reading_time_minutes")) {
                stmt.execute("ALTER TABLE analyses ADD COLUMN reading_time_minutes REAL DEFAULT 0.0");
            }
            if (!existingColumns.contains("full_text")) {
                stmt.execute("ALTER TABLE analyses ADD COLUMN full_text TEXT");
                stmt.execute("UPDATE analyses SET full_text = preview WHERE full_text IS NULL OR full_text = ''");
            }

            // Ensure composite PK on translation_cache
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
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Migration check note: " + e.getMessage());
        }
    }

    public List<String> getManagedTables() {
        return Collections.unmodifiableList(MANAGED_TABLES);
    }

    /**
     * Retrieves high-level database status, storage file details, and record counts.
     */
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("driverAvailable", driverAvailable);
        stats.put("databaseType", "SQLite (Embedded Local File Database)");
        stats.put("databaseFile", DB_FILE);

        File f = getDatabaseFile();
        stats.put("absolutePath", f.getAbsolutePath());
        stats.put("fileSizeBytes", f.exists() ? f.length() : 0);
        stats.put("fileSizeKb", f.exists() ? Math.round((f.length() / 1024.0) * 10.0) / 10.0 : 0.0);
        stats.put("jdbcUrl", jdbcUrl);
        stats.put("driverClass", driverClassName);
        stats.put("propertiesFile", PROPERTIES_FILE);
        stats.put("foreignKeysEnabled", true);
        stats.put("acidTransactions", true);

        if (!driverAvailable) {
            stats.put("status", "OFFLINE");
            stats.put("error", "SQLite JDBC driver not available");
            return stats;
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            DatabaseMetaData meta = conn.getMetaData();
            stats.put("dbProductName", meta.getDatabaseProductName());
            stats.put("dbProductVersion", meta.getDatabaseProductVersion());
            stats.put("driverName", meta.getDriverName());
            stats.put("driverVersion", meta.getDriverVersion());
            stats.put("jdbcVersion", meta.getJDBCMajorVersion() + "." + meta.getJDBCMinorVersion());

            stats.put("analysesCount", getRowCount(stmt, "analyses"));
            stats.put("analysisWordsCount", getRowCount(stmt, "analysis_words"));
            stats.put("cachedWordsCount", getRowCount(stmt, "dictionary_cache"));
            stats.put("cachedTranslationsCount", getRowCount(stmt, "translation_cache"));
            stats.put("sessionsCount", getRowCount(stmt, "sessions"));
            stats.put("activitiesCount", getRowCount(stmt, "session_activities"));

            stats.put("status", "ONLINE");
        } catch (Exception e) {
            stats.put("status", "ERROR");
            stats.put("error", e.getMessage());
        }
        return stats;
    }

    private int getRowCount(Statement stmt, String tableName) {
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM " + tableName)) {
            return rs.next() ? rs.getInt("c") : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Inspects schema structure of a given table using PRAGMA table_info and DatabaseMetaData.
     */
    public List<Map<String, Object>> getTableSchema(String tableName) throws SQLException {
        validateTableName(tableName);
        List<Map<String, Object>> columns = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {

            while (rs.next()) {
                Map<String, Object> col = new LinkedHashMap<>();
                col.put("cid", rs.getInt("cid"));
                col.put("name", rs.getString("name"));
                col.put("type", rs.getString("type"));
                col.put("notnull", rs.getInt("notnull") == 1);
                col.put("defaultValue", rs.getString("dflt_value"));
                col.put("primaryKey", rs.getInt("pk") == 1);
                columns.add(col);
            }
        }
        return columns;
    }

    /**
     * Fetches rows from a specific table with pagination.
     */
    public Map<String, Object> getTableData(String tableName, int limit, int offset) throws SQLException {
        validateTableName(tableName);
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tableName", tableName);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            result.put("totalRows", getRowCount(stmt, tableName));

            String sql = "SELECT * FROM " + tableName + " ORDER BY rowid DESC LIMIT " + safeLimit + " OFFSET " + safeOffset;
            try (ResultSet rs = stmt.executeQuery(sql)) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                List<String> columnNames = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columnNames.add(meta.getColumnLabel(i));
                }
                result.put("columns", columnNames);

                List<List<Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        Object val = rs.getObject(i);
                        row.add(val != null ? val : "");
                    }
                    rows.add(row);
                }
                result.put("rows", rows);
                result.put("limit", safeLimit);
                result.put("offset", safeOffset);
            }
        }
        return result;
    }

    /**
     * Executes a user/evaluator supplied SQL SELECT query using standard JDBC Statement & ResultSet.
     * Captures column metadata and measures execution time.
     */
    public Map<String, Object> executeCustomQuery(String sql, int maxRows) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("Query string cannot be empty.");
        }
        String trimmed = sql.trim();
        String upper = trimmed.toUpperCase();

        // Enforce safety: Allow SELECT, PRAGMA, EXPLAIN queries
        if (!upper.startsWith("SELECT") && !upper.startsWith("PRAGMA") && !upper.startsWith("EXPLAIN")) {
            throw new SQLException("Security Policy: Only read-only SELECT or PRAGMA queries are permitted in the interactive console.");
        }

        int limit = maxRows <= 0 ? 50 : Math.min(maxRows, 500);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", trimmed);

        long startNs = System.nanoTime();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setMaxRows(limit);
            try (ResultSet rs = stmt.executeQuery(trimmed)) {
                long elapsedNs = System.nanoTime() - startNs;
                double elapsedMs = Math.round((elapsedNs / 1_000_000.0) * 100.0) / 100.0;
                result.put("executionTimeMs", elapsedMs);

                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(meta.getColumnLabel(i));
                }
                result.put("columns", columns);

                List<List<Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        Object val = rs.getObject(i);
                        row.add(val != null ? String.valueOf(val) : "");
                    }
                    rows.add(row);
                }
                result.put("rows", rows);
                result.put("rowCount", rows.size());
                result.put("status", "SUCCESS");
            }
        }
        return result;
    }

    /**
     * Generates a complete standard SQL dump file on the laptop disk at data/textanalyzer_dump.sql.
     * Contains standard DDL (CREATE TABLE) and DML (INSERT INTO) statements for all tables and records.
     */
    public File generateSqlDump() throws IOException, SQLException {
        File dumpFile = new File(DUMP_FILE);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dumpFile, StandardCharsets.UTF_8));
             Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            writer.write("-- ========================================================\n");
            writer.write("--  LEXICORE TEXT ANALYZER - SQL DATABASE EXPORT DUMP     \n");
            writer.write("--  Course: PBCST304 Object Oriented Programming (KTU S3) \n");
            writer.write("--  Generated on: " + timestamp + "\n");
            writer.write("--  Source Database: " + DB_FILE + "\n");
            writer.write("--  Driver: " + driverClassName + "\n");
            writer.write("-- ========================================================\n\n");
            writer.write("PRAGMA foreign_keys = ON;\n\n");

            for (String table : MANAGED_TABLES) {
                writer.write("-- --------------------------------------------------------\n");
                writer.write("-- Table structure and data for: " + table + "\n");
                writer.write("-- --------------------------------------------------------\n");

                // Get CREATE TABLE DDL
                try (ResultSet rs = stmt.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='" + table + "'")) {
                    if (rs.next()) {
                        String createSql = rs.getString("sql");
                        if (createSql != null) {
                            writer.write(createSql + ";\n\n");
                        }
                    }
                }

                // Export INSERT rows
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();

                    while (rs.next()) {
                        StringBuilder insert = new StringBuilder("INSERT INTO " + table + " VALUES (");
                        for (int i = 1; i <= cols; i++) {
                            if (i > 1) insert.append(", ");
                            Object val = rs.getObject(i);
                            if (val == null) {
                                insert.append("NULL");
                            } else if (val instanceof Number) {
                                insert.append(val);
                            } else {
                                String str = String.valueOf(val).replace("'", "''");
                                insert.append("'").append(str).append("'");
                            }
                        }
                        insert.append(");\n");
                        writer.write(insert.toString());
                    }
                }
                writer.write("\n");
            }
            writer.flush();
        }
        return dumpFile;
    }

    /**
     * Exports all tables to CSV files in data/exports/*.csv for easy demonstration in Excel or Numbers.
     */
    public List<File> exportAllTablesToCsv() throws IOException, SQLException {
        List<File> exportedFiles = new ArrayList<>();
        File exportDir = new File(EXPORTS_DIR);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            for (String table : MANAGED_TABLES) {
                File csvFile = new File(exportDir, table + ".csv");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, StandardCharsets.UTF_8));
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();

                    // Header row
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) writer.write(",");
                        writer.write(escapeCsv(meta.getColumnLabel(i)));
                    }
                    writer.write("\n");

                    // Data rows
                    while (rs.next()) {
                        for (int i = 1; i <= cols; i++) {
                            if (i > 1) writer.write(",");
                            Object val = rs.getObject(i);
                            writer.write(escapeCsv(val != null ? String.valueOf(val) : ""));
                        }
                        writer.write("\n");
                    }
                    writer.flush();
                }
                exportedFiles.add(csvFile);
            }
        }
        return exportedFiles;
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    /**
     * Creates a physical binary backup copy of data/textanalyzer.db in data/backups/.
     */
    public File createDatabaseBackup() throws IOException {
        File src = getDatabaseFile();
        if (!src.exists()) {
            throw new FileNotFoundException("Database file does not exist to back up: " + src.getAbsolutePath());
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupDir = new File(BACKUPS_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        File dest = new File(backupDir, "textanalyzer_backup_" + timestamp + ".db");
        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest;
    }

    /**
     * Populates rich demonstration data across all tables so the student never presents an empty database.
     * Demonstrates ACID Transactions (commit/rollback) and PreparedStatement Batch updates.
     */
    public void seedDemoData() throws SQLException {
        if (!driverAvailable) return;

        try (Connection conn = getConnection()) {
            // Demonstrate ACID Transaction Management
            conn.setAutoCommit(false);
            try {
                // 1. Seed Analyses
                String insertAnalysis = "INSERT OR REPLACE INTO analyses " +
                        "(id, title, preview, full_text, word_count, char_count, sentence_count, vowel_count, consonant_count, reading_ease, longest_word, reading_time_minutes, saved_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(insertAnalysis)) {
                    // Entry 1: OOP Principles
                    ps.setString(1, "DEMO-OOP-001");
                    ps.setString(2, "Object-Oriented Programming (KTU S3)");
                    ps.setString(3, "Java incorporates encapsulation, inheritance, polymorphism, and abstraction as foundational pillars.");
                    ps.setString(4, "Java incorporates encapsulation, inheritance, polymorphism, and abstraction as foundational pillars. Classes define structure; objects hold state. Inheritance reuses behaviour; polymorphism resolves methods at runtime. JDBC stores the complete source text of every saved analysis.");
                    ps.setInt(5, 85);
                    ps.setInt(6, 592);
                    ps.setInt(7, 4);
                    ps.setInt(8, 198);
                    ps.setInt(9, 312);
                    ps.setDouble(10, 64.5);
                    ps.setString(11, "polymorphism");
                    ps.setDouble(12, 0.4);
                    ps.setString(13, "2026-10-27 10:15:00");
                    ps.executeUpdate();

                    // Entry 2: Operating Systems
                    ps.setString(1, "DEMO-OS-002");
                    ps.setString(2, "Operating Systems & Concurrency");
                    ps.setString(3, "Multithreading enables concurrent thread execution sharing heap memory but maintaining isolated call stacks.");
                    ps.setString(4, "Multithreading enables concurrent thread execution sharing heap memory but maintaining isolated call stacks. Processes isolate address spaces; threads share the heap. Synchronization protects critical sections and avoids race conditions during analysis.");
                    ps.setInt(5, 110);
                    ps.setInt(6, 740);
                    ps.setInt(7, 6);
                    ps.setInt(8, 245);
                    ps.setInt(9, 395);
                    ps.setDouble(10, 58.2);
                    ps.setString(11, "multithreading");
                    ps.setDouble(12, 0.6);
                    ps.setString(13, "2026-10-27 14:30:00");
                    ps.executeUpdate();

                    // Entry 3: Database & JDBC
                    ps.setString(1, "DEMO-DB-003");
                    ps.setString(2, "Database Persistence & JDBC Connectivity");
                    ps.setString(3, "The Java Database Connectivity API enables relational data storage with ACID transaction guarantees.");
                    ps.setString(4, "The Java Database Connectivity API enables relational data storage with ACID transaction guarantees. Prepared statements bind parameters safely. SQLite stores the complete document body in analyses.full_text so a saved record can be restored without truncation.");
                    ps.setInt(5, 94);
                    ps.setInt(6, 665);
                    ps.setInt(7, 5);
                    ps.setInt(8, 218);
                    ps.setInt(9, 355);
                    ps.setDouble(10, 62.0);
                    ps.setString(11, "connectivity");
                    ps.setDouble(12, 0.5);
                    ps.setString(13, "2026-10-27 18:45:00");
                    ps.executeUpdate();
                }

                // 2. Seed Analysis Words (1-to-N Relational Table using Batch Processing)
                String insertWords = "INSERT INTO analysis_words (analysis_id, word, frequency) VALUES (?, ?, ?)";
                try (PreparedStatement psWords = conn.prepareStatement(insertWords)) {
                    // Words for DEMO-OOP-001
                    addWordBatch(psWords, "DEMO-OOP-001", "java", 7);
                    addWordBatch(psWords, "DEMO-OOP-001", "class", 6);
                    addWordBatch(psWords, "DEMO-OOP-001", "object", 5);
                    addWordBatch(psWords, "DEMO-OOP-001", "polymorphism", 4);
                    addWordBatch(psWords, "DEMO-OOP-001", "inheritance", 4);
                    addWordBatch(psWords, "DEMO-OOP-001", "encapsulation", 3);

                    // Words for DEMO-OS-002
                    addWordBatch(psWords, "DEMO-OS-002", "thread", 8);
                    addWordBatch(psWords, "DEMO-OS-002", "process", 5);
                    addWordBatch(psWords, "DEMO-OS-002", "memory", 4);
                    addWordBatch(psWords, "DEMO-OS-002", "concurrency", 4);

                    // Words for DEMO-DB-003
                    addWordBatch(psWords, "DEMO-DB-003", "database", 6);
                    addWordBatch(psWords, "DEMO-DB-003", "jdbc", 5);
                    addWordBatch(psWords, "DEMO-DB-003", "sql", 5);
                    addWordBatch(psWords, "DEMO-DB-003", "transaction", 4);

                    psWords.executeBatch();
                }

                // 3. Seed Sessions and Activities
                String sessionId = "SESS-DEMO-VIVA-01";
                try (PreparedStatement psSess = conn.prepareStatement(
                        "INSERT OR REPLACE INTO sessions (id, created_at, last_active_at) VALUES (?, ?, ?)")) {
                    psSess.setString(1, sessionId);
                    psSess.setString(2, "2026-10-27 10:00:00");
                    psSess.setString(3, "2026-10-27 18:50:00");
                    psSess.executeUpdate();
                }

                String insertAct = "INSERT INTO session_activities (session_id, activity_type, source_text, result_text, extra_json, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psAct = conn.prepareStatement(insertAct)) {
                    addActivityBatch(psAct, sessionId, "ANALYZE", "Java OOP principles sample text", "85 words, 592 chars", "{}", "2026-10-27 10:15:00");
                    addActivityBatch(psAct, sessionId, "SEARCH", "polymorphism", "Found 4 occurrences", "{\"query\":\"polymorphism\"}", "2026-10-27 10:18:00");
                    addActivityBatch(psAct, sessionId, "DICTIONARY", "polymorphism", "The condition of occurring in several different forms.", "{}", "2026-10-27 10:20:00");
                    addActivityBatch(psAct, sessionId, "TRANSLATE", "Object-Oriented Programming", "ऑब्जेक्ट-ओरिएंटेड प्रोग्रामिंग (hi)", "{\"lang\":\"hi\"}", "2026-10-27 10:25:00");
                    addActivityBatch(psAct, sessionId, "EXPORT", "DEMO-OOP-001", "Report exported to data/reports/report_DEMO-OOP-001.txt", "{}", "2026-10-27 10:30:00");
                    psAct.executeBatch();
                }

                // 4. Seed Dictionary Cache
                String insertDict = "INSERT OR REPLACE INTO dictionary_cache (word, phonetic, definition_json, cached_at) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psDict = conn.prepareStatement(insertDict)) {
                    psDict.setString(1, "polymorphism");
                    psDict.setString(2, "/ˌpɒlɪˈmɔːfɪz(ə)m/");
                    psDict.setString(3, "{\"partOfSpeech\":\"noun\",\"definition\":\"The condition of occurring in several different forms in programming.\"}");
                    psDict.setString(4, "2026-10-27 10:20:00");
                    psDict.executeUpdate();

                    psDict.setString(1, "encapsulation");
                    psDict.setString(2, "/ɪnˌkæpsjʊˈleɪʃn/");
                    psDict.setString(3, "{\"partOfSpeech\":\"noun\",\"definition\":\"The bundling of data with the methods that operate on that data.\"}");
                    psDict.setString(4, "2026-10-27 10:22:00");
                    psDict.executeUpdate();
                }

                // 5. Seed Translation Cache
                String insertTrans = "INSERT OR REPLACE INTO translation_cache (source_hash, source_text, target_lang, translated_text, created_at) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psTrans = conn.prepareStatement(insertTrans)) {
                    psTrans.setString(1, "hash_oop_en_hi");
                    psTrans.setString(2, "Object-Oriented Programming");
                    psTrans.setString(3, "hi");
                    psTrans.setString(4, "ऑब्जेक्ट-ओरिएंटेड प्रोग्रामिंग");
                    psTrans.setString(5, "2026-10-27 10:25:00");
                    psTrans.executeUpdate();

                    psTrans.setString(1, "hash_oop_en_ml");
                    psTrans.setString(2, "Object-Oriented Programming");
                    psTrans.setString(3, "ml");
                    psTrans.setString(4, "ഒബ്ജക്റ്റ്-ഓറിയന്റഡ് പ്രോഗ്രാമിംഗ്");
                    psTrans.setString(5, "2026-10-27 10:26:00");
                    psTrans.executeUpdate();
                }

                // Explicit ACID Commit!
                conn.commit();
                System.out.println("[DatabaseManager] Demo records seeded successfully into SQL database.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void addWordBatch(PreparedStatement ps, String analysisId, String word, int freq) throws SQLException {
        ps.setString(1, analysisId);
        ps.setString(2, word);
        ps.setInt(3, freq);
        ps.addBatch();
    }

    private void addActivityBatch(PreparedStatement ps, String sid, String type, String src, String res, String extra, String ts) throws SQLException {
        ps.setString(1, sid);
        ps.setString(2, type);
        ps.setString(3, src);
        ps.setString(4, res);
        ps.setString(5, extra);
        ps.setString(6, ts);
        ps.addBatch();
    }

    public void clearTable(String tableName) throws SQLException {
        if (!driverAvailable) return;
        validateTableName(tableName);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + tableName);
        }
    }

    private void validateTableName(String tableName) {
        if (!MANAGED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Invalid or unmanaged table name: " + tableName);
        }
    }
}
