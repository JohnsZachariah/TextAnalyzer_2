package com.textanalyzer.cli;

import com.textanalyzer.core.ComprehensiveTextAnalyzer;
import com.textanalyzer.core.DictionaryService;
import com.textanalyzer.core.TranslationService;
import com.textanalyzer.exception.TextAnalysisException;
import com.textanalyzer.model.*;
import com.textanalyzer.pattern.FileSourceAdapter;
import com.textanalyzer.pattern.ReportExporter;
import com.textanalyzer.pattern.ReportExporterFactory;
import com.textanalyzer.server.TextAnalyzerServer;
import com.textanalyzer.storage.DatabaseManager;
import com.textanalyzer.storage.JdbcStorageService;
import com.textanalyzer.storage.StorageService;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Menu-driven console interface satisfying Section 1.2 and 1.3 of the Report Specification:
 * "To develop a menu-driven application using Java and Object-Oriented Programming concepts."
 * Demonstrates:
 * - Switch statements and while loops (OOP Module 1)
 * - Scanner input and console I/O
 * - Exception Handling (OOP Module 3)
 * - Integration with OOP core services, Free Dictionary API, Free Hindi Translator API, and Local SQL JDBC Database
 */
public class TextAnalyzerConsoleApp {

    private final ComprehensiveTextAnalyzer analyzer;
    private final StorageService storageService;
    private final DictionaryService dictionaryService;
    private final TranslationService translationService;
    private final DatabaseManager databaseManager;
    private final Scanner scanner;
    private TextDocument currentDocument;
    private AnalysisResult currentResult;

