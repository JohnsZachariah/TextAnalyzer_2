package com.textanalyzer.pattern;

import com.textanalyzer.exception.TextAnalysisException;
import com.textanalyzer.model.TextDocument;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Adapter class that converts an external File into a DocumentSource.
 * Demonstrates:
 * - Adapter Design Pattern (OOP Syllabus Module 3)
 * - Java File I/O using BufferedReader and FileReader (Project Spec Section 1.4)
 * - Try-with-resources statement (OOP Syllabus Module 3)
 * - Exception wrapping
 */
public class FileSourceAdapter implements DocumentSource {

    private final File file;

    public FileSourceAdapter(File file) {
        this.file = file;
    }

    public FileSourceAdapter(String filePath) {
        this(new File(filePath));
    }

    @Override
    public TextDocument loadDocument() throws TextAnalysisException {
        if (file == null || !file.exists()) {
            throw new TextAnalysisException("File does not exist: " + (file != null ? file.getPath() : "null"), "FILE_NOT_FOUND");
        }
        if (!file.canRead()) {
            throw new TextAnalysisException("File cannot be read (permission denied): " + file.getPath(), "FILE_ACCESS_DENIED");
        }

        StringBuilder contentBuilder = new StringBuilder();

        // Java File I/O using BufferedReader / FileReader with try-with-resources
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    contentBuilder.append(System.lineSeparator());
                }
                contentBuilder.append(line);
                firstLine = false;
            }
        } catch (IOException e) {
            throw new TextAnalysisException("I/O error reading file: " + file.getName(), "IO_ERROR", e);
        }

        return new TextDocument(contentBuilder.toString(), file.getName());
    }

    public File getFile() {
        return file;
    }
}
