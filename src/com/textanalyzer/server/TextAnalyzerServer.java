package com.textanalyzer.server;

import com.sun.net.httpserver.HttpServer;
import com.textanalyzer.core.ComprehensiveTextAnalyzer;
import com.textanalyzer.core.DictionaryService;
import com.textanalyzer.core.TranslationService;
import com.textanalyzer.server.handlers.*;
import com.textanalyzer.storage.DatabaseManager;
import com.textanalyzer.storage.JdbcStorageService;
import com.textanalyzer.storage.SessionActivityService;
import com.textanalyzer.storage.StorageService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP Server bridging the Java OOP backend with the HTML/CSS/JS frontend.
 * Demonstrates:
 * - Multi-threading with ExecutorService
 * - Clean RESTful Routing & Endpoint Management
 * - Separation of Concerns (MVC: Server acts as Controller dispatcher)
 * - Module 4: SQL Database Persistence via JDBC Connectivity
 * - Integration of Free Dictionary API and Free Translator API
 */
public class TextAnalyzerServer {

    private final int port;
    private final String webRoot;
    private HttpServer server;
    private final ComprehensiveTextAnalyzer analyzer;
    private final StorageService storageService;
    private final DictionaryService dictionaryService;
    private final TranslationService translationService;
    private final DatabaseManager databaseManager;

    public TextAnalyzerServer(int port, String webRoot) {
        this.port = port;
        this.webRoot = webRoot;
        this.analyzer = new ComprehensiveTextAnalyzer();
        this.storageService = new JdbcStorageService();
        this.dictionaryService = DictionaryService.getInstance();
        this.translationService = TranslationService.getInstance();
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static files handler (serves HTML, CSS, JS)
        StaticFileHandler staticHandler = new StaticFileHandler(webRoot);
        server.createContext("/", staticHandler);

        // Core Text Analysis REST Endpoints
        server.createContext("/api/analyze", new AnalyzeHandler(analyzer));
        server.createContext("/api/search", new SearchHandler(analyzer));
        server.createContext("/api/replace", new ReplaceHandler(analyzer));
        server.createContext("/api/case-convert", new CaseConvertHandler(analyzer));
        server.createContext("/api/upload", new FileUploadHandler(analyzer));
        server.createContext("/api/history", new HistoryHandler(storageService));
        server.createContext("/api/export", new ExportHandler(analyzer, storageService));

        // New Feature Endpoints
        server.createContext("/api/dictionary", new DictionaryHandler(dictionaryService));
        server.createContext("/api/translate", new TranslateHandler(translationService));
        server.createContext("/api/sessions", new SessionHandler(SessionActivityService.getInstance()));
        server.createContext("/api/database", new DatabaseHandler(databaseManager));
        server.createContext("/api/fonts", new FontHandler());

        // Multi-threaded executor for handling concurrent browser requests
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("================================================================");
        System.out.println("   WORD COUNTER & TEXT ANALYZER - WEB SERVER STARTED            ");
        System.out.println("================================================================");
        System.out.printf("   Frontend URL:  http://localhost:%d/\n", port);
        System.out.printf("   API Base:      http://localhost:%d/api\n", port);
        System.out.println("   Storage Engine: SQLite Database via JDBC (data/textanalyzer.db)");
        System.out.println("   Dictionary API: Free Dictionary API (api.dictionaryapi.dev)");
        System.out.println("   Translator:     English → Hindi / Malayalam (MyMemory / GTX)");
        System.out.println("   Serving Web:   " + webRoot);
        System.out.println("================================================================");
        System.out.println("   Press Ctrl+C to terminate the server.\n");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            System.out.println("Server stopped.");
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        try {
            TextAnalyzerServer server = new TextAnalyzerServer(port, "web");
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
