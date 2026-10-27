package com.textanalyzer.pattern;

import com.textanalyzer.model.AnalysisResult;
import com.textanalyzer.model.WordFrequency;
import java.util.Map;

/**
 * Concrete strategy generating a formatted plain-text analysis report.
 * Demonstrates Strategy Pattern and StringBuilder formatting.
 */
public class TextReportExporter implements ReportExporter {

    @Override
    public String export(AnalysisResult res) {
        if (res == null) return "No analysis data available.";

        StringBuilder sb = new StringBuilder();
        String line = "========================================================================\n";
        String sub = "------------------------------------------------------------------------\n";

        sb.append(line);
        sb.append("                  WORD COUNTER & TEXT ANALYZER REPORT                  \n");
        sb.append(line);
        sb.append(String.format(" Document ID   : %s\n", res.getDocumentId()));
        sb.append(String.format(" Source        : %s\n", res.getSourceName()));
        sb.append(String.format(" Generated At  : %s\n", res.getTimestamp()));
        sb.append(sub);
        sb.append(" 1. GENERAL METRICS\n");
        sb.append(String.format("    - Total Words                  : %,d\n", res.getWordCount()));
        sb.append(String.format("    - Total Characters (w/ spaces) : %,d\n", res.getCharCountWithSpaces()));
        sb.append(String.format("    - Characters (without spaces)  : %,d\n", res.getCharCountWithoutSpaces()));
        sb.append(String.format("    - Letters                      : %,d\n", res.getLetterCount()));
        sb.append(String.format("    - Digits                       : %,d\n", res.getDigitCount()));
        sb.append(String.format("    - Special Characters           : %,d\n", res.getSpecialCharCount()));
        sb.append(String.format("    - Sentences                    : %,d\n", res.getSentenceCount()));
        sb.append(String.format("    - Paragraphs                   : %,d\n", res.getParagraphCount()));
        sb.append(sub);
        sb.append(" 2. CHARACTER & PHONETIC ANALYSIS\n");
        sb.append(String.format("    - Total Vowels                 : %,d\n", res.getTotalVowels()));
        sb.append(String.format("    - Total Consonants             : %,d\n", res.getTotalConsonants()));
        sb.append("    - Vowel Distribution          : ");
        for (Map.Entry<Character, Integer> e : res.getVowelBreakdown().entrySet()) {
            sb.append(String.format("[%c: %d] ", Character.toUpperCase(e.getKey()), e.getValue()));
        }
        sb.append("\n");
        sb.append(sub);
        sb.append(" 3. VOCABULARY & EXTREMES\n");
        sb.append(String.format("    - Unique Words                 : %,d\n", res.getUniqueWordCount()));
        sb.append(String.format("    - Average Word Length          : %.2f characters\n", res.getAverageWordLength()));
        sb.append(String.format("    - Longest Word Length          : %d characters\n", res.getLongestWordLength()));
        sb.append(String.format("      Longest Words                : %s\n", String.join(", ", res.getLongestWords())));
        sb.append(String.format("    - Shortest Word Length         : %d characters\n", res.getShortestWordLength()));
        sb.append(String.format("      Shortest Words               : %s\n", String.join(", ", res.getShortestWords())));
        sb.append(sub);
        sb.append(" 4. READABILITY & TIMING\n");
        sb.append(String.format("    - Estimated Reading Time       : %.2f min (at ~200 WPM)\n", res.getReadingTimeMinutes()));
        sb.append(String.format("    - Flesch Reading Ease Score    : %.1f / 100\n", res.getFleschReadingEase()));
        sb.append(String.format("    - Readability Level            : %s\n", res.getReadabilityLevel()));
        sb.append(sub);
        sb.append(" 5. TOP WORD FREQUENCIES (UP TO TOP 20)\n");
        int count = 0;
        for (WordFrequency wf : res.getWordFrequencies()) {
            if (++count > 20) break;
            sb.append(String.format("    %2d. %-20s : %4d occurrences (%.2f%%)\n", count, wf.getWord(), wf.getCount(), wf.getPercentage()));
        }
        sb.append(line);
        sb.append("                  END OF TEXT ANALYSIS REPORT                         \n");
        sb.append(line);

        return sb.toString();
    }

    @Override
    public String getFormatName() {
        return "TEXT";
    }

    @Override
    public String getFileExtension() {
        return ".txt";
    }
}
