package com.textanalyzer;

import com.textanalyzer.cli.TextAnalyzerConsoleApp;
import com.textanalyzer.server.TextAnalyzerServer;
import java.io.IOException;

/**
 * Main application launcher for the Word Counter & Text Analyzer system.
 * Allows running either:
 * 1. Web Application Server (default or --web)
 * 2. Menu-Driven Console Application (--cli or --console)
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && ("--cli".equalsIgnoreCase(args[0]) || "--console".equalsIgnoreCase(args[0]) || "-c".equalsIgnoreCase(args[0]))) {
            System.out.println("Starting Console Application Mode...");
            TextAnalyzerConsoleApp.main(new String[0]);
            return;
        }

        int port = 8080;
        for (int i = 0; i < args.length; i++) {
            if ("--port".equalsIgnoreCase(args[i]) || "-p".equalsIgnoreCase(args[i])) {
                if (i + 1 < args.length) {
                    try {
                        port = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        try {
            System.out.println("Starting Word Counter & Text Analyzer Web Application...");
            TextAnalyzerServer server = new TextAnalyzerServer(port, "web");
            server.start();
        } catch (IOException e) {
            System.err.println("Fatal: Failed to start Text Analyzer server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
