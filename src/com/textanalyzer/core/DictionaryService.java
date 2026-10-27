package com.textanalyzer.core;

import com.textanalyzer.exception.TextAnalysisException;
import com.textanalyzer.model.DictionaryEntry;
import com.textanalyzer.model.DictionaryEntry.Meaning;
import com.textanalyzer.storage.DatabaseManager;
import com.textanalyzer.util.JsonUtil;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service providing dictionary word definitions using Free Dictionary API (api.dictionaryapi.dev)
 * with local SQLite database caching via JDBC and offline fallback resilience.
 * Fulfills:
 * - Integration of Free Dictionary API
 * - Local Database (SQL & JDBC) Caching
 * - SOLID Single Responsibility & Exception Handling
 */
public class DictionaryService {

    private static volatile DictionaryService instance;
    private final HttpClient httpClient;
    private final DatabaseManager databaseManager;
    private final Map<String, DictionaryEntry> fallbackDictionary;

    public DictionaryService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        this.databaseManager = DatabaseManager.getInstance();
        this.fallbackDictionary = new HashMap<>();
        initFallbackDictionary();
    }

    public static DictionaryService getInstance() {
        if (instance == null) {
            synchronized (DictionaryService.class) {
                if (instance == null) {
                    instance = new DictionaryService();
                }
            }
        }
        return instance;
    }

    /**
     * Looks up definition for a word. Checks SQL DB cache first, then API, then fallback.
     */
    public DictionaryEntry lookupWord(String rawWord) throws TextAnalysisException {
        if (rawWord == null || rawWord.trim().isEmpty()) {
            throw new TextAnalysisException("Word cannot be empty for dictionary lookup.");
        }

        String word = rawWord.trim().toLowerCase().replaceAll("[^a-zA-Z-]", "");
        if (word.isEmpty()) {
            throw new TextAnalysisException("Invalid word format: '" + rawWord + "'");
        }

        // 1. Check Local SQL Database cache via JDBC
        DictionaryEntry cached = checkDatabaseCache(word);
        if (cached != null) {
            cached.setFromCache(true);
            return cached;
        }

        // 2. Fetch from Free Dictionary API
        try {
            DictionaryEntry entry = fetchFromApi(word);
            if (entry != null) {
                // Save to local SQL DB cache via JDBC
                saveToDatabaseCache(entry);
                return entry;
            }
        } catch (Exception e) {
            System.err.println("[DictionaryService] API fetch warning for '" + word + "': " + e.getMessage());
        }

        // 3. Fallback to offline dictionary if network fails or word is in fallback
        if (fallbackDictionary.containsKey(word)) {
            DictionaryEntry fb = fallbackDictionary.get(word);
            fb.setFromCache(true);
            return fb;
        }

        throw new TextAnalysisException("No definition found for word: \"" + word + "\". Please check spelling or verify internet connectivity.");
    }

    private DictionaryEntry checkDatabaseCache(String word) {
        if (!databaseManager.isAvailable()) return null;

        String sql = "SELECT phonetic, definition_json FROM dictionary_cache WHERE word = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, word);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String definitionJson = rs.getString("definition_json");
                    DictionaryEntry entry = parseStoredJson(definitionJson);
                    if (entry != null) {
                        return entry;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DictionaryService] DB cache check error: " + e.getMessage());
        }
        return null;
    }

    private void saveToDatabaseCache(DictionaryEntry entry) {
        if (!databaseManager.isAvailable() || entry == null) return;

        String sql = "INSERT OR REPLACE INTO dictionary_cache (word, phonetic, definition_json, cached_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entry.getWord().toLowerCase());
            ps.setString(2, entry.getPhonetic());
            ps.setString(3, entry.toJson());
            ps.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[DictionaryService] DB cache save error: " + e.getMessage());
        }
    }

    private DictionaryEntry fetchFromApi(String word) throws Exception {
        String encoded = URLEncoder.encode(word, StandardCharsets.UTF_8);
        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + encoded;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .header("User-Agent", "Mozilla/5.0 (TextAnalyzer/2.0; Java/21)")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return parseApiResponse(word, response.body());
        }
        return null;
    }

    private DictionaryEntry parseApiResponse(String fallbackWord, String json) {
        try {
            // Extract phonetic
            String phonetic = "";
            Matcher pMat = Pattern.compile("\"phonetic\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (pMat.find()) {
                phonetic = pMat.group(1);
            }

            // Extract audio
            String audioUrl = "";
            Matcher aMat = Pattern.compile("\"audio\"\\s*:\\s*\"(https?://[^\"]+\\.mp3)\"").matcher(json);
            if (aMat.find()) {
                audioUrl = aMat.group(1);
            }

            DictionaryEntry entry = new DictionaryEntry(fallbackWord, phonetic, audioUrl);

            // Extract meanings blocks
            Pattern meaningBlockPat = Pattern.compile("\\{\"partOfSpeech\"\\s*:\\s*\"([^\"]+)\"[^}]*\"definitions\"\\s*:\\s*\\[(.*?)\\]\\s*\\}", Pattern.DOTALL);
            Matcher mMat = meaningBlockPat.matcher(json);

            while (mMat.find()) {
                String pos = mMat.group(1);
                String defsChunk = mMat.group(2);
                Meaning meaning = new Meaning(pos);

                // Extract each definition & example
                Pattern defItemPat = Pattern.compile("\"definition\"\\s*:\\s*\"([^\"]+)\"(?:.*?\"example\"\\s*:\\s*\"([^\"]+)\")?", Pattern.DOTALL);
                Matcher dMat = defItemPat.matcher(defsChunk);

                while (dMat.find()) {
                    String def = dMat.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
                    String ex = dMat.group(2) != null ? dMat.group(2).replace("\\\"", "\"").replace("\\\\", "\\") : "";
                    meaning.addDefinition(def, ex);
                }

                if (!meaning.getDefinitions().isEmpty()) {
                    entry.addMeaning(meaning);
                }
            }

            // If regex block parser didn't catch definitions, do loose fallback extraction
            if (entry.getMeanings().isEmpty()) {
                Meaning generalMeaning = new Meaning("general");
                Matcher dMat = Pattern.compile("\"definition\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
                int count = 0;
                while (dMat.find() && count < 3) {
                    generalMeaning.addDefinition(dMat.group(1).replace("\\\"", "\""), "");
                    count++;
                }
                if (!generalMeaning.getDefinitions().isEmpty()) {
                    entry.addMeaning(generalMeaning);
                }
            }

            return entry;
        } catch (Exception e) {
            System.err.println("[DictionaryService] Parse error: " + e.getMessage());
            return null;
        }
    }

    private DictionaryEntry parseStoredJson(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            Matcher wMat = Pattern.compile("\"word\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            String word = wMat.find() ? wMat.group(1) : "";

            Matcher pMat = Pattern.compile("\"phonetic\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            String phonetic = pMat.find() ? pMat.group(1) : "";

            Matcher aMat = Pattern.compile("\"audioUrl\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            String audioUrl = aMat.find() ? aMat.group(1) : "";

            DictionaryEntry entry = new DictionaryEntry(word, phonetic, audioUrl);

            Pattern meaningBlockPat = Pattern.compile("\\{\"partOfSpeech\"\\s*:\\s*\"([^\"]+)\"[^}]*\"definitions\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
            Matcher mMat = meaningBlockPat.matcher(json);
            while (mMat.find()) {
                String pos = mMat.group(1);
                String defsChunk = mMat.group(2);
                Meaning meaning = new Meaning(pos);

                Pattern defItemPat = Pattern.compile("\\{\"definition\"\\s*:\\s*\"([^\"]+)\",\"example\"\\s*:\\s*\"([^\"]*)\"\\}");
                Matcher dMat = defItemPat.matcher(defsChunk);
                while (dMat.find()) {
                    meaning.addDefinition(dMat.group(1), dMat.group(2));
                }
                if (!meaning.getDefinitions().isEmpty()) {
                    entry.addMeaning(meaning);
                }
            }
            return entry;
        } catch (Exception e) {
            return null;
        }
    }

    private void initFallbackDictionary() {
        addFallback("algorithm", "/ˈæl.ɡə.rɪð.əm/", "noun",
                "A finite sequence of rigorous, unambiguous instructions to solve a class of specific problems or perform computation.",
                "Dijkstra proposed an efficient algorithm for shortest paths.");

        addFallback("polymorphism", "/ˌpɒl.iˈmɔː.fɪ.zəm/", "noun",
                "The provision of a single interface to entities of different types, or the ability of an object to take on many forms.",
                "Method overriding is a classic demonstration of runtime dynamic polymorphism.");

        addFallback("encapsulation", "/ɪnˌkæp.sjʊˈleɪ.ʃən/", "noun",
                "The bundling of data with the methods that operate on that data, restricting direct access to internal object components.",
                "Java enforces encapsulation through private variables and public getters and setters.");

        addFallback("inheritance", "/ɪnˈher.ɪ.təns/", "noun",
                "A mechanism wherein a new class is derived from an existing class, adopting its fields and behaviors.",
                "ComprehensiveTextAnalyzer utilizes inheritance by extending AbstractTextAnalyzer.");

        addFallback("abstraction", "/æbˈstræk.ʃən/", "noun",
                "The process of removing physical, spatial, or temporal details in the study of objects or systems to focus attention on essential details.",
                "Interfaces provide pure abstraction without implementation details.");

        addFallback("database", "/ˈdeɪ.tə.beɪs/", "noun",
                "An organized collection of structured information or data typically stored electronically in a computer system.",
                "We use a local SQLite database connected via JDBC.");

        addFallback("computer", "/kəmˈpjuː.tər/", "noun",
                "An electronic device for storing and processing data, typically in binary form, according to variable program instructions.",
                "Modern computers execute billions of instructions per second.");

        addFallback("java", "/ˈdʒɑː.və/", "noun",
                "A high-level, class-based, object-oriented programming language designed to have as few implementation dependencies as possible.",
                "Java 21 LTS powers the high-performance backend of this application.");

        addFallback("dictionary", "/ˈdɪk.ʃən.ər.i/", "noun",
                "A book or electronic resource that lists the words of a language and gives their meaning or equivalent words in another language.",
                "This web application features an embedded dictionary API.");

        addFallback("translation", "/trænsˈleɪ.ʃən/", "noun",
                "The process of translating words or text from one language into another.",
                "The tool provides English to Hindi translation.");

        addFallback("lexicon", "/ˈlek.sɪ.kən/", "noun",
                "The vocabulary of a person, language, or branch of knowledge.",
                "The LexiCore engine evaluates the text lexicon.");
    }

    private void addFallback(String word, String phonetic, String pos, String def, String ex) {
        DictionaryEntry entry = new DictionaryEntry(word, phonetic, "");
        Meaning m = new Meaning(pos);
        m.addDefinition(def, ex);
        entry.addMeaning(m);
        fallbackDictionary.put(word, entry);
    }
}
