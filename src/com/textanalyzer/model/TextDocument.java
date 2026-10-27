package com.textanalyzer.model;

import com.textanalyzer.exception.InvalidInputException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Encapsulates a text document undergoing analysis.
 * Demonstrates Data Abstraction, Encapsulation, and Constructor Overloading.
 */
public class TextDocument {

    private final String id;
    private final String content;
    private final String sourceName;
    private final LocalDateTime creationTime;

    /**
     * Default constructor for direct text input.
     * Overloaded constructor demonstrating Constructor Chaining (OOP Module 2).
     */
    public TextDocument(String content) {
        this(content, "Direct Input");
    }

    public TextDocument(String content, String sourceName) {
        this("DOC-" + System.currentTimeMillis(), content, sourceName, LocalDateTime.now());
    }

    public TextDocument(String id, String content, String sourceName, LocalDateTime creationTime) {
        if (content == null) {
            throw new InvalidInputException("Text content cannot be null.");
        }
        this.id = Objects.requireNonNullElseGet(id, () -> "DOC-" + System.currentTimeMillis());
        this.content = content;
        this.sourceName = (sourceName == null || sourceName.trim().isEmpty()) ? "Direct Input" : sourceName.trim();
        this.creationTime = creationTime != null ? creationTime : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getSourceName() {
        return sourceName;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return creationTime.format(formatter);
    }

    public int getLength() {
        return content.length();
    }

    public boolean isEmpty() {
        return content.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("TextDocument[id='%s', source='%s', length=%d]", id, sourceName, getLength());
    }
}
