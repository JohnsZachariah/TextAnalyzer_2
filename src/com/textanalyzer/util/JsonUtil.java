package com.textanalyzer.util;

import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.HistoryRecord;
import com.textanalyzer.model.SearchResult;
import com.textanalyzer.model.WordFrequency;
import java.util.*;

/**
 * Lightweight, zero-dependency JSON utility for serialization and deserialization.
 * Supports converting Model objects to JSON and parsing basic JSON request maps.
 * Demonstrates Collections, String parsing, and defensive data transformation.
 */
public final class JsonUtil {

    private JsonUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Escapes special characters for JSON string output.
     */
    public static String escape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Serializes an AnalysisResult object to a JSON string.
     */
    public static String toJson(AnalysisResult res) {
        if (res == null) return "null";

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"documentId\":\"").append(escape(res.getDocumentId())).append("\",");
        sb.append("\"sourceName\":\"").append(escape(res.getSourceName())).append("\",");
        sb.append("\"timestamp\":\"").append(escape(res.getTimestamp())).append("\",");
        sb.append("\"wordCount\":").append(res.getWordCount()).append(",");
        sb.append("\"charCountWithSpaces\":").append(res.getCharCountWithSpaces()).append(",");
        sb.append("\"charCountWithoutSpaces\":").append(res.getCharCountWithoutSpaces()).append(",");
        sb.append("\"letterCount\":").append(res.getLetterCount()).append(",");
        sb.append("\"digitCount\":").append(res.getDigitCount()).append(",");
        sb.append("\"specialCharCount\":").append(res.getSpecialCharCount()).append(",");
        sb.append("\"sentenceCount\":").append(res.getSentenceCount()).append(",");
        sb.append("\"paragraphCount\":").append(res.getParagraphCount()).append(",");
        sb.append("\"totalVowels\":").append(res.getTotalVowels()).append(",");
        sb.append("\"totalConsonants\":").append(res.getTotalConsonants()).append(",");

        // Vowel breakdown map
        sb.append("\"vowelBreakdown\":{");
        int vi = 0;
        for (Map.Entry<Character, Integer> entry : res.getVowelBreakdown().entrySet()) {
            if (vi++ > 0) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
        }
        sb.append("},");

