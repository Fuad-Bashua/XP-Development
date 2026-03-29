package com.codeshield.compare;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonReportReader {

    private String projectName;
    private String timestamp;
    private final List<Map<String, String>> modules = new ArrayList<>();

    private static final Pattern KV_STRING = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern KV_NUMBER = Pattern.compile("\"(\\w+)\"\\s*:\\s*([\\d.\\-]+)");
    private static final Pattern KV_BOOL = Pattern.compile("\"(\\w+)\"\\s*:\\s*(true|false)");

    public void load(String filepath) throws IOException {
        String content = Files.readString(Path.of(filepath));
        Matcher pn = Pattern.compile("\"project_name\"\\s*:\\s*\"([^\"]*)\"").matcher(content);
        if (pn.find()) projectName = pn.group(1);
        Matcher ts = Pattern.compile("\"timestamp\"\\s*:\\s*\"([^\"]*)\"").matcher(content);
        if (ts.find()) timestamp = ts.group(1);
        int modulesStart = content.indexOf("\"modules\"");
        if (modulesStart == -1) return;
        String modulesSection = content.substring(modulesStart);
        for (String block : extractModuleBlocks(modulesSection)) {
            Map<String, String> data = new HashMap<>();
            Matcher sm = KV_STRING.matcher(block); while (sm.find()) data.put(sm.group(1), sm.group(2));
            Matcher nm = KV_NUMBER.matcher(block); while (nm.find()) data.putIfAbsent(nm.group(1), nm.group(2));
            Matcher bm = KV_BOOL.matcher(block); while (bm.find()) data.putIfAbsent(bm.group(1), bm.group(2));
            if (data.containsKey("filename")) modules.add(data);
        }
    }

    private List<String> extractModuleBlocks(String text) {
        List<String> blocks = new ArrayList<>();
        int depth = 0, blockStart = -1;
        boolean inArray = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!inArray && c == '[') { inArray = true; continue; }
            if (!inArray) continue;
            if (c == '{') { if (depth == 0) blockStart = i; depth++; }
            else if (c == '}') { depth--; if (depth == 0 && blockStart >= 0) {
                String block = text.substring(blockStart, i + 1);
                int fi = block.indexOf("\"functions\"");
                if (fi > 0) block = block.substring(0, fi) + "}";
                blocks.add(block); blockStart = -1;
            }}
            if (inArray && depth == 0 && c == ']') break;
        }
        return blocks;
    }

    public String getProjectName() { return projectName; }
    public String getTimestamp() { return timestamp; }
    public List<Map<String, String>> getModules() { return modules; }

    public static String extractBasename(String filepath) {
        if (filepath == null) return "";
        int slash = Math.max(filepath.lastIndexOf('/'), filepath.lastIndexOf('\\'));
        return slash >= 0 ? filepath.substring(slash + 1) : filepath;
    }
}
