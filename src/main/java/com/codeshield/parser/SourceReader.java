package com.codeshield.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SourceReader {

    private final String filename;
    private String sourceCode;
    private List<String> lines;
    private String error;

    public SourceReader(String filename) {
        this.filename = filename;
    }

    public boolean read() {
        try {
            Path path = Path.of(filename);
            sourceCode = Files.readString(path);
            lines = Files.readAllLines(path);
            return true;
        } catch (IOException e) {
            error = "Could not read file: " + e.getMessage();
            return false;
        }
    }

    public int countLoc() {
        if (lines == null) {
            return 0;
        }

        int count = 0;
        boolean inMultilineString = false;

        for (String line : lines) {
            String stripped = line.strip();

            if (stripped.contains("\"\"\"") || stripped.contains("'''")) {
                String quote = stripped.contains("\"\"\"") ? "\"\"\"" : "'''";
                int occurrences = countOccurrences(stripped, quote);
                if (occurrences == 1) {
                    inMultilineString = !inMultilineString;
                }

                continue;
            }

            if (inMultilineString) {
                continue;
            }
            if (stripped.isEmpty()) {
                continue;
            }
            if (stripped.startsWith("#")) {
                continue;
            }

            count++;
        }

        return count;
    }

    private int countOccurrences(String text, String sub) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length();
        }
        return count;
    }

    public String getFilename() {
        return filename;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public List<String> getLines() {
        return lines;
    }

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }
}
