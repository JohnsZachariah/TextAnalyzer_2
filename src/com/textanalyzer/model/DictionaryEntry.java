package com.textanalyzer.model;

import com.textanalyzer.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a word's dictionary entry with definitions,
 * phonetics, parts of speech, and usage examples.
 * Demonstrates Module 2: Encapsulation, Composition, and Inner Classes.
 */
public class DictionaryEntry {

    public static class DefinitionItem {
        private final String definition;
        private final String example;

        public DefinitionItem(String definition, String example) {
            this.definition = definition != null ? definition : "";
            this.example = example != null ? example : "";
        }

        public String getDefinition() { return definition; }
        public String getExample() { return example; }
    }

    public static class Meaning {
        private final String partOfSpeech;
        private final List<DefinitionItem> definitions;
        private final List<String> synonyms;
        private final List<String> antonyms;

        public Meaning(String partOfSpeech) {
            this.partOfSpeech = partOfSpeech != null ? partOfSpeech : "";
            this.definitions = new ArrayList<>();
            this.synonyms = new ArrayList<>();
            this.antonyms = new ArrayList<>();
        }

        public String getPartOfSpeech() { return partOfSpeech; }
        public List<DefinitionItem> getDefinitions() { return definitions; }
        public List<String> getSynonyms() { return synonyms; }
        public List<String> getAntonyms() { return antonyms; }

        public void addDefinition(String def, String example) {
            this.definitions.add(new DefinitionItem(def, example));
        }
    }

    private final String word;
    private final String phonetic;
    private final String audioUrl;
    private final List<Meaning> meanings;
    private boolean fromCache;

    public DictionaryEntry(String word, String phonetic, String audioUrl) {
        this.word = word != null ? word : "";
        this.phonetic = phonetic != null ? phonetic : "";
        this.audioUrl = audioUrl != null ? audioUrl : "";
        this.meanings = new ArrayList<>();
        this.fromCache = false;
    }

    public String getWord() { return word; }
    public String getPhonetic() { return phonetic; }
    public String getAudioUrl() { return audioUrl; }
    public List<Meaning> getMeanings() { return meanings; }
    public boolean isFromCache() { return fromCache; }
    public void setFromCache(boolean fromCache) { this.fromCache = fromCache; }

    public void addMeaning(Meaning meaning) {
        this.meanings.add(meaning);
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"word\":\"").append(JsonUtil.escape(word)).append("\",");
        sb.append("\"phonetic\":\"").append(JsonUtil.escape(phonetic)).append("\",");
        sb.append("\"audioUrl\":\"").append(JsonUtil.escape(audioUrl)).append("\",");
        sb.append("\"fromCache\":").append(fromCache).append(",");
        sb.append("\"meanings\":[");

        for (int i = 0; i < meanings.size(); i++) {
            if (i > 0) sb.append(",");
            Meaning m = meanings.get(i);
            sb.append("{");
            sb.append("\"partOfSpeech\":\"").append(JsonUtil.escape(m.getPartOfSpeech())).append("\",");
            sb.append("\"definitions\":[");
            for (int j = 0; j < m.getDefinitions().size(); j++) {
                if (j > 0) sb.append(",");
                DefinitionItem def = m.getDefinitions().get(j);
                sb.append("{");
                sb.append("\"definition\":\"").append(JsonUtil.escape(def.getDefinition())).append("\",");
                sb.append("\"example\":\"").append(JsonUtil.escape(def.getExample())).append("\"");
                sb.append("}");
            }
            sb.append("],");
            sb.append("\"synonyms\":[");
            for (int k = 0; k < m.getSynonyms().size(); k++) {
                if (k > 0) sb.append(",");
                sb.append("\"").append(JsonUtil.escape(m.getSynonyms().get(k))).append("\"");
            }
            sb.append("]");
            sb.append("}");
        }

        sb.append("]}");
        return sb.toString();
    }
}
