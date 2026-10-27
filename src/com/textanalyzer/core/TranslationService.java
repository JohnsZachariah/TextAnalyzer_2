package com.textanalyzer.core;

import com.textanalyzer.exception.TextAnalysisException;
import com.textanalyzer.storage.DatabaseManager;
import com.textanalyzer.util.JsonUtil;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service providing English to Hindi/Malayalam translation using Free APIs
 * (MyMemory & Google GTX) with local SQLite caching via JDBC.
 */
public class TranslationService {

    private static volatile TranslationService instance;
    private final HttpClient httpClient;
    private final DatabaseManager databaseManager;
    private final Map<String, String> hindiOffline;
    private final Map<String, String> malayalamOffline;

    public TranslationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        this.databaseManager = DatabaseManager.getInstance();
        this.hindiOffline = new HashMap<>();
        this.malayalamOffline = new HashMap<>();
        initOfflineDictionary();
    }

    public static TranslationService getInstance() {
        if (instance == null) {
            synchronized (TranslationService.class) {
                if (instance == null) {
                    instance = new TranslationService();
                }
            }
        }
        return instance;
    }

    public TranslationResult translateEnglishToHindi(String text) throws TextAnalysisException {
        return translate(text, "hi");
    }

    /**
     * Translates English text to Hindi (hi) or Malayalam (ml).
     */
    public TranslationResult translate(String text, String targetLang) throws TextAnalysisException {
        if (text == null || text.trim().isEmpty()) {
            throw new TextAnalysisException("Text cannot be empty for translation.");
        }

        String lang = normalizeLang(targetLang);
        String trimmed = text.trim();
        String hash = computeHash(trimmed + "|" + lang);

        String cachedTranslation = checkDatabaseCache(hash, lang);
        if (cachedTranslation != null && !cachedTranslation.isEmpty()) {
            return new TranslationResult(trimmed, cachedTranslation, "en", lang, true);
        }

        String translated = translateChunks(trimmed, lang);
        if (translated != null && !translated.isEmpty()) {
            saveToDatabaseCache(hash, trimmed, lang, translated);
            return new TranslationResult(trimmed, translated, "en", lang, false);
        }

        String offlineResult = translateOffline(trimmed, lang);
        if (offlineResult != null && !offlineResult.isEmpty()) {
            return new TranslationResult(trimmed, offlineResult, "en", lang, true);
        }

        String label = "hi".equals(lang) ? "Hindi" : "Malayalam";
        throw new TextAnalysisException("Could not translate text to " + label + ". Please verify your internet connection.");
    }

    public static String normalizeLang(String targetLang) {
        if (targetLang == null) return "hi";
        String v = targetLang.trim().toLowerCase();
        if (v.equals("ml") || v.equals("malayalam") || v.equals("മലയാളം")) {
            return "ml";
        }
        return "hi";
    }

    private String translateChunks(String text, String lang) {
        List<String> chunks = splitForApi(text);
        StringBuilder combined = new StringBuilder();
        boolean any = false;
        for (String chunk : chunks) {
            String piece = translateSingleChunk(chunk, lang);
            if (piece != null && !piece.isEmpty()) {
                if (combined.length() > 0) combined.append(" ");
                combined.append(piece);
                any = true;
            } else if (chunks.size() == 1) {
                return null;
            }
        }
        return any ? combined.toString().trim() : null;
    }

    private String translateSingleChunk(String trimmed, String lang) {
        try {
            String translated = fetchFromMyMemory(trimmed, lang);
            if (translated != null && !translated.isEmpty()) {
                return translated;
            }
        } catch (Exception e) {
            System.err.println("[TranslationService] MyMemory API warning: " + e.getMessage());
        }

        try {
            String translated = fetchFromGoogleGtx(trimmed, lang);
            if (translated != null && !translated.isEmpty()) {
                return translated;
            }
        } catch (Exception e) {
            System.err.println("[TranslationService] Google GTX API warning: " + e.getMessage());
        }
        return null;
    }

    private List<String> splitForApi(String text) {
        List<String> chunks = new ArrayList<>();
        if (text.length() <= 450) {
            chunks.add(text);
            return chunks;
        }
        StringBuilder current = new StringBuilder();
        String[] parts = text.split("(?<=[.!?\\n])\\s+");
        for (String part : parts) {
            if (current.length() + part.length() + 1 > 450 && current.length() > 0) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
            if (current.length() > 0) current.append(" ");
            current.append(part);
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    private String checkDatabaseCache(String hash, String targetLang) {
        if (!databaseManager.isAvailable()) return null;

        String sql = "SELECT translated_text FROM translation_cache WHERE source_hash = ? AND target_lang = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setString(2, targetLang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("translated_text");
                }
            }
        } catch (Exception e) {
            System.err.println("[TranslationService] DB cache check error: " + e.getMessage());
        }
        return null;
    }

    private void saveToDatabaseCache(String hash, String sourceText, String targetLang, String translatedText) {
        if (!databaseManager.isAvailable()) return;

        String sql = "INSERT OR REPLACE INTO translation_cache (source_hash, source_text, target_lang, translated_text, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setString(2, sourceText);
            ps.setString(3, targetLang);
            ps.setString(4, translatedText);
            ps.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[TranslationService] DB cache save error: " + e.getMessage());
        }
    }

    private String fetchFromMyMemory(String text, String lang) throws Exception {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=en%7C" + lang;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .header("User-Agent", "Mozilla/5.0 (TextAnalyzer/2.0; Java/21)")
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            String body = resp.body();
            // Parse "translatedText":"..."
            Pattern pat = Pattern.compile("\"translatedText\"\\s*:\\s*\"(.*?)\"(?:,|\\})");
            Matcher mat = pat.matcher(body);
            if (mat.find()) {
                String raw = mat.group(1);
                String unescaped = unescapeUnicode(raw);
                unescaped = decodeHtmlEntities(unescaped);
                if (!unescaped.trim().isEmpty() && !unescaped.equalsIgnoreCase(text)) {
                    return unescaped;
                }
            }
        }
        return null;
    }

    private String fetchFromGoogleGtx(String text, String lang) throws Exception {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=" + lang + "&dt=t&q=" + encoded;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            String body = resp.body();
            // Google GTX response shape: [[["नमस्ते","Hello",null,null,10]],...]
            StringBuilder result = new StringBuilder();
            Pattern pat = Pattern.compile("\\[\\s*\"([^\"]+)\"\\s*,\\s*\"[^\"]*\"\\s*");
            Matcher mat = pat.matcher(body);
            while (mat.find()) {
                String chunk = mat.group(1);
                chunk = unescapeUnicode(chunk);
                result.append(chunk);
            }
            if (result.length() > 0) {
                return result.toString();
            }
        }
        return null;
    }

    private String translateOffline(String text, String lang) {
        Map<String, String> dict = "ml".equals(lang) ? malayalamOffline : hindiOffline;
        String lower = text.toLowerCase().trim();
        if (dict.containsKey(lower)) {
            return dict.get(lower);
        }

        StringBuilder sb = new StringBuilder();
        String[] words = text.split("\\b");
        boolean matchedAny = false;
        for (String w : words) {
            String lookup = w.toLowerCase().trim();
            if (dict.containsKey(lookup)) {
                sb.append(dict.get(lookup));
                matchedAny = true;
            } else {
                sb.append(w);
            }
        }
        return matchedAny ? sb.toString() : null;
    }

    private String computeHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    private String unescapeUnicode(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        int len = input.length();
        for (int i = 0; i < len; i++) {
            char ch = input.charAt(i);
            if (ch == '\\' && i + 1 < len) {
                char next = input.charAt(i + 1);
                if (next == 'u' && i + 5 < len) {
                    try {
                        String hex = input.substring(i + 2, i + 6);
                        int code = Integer.parseInt(hex, 16);
                        sb.append((char) code);
                        i += 5;
                        continue;
                    } catch (NumberFormatException ignored) {}
                } else if (next == 'n') {
                    sb.append('\n');
                    i++;
                    continue;
                } else if (next == 'r') {
                    i++;
                    continue;
                } else if (next == 't') {
                    sb.append('\t');
                    i++;
                    continue;
                } else if (next == '"') {
                    sb.append('"');
                    i++;
                    continue;
                } else if (next == '\\') {
                    sb.append('\\');
                    i++;
                    continue;
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private String decodeHtmlEntities(String s) {
        if (s == null) return "";
        return s.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'");
    }

    private void initOfflineDictionary() {
        hindiOffline.put("hello", "नमस्ते");
        hindiOffline.put("welcome", "स्वागत है");
        hindiOffline.put("world", "दुनिया");
        hindiOffline.put("computer", "कंप्यूटर");
        hindiOffline.put("program", "प्रोग्राम");
        hindiOffline.put("programming", "प्रोग्रामिंग");
        hindiOffline.put("object oriented programming", "ऑब्जेक्ट ओरिएंटेड प्रोग्रामिंग");
        hindiOffline.put("java", "जावा");
        hindiOffline.put("database", "डेटाबेस");
        hindiOffline.put("text", "पाठ");
        hindiOffline.put("analysis", "विश्लेषण");
        hindiOffline.put("word", "शब्द");
        hindiOffline.put("character", "वर्ण");
        hindiOffline.put("sentence", "वाक्य");
        hindiOffline.put("language", "भाषा");
        hindiOffline.put("english", "अंग्रेज़ी");
        hindiOffline.put("hindi", "हिंदी");
        hindiOffline.put("malayalam", "मलयालम");
        hindiOffline.put("dictionary", "शब्दकोश");
        hindiOffline.put("thank you", "धन्यवाद");
        hindiOffline.put("good morning", "शुभ प्रभात");
        hindiOffline.put("good evening", "शुभ संध्या");
        hindiOffline.put("how are you", "आप कैसे हैं");
        hindiOffline.put("student", "छात्र");
        hindiOffline.put("project", "परियोजना");

        malayalamOffline.put("hello", "ഹലോ");
        malayalamOffline.put("welcome", "സ്വാഗതം");
        malayalamOffline.put("world", "ലോകം");
        malayalamOffline.put("computer", "കമ്പ്യൂട്ടർ");
        malayalamOffline.put("program", "പ്രോഗ്രാം");
        malayalamOffline.put("programming", "പ്രോഗ്രാമിങ്");
        malayalamOffline.put("object oriented programming", "ഒബ്ജക്റ്റ് ഓറിയന്റഡ് പ്രോഗ്രാമിങ്");
        malayalamOffline.put("java", "ജാവ");
        malayalamOffline.put("database", "ഡാറ്റാബേസ്");
        malayalamOffline.put("text", "വാചകം");
        malayalamOffline.put("analysis", "വിശകലനം");
        malayalamOffline.put("word", "വാക്ക്");
        malayalamOffline.put("character", "അക്ഷരം");
        malayalamOffline.put("sentence", "വാക്യം");
        malayalamOffline.put("language", "ഭാഷ");
        malayalamOffline.put("english", "ഇംഗ്ലീഷ്");
        malayalamOffline.put("hindi", "ഹിന്ദി");
        malayalamOffline.put("malayalam", "മലയാളം");
        malayalamOffline.put("dictionary", "നിഘണ്ടു");
        malayalamOffline.put("thank you", "നന്ദി");
        malayalamOffline.put("good morning", "സുപ്രഭാതം");
        malayalamOffline.put("good evening", "ശുഭ സന്ധ്യ");
        malayalamOffline.put("how are you", "സുഖമാണോ");
        malayalamOffline.put("student", "വിദ്യാർത്ഥി");
        malayalamOffline.put("project", "പ്രോജക്റ്റ്");
    }

    public static class TranslationResult {
        private final String sourceText;
        private final String translatedText;
        private final String sourceLang;
        private final String targetLang;
        private final boolean fromCache;

        public TranslationResult(String sourceText, String translatedText, String sourceLang, String targetLang, boolean fromCache) {
            this.sourceText = sourceText;
            this.translatedText = translatedText;
            this.sourceLang = sourceLang;
            this.targetLang = targetLang;
            this.fromCache = fromCache;
        }

        public String getSourceText() { return sourceText; }
        public String getTranslatedText() { return translatedText; }
        public String getSourceLang() { return sourceLang; }
        public String getTargetLang() { return targetLang; }
        public boolean isFromCache() { return fromCache; }

        public String toJson() {
            return String.format(
                    "{\"sourceText\":\"%s\",\"translatedText\":\"%s\",\"sourceLang\":\"%s\",\"targetLang\":\"%s\",\"fromCache\":%b}",
                    JsonUtil.escape(sourceText),
                    JsonUtil.escape(translatedText),
                    JsonUtil.escape(sourceLang),
                    JsonUtil.escape(targetLang),
                    fromCache
            );
        }
    }
}