        // Longest words
        sb.append("\"longestWordLength\":").append(res.getLongestWordLength()).append(",");
        sb.append("\"longestWords\":[");
        for (int i = 0; i < res.getLongestWords().size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(res.getLongestWords().get(i))).append("\"");
        }
        sb.append("],");

        // Shortest words
        sb.append("\"shortestWordLength\":").append(res.getShortestWordLength()).append(",");
        sb.append("\"shortestWords\":[");
        for (int i = 0; i < res.getShortestWords().size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(res.getShortestWords().get(i))).append("\"");
        }
        sb.append("],");

        // Word frequencies
        sb.append("\"uniqueWordCount\":").append(res.getUniqueWordCount()).append(",");
        sb.append("\"wordFrequencies\":[");
        for (int i = 0; i < res.getWordFrequencies().size(); i++) {
            if (i > 0) sb.append(",");
            WordFrequency wf = res.getWordFrequencies().get(i);
            sb.append("{\"word\":\"").append(escape(wf.getWord())).append("\",\"count\":").append(wf.getCount())
              .append(",\"percentage\":").append(String.format(Locale.US, "%.2f", wf.getPercentage())).append("}");
        }
        sb.append("],");

        sb.append("\"averageWordLength\":").append(String.format(Locale.US, "%.2f", res.getAverageWordLength())).append(",");
        sb.append("\"readingTimeMinutes\":").append(String.format(Locale.US, "%.2f", res.getReadingTimeMinutes())).append(",");
        sb.append("\"fleschReadingEase\":").append(String.format(Locale.US, "%.1f", res.getFleschReadingEase())).append(",");
        sb.append("\"readabilityLevel\":\"").append(escape(res.getReadabilityLevel())).append("\"");

        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializes a SearchResult object to JSON.
     */
    public static String toJson(SearchResult sr) {
        if (sr == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"query\":\"").append(escape(sr.getQuery())).append("\",");
        sb.append("\"found\":").append(sr.isFound()).append(",");
        sb.append("\"occurrences\":").append(sr.getOccurrences()).append(",");

        sb.append("\"matchIndices\":[");
        for (int i = 0; i < sr.getMatchIndices().size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(sr.getMatchIndices().get(i));
        }
        sb.append("],");

        sb.append("\"matchedLines\":[");
        for (int i = 0; i < sr.getMatchedLines().size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(sr.getMatchedLines().get(i))).append("\"");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializes History records to JSON list.
     */
    public static String historyToJson(List<HistoryRecord> records) {
        StringBuilder sb = new StringBuilder("[");
        if (records != null) {
            for (int i = 0; i < records.size(); i++) {
                if (i > 0) sb.append(",");
                HistoryRecord rec = records.get(i);
                sb.append("{\"id\":\"").append(escape(rec.getId())).append("\",")
                  .append("\"title\":\"").append(escape(rec.getTitle())).append("\",")
                  .append("\"preview\":\"").append(escape(rec.getPreview())).append("\",")
                  .append("\"savedAt\":\"").append(escape(rec.getSavedAt())).append("\",")
                  .append("\"wordCount\":").append(rec.getWordCount()).append(",")
                  .append("\"charCount\":").append(rec.getCharCount()).append(",")
                  .append("\"sentenceCount\":").append(rec.getSentenceCount()).append("}");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parses a flat/simple JSON string into a Map of key-value pairs (String -> Object).
     */
    public static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return map;
        }

        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        int len = trimmed.length();
        int i = 0;

        while (i < len) {
            // Find key
            while (i < len && (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == ',')) i++;
            if (i >= len) break;

            if (trimmed.charAt(i) != '"') {
                i++;
                continue;
            }
            i++; // skip open quote
            StringBuilder keySb = new StringBuilder();
            while (i < len && trimmed.charAt(i) != '"') {
                if (trimmed.charAt(i) == '\\' && i + 1 < len) i++;
                keySb.append(trimmed.charAt(i));
                i++;
            }
            if (i < len && trimmed.charAt(i) == '"') i++; // skip close quote

            // Skip to colon
            while (i < len && trimmed.charAt(i) != ':') i++;
            if (i < len && trimmed.charAt(i) == ':') i++;

            // Skip whitespace
            while (i < len && Character.isWhitespace(trimmed.charAt(i))) i++;
            if (i >= len) break;

            // Read value
            char firstChar = trimmed.charAt(i);
            if (firstChar == '"') {
                // String value
                i++;
                StringBuilder valSb = new StringBuilder();
                while (i < len) {
                    char c = trimmed.charAt(i);
                    if (c == '\\' && i + 1 < len) {
                        char next = trimmed.charAt(i + 1);
                        if (next == '"' || next == '\\' || next == '/') valSb.append(next);
                        else if (next == 'n') valSb.append('\n');
                        else if (next == 'r') valSb.append('\r');
                        else if (next == 't') valSb.append('\t');
                        else valSb.append(next);
                        i += 2;
                        continue;
                    }
                    if (c == '"') {
                        i++;
                        break;
                    }
                    valSb.append(c);
                    i++;
                }
                map.put(keySb.toString(), valSb.toString());
            } else if (firstChar == '{') {
                // Nested object - find matching brace
                int braceDepth = 1;
                int start = i;
                i++;
                while (i < len && braceDepth > 0) {
                    if (trimmed.charAt(i) == '{') braceDepth++;
                    else if (trimmed.charAt(i) == '}') braceDepth--;
                    i++;
                }
                map.put(keySb.toString(), trimmed.substring(start, i));
            } else if (firstChar == '[') {
                // Array - find matching bracket
                int bracketDepth = 1;
                int start = i;
                i++;
                while (i < len && bracketDepth > 0) {
                    if (trimmed.charAt(i) == '[') bracketDepth++;
                    else if (trimmed.charAt(i) == ']') bracketDepth--;
                    i++;
                }
                map.put(keySb.toString(), trimmed.substring(start, i));
            } else {
                // Primitive (number or boolean)
                StringBuilder valSb = new StringBuilder();
                while (i < len && trimmed.charAt(i) != ',' && trimmed.charAt(i) != '}') {
                    valSb.append(trimmed.charAt(i));
                    i++;
                }
                String valStr = valSb.toString().trim();
                if ("true".equalsIgnoreCase(valStr)) map.put(keySb.toString(), Boolean.TRUE);
                else if ("false".equalsIgnoreCase(valStr)) map.put(keySb.toString(), Boolean.FALSE);
                else {
                    try {
                        if (valStr.contains(".")) map.put(keySb.toString(), Double.parseDouble(valStr));
                        else map.put(keySb.toString(), Long.parseLong(valStr));
                    } catch (NumberFormatException e) {
                        map.put(keySb.toString(), valStr);
                    }
                }
            }
        }
        return map;
    }
}
