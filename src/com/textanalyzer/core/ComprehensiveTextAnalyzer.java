package com.textanalyzer.core;

import com.textanalyzer.exception.TextAnalysisException;
import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.CaseType;
import com.textanalyzer.model.SearchResult;
import com.textanalyzer.model.TextDocument;
import com.textanalyzer.model.WordFrequency;
import com.textanalyzer.util.TextUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Concrete implementation of AbstractTextAnalyzer, implementing Searchable and Transformable.
 * Demonstrates:
 * - Inheritance (extends AbstractTextAnalyzer) with super constructor call
 * - Multiple Interface Implementation (implements TextAnalyzer, Searchable, Transformable)
 * - Method Overriding and Dynamic Method Dispatch
 * - Method Overloading
 * - Java Collections Framework (ArrayList, HashMap, Map, Collections.sort)
 * - String and StringBuilder manipulation
 * - Exception Handling (throw, throws, try-catch)
 * - Clean Object-Oriented Architecture
 */
public class ComprehensiveTextAnalyzer extends AbstractTextAnalyzer implements Searchable, Transformable {

    private static final int DEFAULT_READING_WPM = 200;

    /**
     * Default constructor calling superclass constructor.
     */
    public ComprehensiveTextAnalyzer() {
        super("Comprehensive Text Analyzer & Word Counter", "2.1.0");
    }

    /**
     * Parameterized constructor demonstrating constructor overloading and 'super'.
     */
    public ComprehensiveTextAnalyzer(String customName) {
        super(customName, "2.1.0");
    }

    /**
     * Primary analysis method implementing dynamic polymorphism.
     * Demonstrates dynamic method dispatch and exception handling.
     */
    @Override
    public AnalysisResult analyze(TextDocument document) throws TextAnalysisException {
        // Validate input using protected superclass method
        validateDocument(document);

        String text = document.getContent();
        if (text == null) {
            throw new TextAnalysisException("Document content cannot be null", "NULL_CONTENT");
        }

        try {
            // Track processed characters in superclass protected field
            this.totalProcessedCharacters += text.length();

            // 1. Character Analysis (Counts: with spaces, without spaces, letters, digits, special characters)
            int charCountWithSpaces = text.length();
            int charCountWithoutSpaces = 0;
            int letterCount = 0;
            int digitCount = 0;
            int specialCharCount = 0;
            int totalVowels = 0;
            int totalConsonants = 0;

            Map<Character, Integer> vowelBreakdown = new TreeMap<>();
            vowelBreakdown.put('a', 0);
            vowelBreakdown.put('e', 0);
            vowelBreakdown.put('i', 0);
            vowelBreakdown.put('o', 0);
            vowelBreakdown.put('u', 0);

            // Character-by-character analysis (Chapter 2.1.3 & 2.1.5)
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);

                if (!Character.isWhitespace(ch)) {
                    charCountWithoutSpaces++;
                }

                if (Character.isLetter(ch)) {
                    letterCount++;
                    char lower = Character.toLowerCase(ch);
                    if (TextUtils.isVowel(lower)) {
                        totalVowels++;
                        vowelBreakdown.put(lower, vowelBreakdown.getOrDefault(lower, 0) + 1);
                    } else {
                        totalConsonants++;
                    }
                } else if (Character.isDigit(ch)) {
                    digitCount++;
                } else if (!Character.isWhitespace(ch)) {
                    specialCharCount++;
                }
            }

            // 2. Word Tokenization & Frequency Analysis (Chapter 2.1.2 & 2.1.7)
            List<String> rawWords = TextUtils.extractWords(text);
            int wordCount = rawWords.size();
            Map<String, Integer> frequencyMap = new HashMap<>();
            int totalWordCharacters = 0;
            int totalSyllables = 0;

            for (String w : rawWords) {
                String normalized = w.toLowerCase();
                frequencyMap.put(normalized, frequencyMap.getOrDefault(normalized, 0) + 1);
                totalWordCharacters += w.length();
                totalSyllables += TextUtils.estimateSyllables(w);
            }

