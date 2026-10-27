package com.textanalyzer.test;

import com.textanalyzer.core.ComprehensiveTextAnalyzer;
import com.textanalyzer.model.*;
import com.textanalyzer.pattern.*;
import com.textanalyzer.storage.FileStorageService;
import com.textanalyzer.storage.StorageService;
import com.textanalyzer.util.TextUtils;
import java.io.File;
import java.util.List;

/**
 * Automated Test Suite verifying all functional requirements from the
 * Report Specification (Word_Counter_and_Text_Analyzer.pdf) and OOP principles.
 */
public class TextAnalyzerTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("         RUNNING AUTOMATED VERIFICATION TEST SUITE              ");
        System.out.println("================================================================");

        ComprehensiveTextAnalyzer analyzer = new ComprehensiveTextAnalyzer();

        testWordAndCharacterCounts(analyzer);
        testSentenceCounting(analyzer);
        testVowelAndConsonantCounts(analyzer);
        testLongestAndShortestWords(analyzer);
        testWordFrequency(analyzer);
        testSearchFunctionality(analyzer);
        testFindAndReplace(analyzer);
        testCaseConversion(analyzer);
        testFileAdapterAndPersistence();
        testReportExporters(analyzer);

        System.out.println("================================================================");
        System.out.printf("   TEST RESULTS: %d / %d PASSED (100%%)\n", testsPassed, testsTotal);
        System.out.println("================================================================");
    }

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.printf("   [PASS] %s\n", testName);
        } else {
            System.err.printf("   [FAIL] %s\n", testName);
            throw new AssertionError("Test failed: " + testName);
        }
    }

    private static void testWordAndCharacterCounts(ComprehensiveTextAnalyzer analyzer) {
        try {
            String sample = "Hello World! 123";
            AnalysisResult res = analyzer.analyze(sample);

            assertTrue("Word count equals 3", res.getWordCount() == 3);
            assertTrue("Char count with spaces is 16", res.getCharCountWithSpaces() == 16);
            assertTrue("Char count without spaces is 14", res.getCharCountWithoutSpaces() == 14);
            assertTrue("Letter count is 10", res.getLetterCount() == 10);
            assertTrue("Digit count is 3", res.getDigitCount() == 3);
            assertTrue("Special char count is 1 ('!')", res.getSpecialCharCount() == 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void testSentenceCounting(ComprehensiveTextAnalyzer analyzer) {
        try {
            String sample = "First sentence. Second sentence? Third sentence!";
            AnalysisResult res = analyzer.analyze(sample);
            assertTrue("Sentence count is 3", res.getSentenceCount() == 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void testVowelAndConsonantCounts(ComprehensiveTextAnalyzer analyzer) {
        try {
            // "Apple" -> vowels: A, e (2); consonants: p, p, l (3)
            String sample = "Apple";
            AnalysisResult res = analyzer.analyze(sample);
            assertTrue("Total vowels in 'Apple' is 2", res.getTotalVowels() == 2);
            assertTrue("Total consonants in 'Apple' is 3", res.getTotalConsonants() == 3);
            assertTrue("Vowel 'a' count is 1", res.getVowelBreakdown().get('a') == 1);
            assertTrue("Vowel 'e' count is 1", res.getVowelBreakdown().get('e') == 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void testLongestAndShortestWords(ComprehensiveTextAnalyzer analyzer) {
        try {
            String sample = "I love programming in Java";
            AnalysisResult res = analyzer.analyze(sample);
            assertTrue("Longest word is 'programming' (11 chars)", res.getLongestWordLength() == 11 && res.getLongestWords().contains("programming"));
            assertTrue("Shortest word is 'i' (1 char)", res.getShortestWordLength() == 1 && res.getShortestWords().contains("i"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void testWordFrequency(ComprehensiveTextAnalyzer analyzer) {
        try {
            String sample = "Java is great. Java is object oriented. Java!";
            AnalysisResult res = analyzer.analyze(sample);
            assertTrue("Unique word count is 5", res.getUniqueWordCount() == 5);
            WordFrequency top = res.getWordFrequencies().get(0);
            assertTrue("Top word is 'java' with count 3", "java".equals(top.getWord()) && top.getCount() == 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void testSearchFunctionality(ComprehensiveTextAnalyzer analyzer) {
        String text = "The quick brown fox jumps over the lazy dog. Fox is cunning.";
        SearchResult res = analyzer.search(text, "fox", false, false);
        assertTrue("Search found query 'fox'", res.isFound());
        assertTrue("Search occurrences for 'fox' is 2", res.getOccurrences() == 2);

        SearchResult resCase = analyzer.search(text, "Fox", true, false);
        assertTrue("Case sensitive search occurrences for 'Fox' is 1", resCase.getOccurrences() == 1);
    }

    private static void testFindAndReplace(ComprehensiveTextAnalyzer analyzer) {
        String original = "Java 8 is old. Java is still popular.";
        String replaced = analyzer.findAndReplace(original, "Java", "Kotlin", false);
        assertTrue("Find and replace replaced all occurrences", "Kotlin 8 is old. Kotlin is still popular.".equals(replaced));
    }

    private static void testCaseConversion(ComprehensiveTextAnalyzer analyzer) {
        String input = "object oriented programming";
        assertTrue("Upper case conversion", "OBJECT ORIENTED PROGRAMMING".equals(analyzer.convertCase(input, CaseType.UPPERCASE)));
        assertTrue("Lower case conversion", "object oriented programming".equals(analyzer.convertCase(input, CaseType.LOWERCASE)));
        assertTrue("Title case conversion", "Object Oriented Programming".equals(analyzer.convertCase(input, CaseType.TITLE_CASE)));
    }

    private static void testFileAdapterAndPersistence() {
        try {
            FileSourceAdapter adapter = new FileSourceAdapter("data/sample.txt");
            TextDocument doc = adapter.loadDocument();
            assertTrue("FileSourceAdapter loads data/sample.txt", doc != null && doc.getLength() > 0);

            StorageService storage = new FileStorageService();
            HistoryRecord record = new HistoryRecord("REC-TEST", "Unit Test Document", "Preview content", 50, 300, 4);
            storage.saveAnalysisRecord(record);

            List<HistoryRecord> history = storage.loadHistory();
            assertTrue("FileStorageService persisted record and loaded it back", !history.isEmpty() && "Unit Test Document".equals(history.get(0).getTitle()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void testReportExporters(ComprehensiveTextAnalyzer analyzer) {
        try {
            AnalysisResult res = analyzer.analyze("Testing report exporters with Java OOP.");
            ReportExporter txtExp = ReportExporterFactory.getExporter("TEXT");
            ReportExporter jsonExp = ReportExporterFactory.getExporter("JSON");

            String txtOutput = txtExp.export(res);
            String jsonOutput = jsonExp.export(res);

            assertTrue("Text exporter produces header", txtOutput.contains("WORD COUNTER & TEXT ANALYZER REPORT"));
            assertTrue("JSON exporter produces JSON string", jsonOutput.contains("\"wordCount\":6"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
