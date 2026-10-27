package com.textanalyzer.util;

import com.textanalyzer.model.CaseType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility class providing pure text manipulation algorithms.
 * Demonstrates:
 * - Static methods and constants (OOP Module 2)
 * - String and StringBuilder manipulation (OOP Module 1)
 * - Regular Expressions
 * - Control flow and algorithms
 */
public final class TextUtils {

    // Private constructor to prevent instantiation (Utility class pattern)
    private TextUtils() {
        throw novelUnsupportedOperationException();
    }

    private static UnsupportedOperationException novelUnsupportedOperationException() {
        return new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if a character is an English vowel (case-insensitive).
     */
    public static boolean isVowel(char ch) {
        char lower = Character.toLowerCase(ch);
        return lower == 'a' || lower == 'e' || lower == 'i' || lower == 'o' || lower == 'u';
    }

    /**
     * Checks if a character is an English consonant (letter and not a vowel).
     */
    public static boolean isConsonant(char ch) {
        return Character.isLetter(ch) && !isVowel(ch);
    }

    /**
     * Extracts words from text, stripping surrounding punctuation while preserving hyphens/apostrophes where appropriate.
     */
    public static List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return words;
        }

        // Match continuous alphabetic/alphanumeric tokens, including internal apostrophes/hyphens
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+(?:['’\\-][a-zA-Z0-9]+)*");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }

    /**
     * Counts sentences ending with '.', '?', or '!' as specified in Chapter 2.1.4.
     */
    public static int countSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        // Match sentence terminating punctuation followed by whitespace or end of string
        Pattern pattern = Pattern.compile("[.!?]+(?:\\s+|$)");
        Matcher matcher = pattern.matcher(text.trim());
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        // If text contains words but no terminating punctuation, treat the whole block as at least 1 sentence
        if (count == 0 && !extractWords(text).isEmpty()) {
            return 1;
        }
        return count;
    }

    /**
     * Counts paragraphs separated by two or more newlines.
     */
    public static int countParagraphs(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] paragraphs = text.split("(\\r?\\n\\s*){2,}");
        int count = 0;
        for (String p : paragraphs) {
            if (!p.trim().isEmpty()) {
                count++;
            }
        }
        return Math.max(count, 1);
    }

    /**
     * Converts text according to the requested CaseType.
     * Demonstrates Switch expression and StringBuilder.
     */
    public static String convertCase(String text, CaseType caseType) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        switch (caseType) {
            case UPPERCASE:
                return text.toUpperCase();
            case LOWERCASE:
                return text.toLowerCase();
            case TITLE_CASE:
                return toTitleCase(text);
            case SENTENCE_CASE:
                return toSentenceCase(text);
            default:
                return text;
        }
    }

    /**
     * Converts each word's initial character to uppercase and the rest to lowercase.
     */
    public static String toTitleCase(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        boolean capitalizeNext = true;

        for (char ch : text.toCharArray()) {
            if (Character.isWhitespace(ch) || ch == '-' || ch == '/' || ch == '(') {
                sb.append(ch);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toTitleCase(ch));
                capitalizeNext = false;
            } else {
                sb.append(Character.toLowerCase(ch));
            }
        }
        return sb.toString();
    }

    /**
     * Capitalizes the first letter of each sentence while preserving subsequent casing.
     */
    public static String toSentenceCase(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        boolean capitalizeNext = true;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (capitalizeNext && Character.isLetter(ch)) {
                sb.append(Character.toUpperCase(ch));
                capitalizeNext = false;
            } else {
                sb.append(Character.toLowerCase(ch));
            }

            if (ch == '.' || ch == '?' || ch == '!') {
                capitalizeNext = true;
            }
        }
        return sb.toString();
    }

    /**
     * Computes the Flesch Reading Ease score:
     * 206.835 - 1.015 * (total words / total sentences) - 84.6 * (total syllables / total words)
     */
    public static double calculateFleschReadingEase(int totalWords, int totalSentences, int totalSyllables) {
        if (totalWords == 0 || totalSentences == 0) {
            return 0.0;
        }
        double wordsPerSentence = (double) totalWords / totalSentences;
        double syllablesPerWord = (double) totalSyllables / totalWords;
        double score = 206.835 - (1.015 * wordsPerSentence) - (84.6 * syllablesPerWord);
        return Math.max(0.0, Math.min(100.0, Math.round(score * 10.0) / 10.0));
    }

    /**
     * Estimates syllable count for a word using heuristic vowel cluster counting.
     */
    public static int estimateSyllables(String word) {
        if (word == null || word.isEmpty()) return 0;
        String lower = word.toLowerCase();
        int count = 0;
        boolean prevIsVowel = false;

        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            boolean currIsVowel = isVowel(ch) || (ch == 'y' && i > 0);
            if (currIsVowel && !prevIsVowel) {
                count++;
            }
            prevIsVowel = currIsVowel;
        }

        // Silent 'e' at end
        if (lower.endsWith("e") && !lower.endsWith("le") && count > 1) {
            count--;
        }

        return Math.max(1, count);
    }

    /**
     * Categorizes readability level according to Flesch score.
     */
    public static String getReadabilityDescription(double score) {
        if (score >= 90.0) return "Very Easy (5th grade level)";
        if (score >= 80.0) return "Easy (6th grade level)";
        if (score >= 70.0) return "Fairly Easy (7th grade level)";
        if (score >= 60.0) return "Standard (8th-9th grade level)";
        if (score >= 50.0) return "Fairly Difficult (10th-12th grade level)";
        if (score >= 30.0) return "Difficult (College level)";
        return "Very Difficult (Graduate level)";
    }
}