            // Convert frequency map to sorted WordFrequency objects (Collections usage)
            List<WordFrequency> wordFrequencies = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
                double pct = wordCount > 0 ? (entry.getValue() * 100.0) / wordCount : 0.0;
                wordFrequencies.add(new WordFrequency(entry.getKey(), entry.getValue(), pct));
            }
            Collections.sort(wordFrequencies); // Uses WordFrequency.compareTo

            // 3. Longest and Shortest Words (Chapter 2.1.6)
            List<String> longestWords = new ArrayList<>();
            int longestWordLength = 0;
            List<String> shortestWords = new ArrayList<>();
            int shortestWordLength = Integer.MAX_VALUE;

            if (!rawWords.isEmpty()) {
                // Find max and min lengths
                for (String w : rawWords) {
                    int len = w.length();
                    if (len > longestWordLength) {
                        longestWordLength = len;
                    }
                    if (len < shortestWordLength) {
                        shortestWordLength = len;
                    }
                }

                // Collect unique words matching those lengths
                Set<String> longestSet = new LinkedHashSet<>();
                Set<String> shortestSet = new LinkedHashSet<>();
                for (String w : rawWords) {
                    if (w.length() == longestWordLength) {
                        longestSet.add(w.toLowerCase());
                    }
                    if (w.length() == shortestWordLength) {
                        shortestSet.add(w.toLowerCase());
                    }
                }
                longestWords.addAll(longestSet);
                shortestWords.addAll(shortestSet);
            } else {
                shortestWordLength = 0;
            }

            // 4. Sentence and Paragraph Counts (Chapter 2.1.4)
            int sentenceCount = TextUtils.countSentences(text);
            int paragraphCount = TextUtils.countParagraphs(text);

            // 5. Additional Metrics (Average length, Reading time, Readability)
            double averageWordLength = wordCount > 0 ? (double) totalWordCharacters / wordCount : 0.0;
            double readingTimeMinutes = wordCount > 0 ? (double) wordCount / DEFAULT_READING_WPM : 0.0;
            double fleschScore = TextUtils.calculateFleschReadingEase(wordCount, Math.max(1, sentenceCount), totalSyllables);
            String readabilityLevel = TextUtils.getReadabilityDescription(fleschScore);

            // 6. Construct Immutable Result via Builder
            return new AnalysisResult.Builder()
                    .documentId(document.getId())
                    .sourceName(document.getSourceName())
                    .timestamp(document.getFormattedTimestamp())
                    .wordCount(wordCount)
                    .charCountWithSpaces(charCountWithSpaces)
                    .charCountWithoutSpaces(charCountWithoutSpaces)
                    .letterCount(letterCount)
                    .digitCount(digitCount)
                    .specialCharCount(specialCharCount)
                    .sentenceCount(sentenceCount)
                    .paragraphCount(paragraphCount)
                    .totalVowels(totalVowels)
                    .totalConsonants(totalConsonants)
                    .vowelBreakdown(vowelBreakdown)
                    .longestWords(longestWords, longestWordLength)
                    .shortestWords(shortestWords, shortestWordLength)
                    .wordFrequencies(wordFrequencies)
                    .uniqueWordCount(frequencyMap.size())
                    .averageWordLength(averageWordLength)
                    .readingTimeMinutes(readingTimeMinutes)
                    .fleschReadingEase(fleschScore)
                    .readabilityLevel(readabilityLevel)
                    .build();

        } catch (Exception ex) {
            throw new TextAnalysisException("Failed to analyze text: " + ex.getMessage(), "PROCESSING_ERROR", ex);
        }
    }

    // =========================================================================
    // Searchable Interface Implementations (Chapter 2.1.8)
    // =========================================================================

    @Override
    public SearchResult search(String text, String query) {
        return search(text, query, false, false);
    }

    @Override
    public SearchResult search(String text, String query, boolean matchCase, boolean wholeWord) {
        if (text == null || query == null || query.trim().isEmpty()) {
            return SearchResult.notFound(query);
        }

        String regex = wholeWord ? "\\b" + Pattern.quote(query) + "\\b" : Pattern.quote(query);
        int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE;
        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(text);

        List<Integer> matchIndices = new ArrayList<>();
        List<String> matchedLines = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");

        while (matcher.find()) {
            matchIndices.add(matcher.start());
        }

        // Find sample matching lines for context
        Pattern linePattern = Pattern.compile(regex, flags);
        for (int i = 0; i < lines.length; i++) {
            if (linePattern.matcher(lines[i]).find()) {
                matchedLines.add(String.format("Line %d: %s", (i + 1), lines[i].trim()));
                if (matchedLines.size() >= 10) break; // Limit context snippets
            }
        }

        return new SearchResult(query, matchIndices.size(), matchIndices, matchedLines);
    }

    // =========================================================================
    // Transformable Interface Implementations (Chapter 2.1.9 & 2.1.10)
    // =========================================================================

    @Override
    public String findAndReplace(String text, String target, String replacement, boolean matchCase) {
        if (text == null || target == null || target.isEmpty()) {
            return text != null ? text : "";
        }
        String safeReplacement = replacement != null ? replacement : "";

        if (matchCase) {
            return text.replace(target, safeReplacement);
        } else {
            // Case-insensitive replacement
            Pattern pattern = Pattern.compile(Pattern.quote(target), Pattern.CASE_INSENSITIVE);
            return pattern.matcher(text).replaceAll(Matcher.quoteReplacement(safeReplacement));
        }
    }

    @Override
    public String convertCase(String text, CaseType caseType) {
        return TextUtils.convertCase(text, caseType);
    }
}