    public TextAnalyzerConsoleApp() {
        this.analyzer = new ComprehensiveTextAnalyzer();
        this.storageService = new JdbcStorageService();
        this.dictionaryService = DictionaryService.getInstance();
        this.translationService = TranslationService.getInstance();
        this.databaseManager = DatabaseManager.getInstance();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            displayMenu();
            System.out.print("Enter your choice (1-12): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    inputManualText();
                    break;
                case "2":
                    loadFileText();
                    break;
                case "3":
                    performComprehensiveAnalysis();
                    break;
                case "4":
                    searchWord();
                    break;
                case "5":
                    findAndReplace();
                    break;
                case "6":
                    convertCase();
                    break;
                case "7":
                    lookupDictionaryWord();
                    break;
                case "8":
                    translateEnglishToHindi();
                    break;
                case "9":
                    viewDatabaseHistory();
                    break;
                case "10":
                    exportReport();
                    break;
                case "11":
                    launchWebServer();
                    break;
                case "12":
                    running = false;
                    System.out.println("\nThank you for using the Word Counter & Text Analyzer. Goodbye!");
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please select a number between 1 and 12.");
            }
            if (running) {
                System.out.println("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n========================================================");
        System.out.println("       WORD COUNTER & TEXT ANALYZER - MAIN MENU         ");
        System.out.println("========================================================");
        if (currentDocument != null) {
            System.out.printf(" Active Text: [%s] (%d characters loaded)\n",
                    currentDocument.getSourceName(), currentDocument.getLength());
            System.out.println("--------------------------------------------------------");
        }
        System.out.println(" 1. Input Text Directly (Console)");
        System.out.println(" 2. Load Text from External File (Adapter Pattern)");
        System.out.println(" 3. Perform Comprehensive Analysis");
        System.out.println(" 4. Search Word Occurrences");
        System.out.println(" 5. Find and Replace");
        System.out.println(" 6. Case Conversion (Upper, Lower, Title, Sentence)");
        System.out.println(" 7. Look Up Word Meaning (Free Dictionary API + JDBC Cache)");
        System.out.println(" 8. Translate English to Hindi / Malayalam (Free Translator API + JDBC Cache)");
        System.out.println(" 9. View SQL Database History & JDBC Status");
        System.out.println(" 10. Export Analysis Report (.txt or .json)");
        System.out.println(" 11. Launch Web Server (Browser Application)");
        System.out.println(" 12. Exit Application");
        System.out.println("========================================================");
    }

    private void inputManualText() {
        System.out.println("\n--- [1] DIRECT TEXT INPUT ---");
        System.out.println("Enter text (type 'END' on a single line to finish):");
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("END")) {
                break;
            }
            sb.append(line).append(System.lineSeparator());
        }
        String text = sb.toString().trim();
        if (text.isEmpty()) {
            System.out.println("[!] No text was entered.");
            return;
        }
        this.currentDocument = new TextDocument(text, "Console Input");
        this.currentResult = null;
        System.out.println("[✔] Text loaded successfully! (" + currentDocument.getLength() + " characters)");
    }

    private void loadFileText() {
        System.out.println("\n--- [2] LOAD TEXT FROM FILE ---");
        System.out.print("Enter file path: ");
        String path = scanner.nextLine().trim();

        try {
            FileSourceAdapter adapter = new FileSourceAdapter(path);
            this.currentDocument = adapter.loadDocument();
            this.currentResult = null;
            System.out.printf("[✔] Successfully loaded file '%s' (%d characters)!\n",
                    currentDocument.getSourceName(), currentDocument.getLength());
        } catch (TextAnalysisException e) {
            System.out.println("[✘] Error loading file: " + e.getMessage());
        }
    }

    private void performComprehensiveAnalysis() {
        if (!checkDocumentLoaded()) return;

        System.out.println("\n--- [3] COMPREHENSIVE TEXT ANALYSIS ---");
        try {
            currentResult = analyzer.analyze(currentDocument);
            ReportExporter exporter = ReportExporterFactory.getExporter("TEXT");
            System.out.println(exporter.export(currentResult));

            // Auto-persist to SQL database history via JDBC
            String preview = currentDocument.getContent().length() > 60
                    ? currentDocument.getContent().substring(0, 60) + "..."
                    : currentDocument.getContent();
            storageService.saveAnalysisRecord(new HistoryRecord(
                    currentResult.getDocumentId(),
                    currentDocument.getSourceName(),
                    preview,
                    currentResult.getWordCount(),
                    currentResult.getCharCountWithSpaces(),
                    currentResult.getSentenceCount()
            ));
            System.out.println("[✔] Analysis saved to local SQL database via JDBC.");
        } catch (Exception e) {
            System.out.println("[✘] Analysis failed: " + e.getMessage());
        }
    }

    private void searchWord() {
        if (!checkDocumentLoaded()) return;

        System.out.println("\n--- [4] SEARCH WORD ---");
        System.out.print("Enter search word/phrase: ");
        String query = scanner.nextLine().trim();
        System.out.print("Case-sensitive? (y/N): ");
        boolean matchCase = scanner.nextLine().trim().equalsIgnoreCase("y");
        System.out.print("Match whole word only? (y/N): ");
        boolean wholeWord = scanner.nextLine().trim().equalsIgnoreCase("y");

        SearchResult result = analyzer.search(currentDocument.getContent(), query, matchCase, wholeWord);
        System.out.println("\n--------------------------------------------------------");
        System.out.printf(" Query        : '%s'\n", result.getQuery());
        System.out.printf(" Found        : %b\n", result.isFound());
        System.out.printf(" Occurrences  : %d\n", result.getOccurrences());
        if (result.isFound() && !result.getMatchedLines().isEmpty()) {
            System.out.println(" Sample Matches:");
            for (String line : result.getMatchedLines()) {
                System.out.println("   " + line);
            }
        }
        System.out.println("--------------------------------------------------------");
    }

    private void findAndReplace() {
        if (!checkDocumentLoaded()) return;

        System.out.println("\n--- [5] FIND AND REPLACE ---");
        System.out.print("Target text to find: ");
        String target = scanner.nextLine();
        System.out.print("Replacement text: ");
        String replacement = scanner.nextLine();
        System.out.print("Case-sensitive? (y/N): ");
        boolean matchCase = scanner.nextLine().trim().equalsIgnoreCase("y");

        String updated = analyzer.findAndReplace(currentDocument.getContent(), target, replacement, matchCase);
        this.currentDocument = new TextDocument(updated, currentDocument.getSourceName() + " (Edited)");
        this.currentResult = null;

        System.out.println("[✔] Replacement applied! Updated text preview:");
        System.out.println("--------------------------------------------------------");
        String preview = updated.length() > 200 ? updated.substring(0, 200) + "...\n[truncated]" : updated;
        System.out.println(preview);
        System.out.println("--------------------------------------------------------");
    }

    private void convertCase() {
        if (!checkDocumentLoaded()) return;

        System.out.println("\n--- [6] CASE CONVERSION ---");
        System.out.println(" 1. UPPERCASE");
        System.out.println(" 2. lowercase");
        System.out.println(" 3. Title Case");
        System.out.println(" 4. Sentence case");
        System.out.print("Select format (1-4): ");
        String choice = scanner.nextLine().trim();

        CaseType caseType;
        switch (choice) {
            case "1": caseType = CaseType.UPPERCASE; break;
            case "2": caseType = CaseType.LOWERCASE; break;
            case "3": caseType = CaseType.TITLE_CASE; break;
            case "4": caseType = CaseType.SENTENCE_CASE; break;
            default:
                System.out.println("[!] Invalid choice.");
                return;
        }

        String converted = analyzer.convertCase(currentDocument.getContent(), caseType);
        this.currentDocument = new TextDocument(converted, currentDocument.getSourceName() + " (" + caseType.name() + ")");
        this.currentResult = null;

        System.out.println("[✔] Case converted successfully! Preview:");
        System.out.println("--------------------------------------------------------");
        String preview = converted.length() > 200 ? converted.substring(0, 200) + "...\n[truncated]" : converted;
        System.out.println(preview);
        System.out.println("--------------------------------------------------------");
    }

    private void lookupDictionaryWord() {
        System.out.println("\n--- [7] DICTIONARY WORD LOOKUP (FREE DICTIONARY API) ---");
        System.out.print("Enter word to look up: ");
        String word = scanner.nextLine().trim();
        if (word.isEmpty()) {
            System.out.println("[!] No word entered.");
            return;
        }

        try {
            DictionaryEntry entry = dictionaryService.lookupWord(word);
            System.out.println("\n========================================================");
            System.out.printf(" Word: %s   %s\n", entry.getWord().toUpperCase(), entry.getPhonetic());
            System.out.printf(" Source: %s\n", entry.isFromCache() ? "Local SQLite Database (JDBC Cached)" : "Live Dictionary API");
            System.out.println("--------------------------------------------------------");

            for (DictionaryEntry.Meaning m : entry.getMeanings()) {
                System.out.printf(" [%s]\n", m.getPartOfSpeech().toUpperCase());
                for (int i = 0; i < m.getDefinitions().size(); i++) {
                    DictionaryEntry.DefinitionItem def = m.getDefinitions().get(i);
                    System.out.printf("   %d. %s\n", (i + 1), def.getDefinition());
                    if (!def.getExample().isEmpty()) {
                        System.out.printf("      Example: \"%s\"\n", def.getExample());
                    }
                }
            }
            System.out.println("========================================================");
        } catch (Exception e) {
            System.out.println("[✘] Dictionary lookup failed: " + e.getMessage());
        }
    }

    private void translateEnglishToHindi() {
        System.out.println("\n--- [8] ENGLISH TO HINDI / MALAYALAM TRANSLATION ---");
        System.out.print("Target language (1 = Hindi, 2 = Malayalam) [1]: ");
        String langChoice = scanner.nextLine().trim();
        String targetLang = "2".equals(langChoice) ? "ml" : "hi";
        String langLabel = "ml".equals(targetLang) ? "Malayalam (മലയാളം)" : "Hindi (हिंदी)";

        String textToTranslate = null;
        if (currentDocument != null && !currentDocument.isEmpty()) {
            System.out.print("Translate currently loaded text? (Y/n): ");
            String ans = scanner.nextLine().trim();
            if (!ans.equalsIgnoreCase("n")) {
                textToTranslate = currentDocument.getContent();
            }
        }

        if (textToTranslate == null) {
            System.out.println("Enter English text to translate:");
            textToTranslate = scanner.nextLine().trim();
        }

        if (textToTranslate.isEmpty()) {
            System.out.println("[!] No text provided.");
            return;
        }

        try {
            System.out.println("Translating to " + langLabel + "...");
            TranslationService.TranslationResult res = translationService.translate(textToTranslate, targetLang);
            System.out.println("\n========================================================");
            System.out.println(" ENGLISH SOURCE:");
            System.out.println(res.getSourceText());
            System.out.println("--------------------------------------------------------");
            System.out.println(" TRANSLATION (" + langLabel + "):");
            System.out.println(res.getTranslatedText());
            System.out.printf(" Source: %s\n", res.isFromCache() ? "Local SQL Database Cache (JDBC)" : "Free Translator API");
            System.out.println("========================================================");
        } catch (Exception e) {
            System.out.println("[✘] Translation failed: " + e.getMessage());
        }
    }

    private void viewDatabaseHistory() {
        System.out.println("\n--- [9] SQL DATABASE & PERSISTENT HISTORY ---");
        Map<String, Object> stats = databaseManager.getDatabaseStats();
        System.out.printf(" Storage Backend: %s\n", stats.get("databaseType"));
        System.out.printf(" JDBC Driver:     %s (Available: %s)\n", stats.get("jdbcUrl"), stats.get("driverAvailable"));
        System.out.printf(" Total Analyses:  %s records\n", stats.get("analysesCount"));
        System.out.printf(" Cached Words:    %s words\n", stats.get("cachedWordsCount"));
        System.out.printf(" Cached Trans:    %s translations\n", stats.get("cachedTranslationsCount"));
        System.out.println("--------------------------------------------------------");

        try {
            List<HistoryRecord> history = storageService.loadHistory();
            if (history.isEmpty()) {
                System.out.println("No saved analysis records found in SQL database.");
                return;
            }
            System.out.println("Recent SQL Records (Top " + history.size() + "):\n");
            for (int i = 0; i < history.size(); i++) {
                HistoryRecord h = history.get(i);
                System.out.printf(" [%2d] ID: %s | Saved: %s | Source: %s\n", (i + 1), h.getId(), h.getSavedAt(), h.getTitle());
                System.out.printf("      Words: %d | Chars: %d | Sentences: %d\n",
                        h.getWordCount(), h.getCharCount(), h.getSentenceCount());
                System.out.printf("      Preview: %s\n\n", h.getPreview());
            }
        } catch (Exception e) {
            System.out.println("[✘] Error loading SQL records: " + e.getMessage());
        }
    }

    private void exportReport() {
        if (!checkDocumentLoaded()) return;

        if (currentResult == null) {
            try {
                currentResult = analyzer.analyze(currentDocument);
            } catch (TextAnalysisException e) {
                System.out.println("[✘] Failed to generate analysis for export: " + e.getMessage());
                return;
            }
        }

        System.out.println("\n--- [10] EXPORT REPORT ---");
        System.out.print("Select format (TEXT / JSON) [default: TEXT]: ");
        String format = scanner.nextLine().trim();
        if (format.isEmpty()) format = "TEXT";

        try {
            File saved = storageService.saveReportFile(currentResult, format);
            System.out.println("[✔] Report successfully exported and saved to disk:");
            System.out.println("    Path: " + saved.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("[✘] Error exporting report: " + e.getMessage());
        }
    }

    private void launchWebServer() {
        System.out.println("\n--- [11] LAUNCH WEB SERVER ---");
        System.out.print("Enter server port [default: 8080]: ");
        String portStr = scanner.nextLine().trim();
        int port = 8080;
        if (!portStr.isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException ignored) {}
        }

        try {
            TextAnalyzerServer server = new TextAnalyzerServer(port, "web");
            server.start();
        } catch (Exception e) {
            System.out.println("[✘] Could not start server: " + e.getMessage());
        }
    }

    private boolean checkDocumentLoaded() {
        if (currentDocument == null || currentDocument.isEmpty()) {
            System.out.println("[!] No text loaded. Please select Option 1 or Option 2 first.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        new TextAnalyzerConsoleApp().start();
    }
}
