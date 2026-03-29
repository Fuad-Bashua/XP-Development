package com.codeshield.parser;

import com.codeshield.model.CFGEdge;
import com.codeshield.model.CFGNode;
import com.codeshield.model.ControlFlowGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CFGBuilder {

    private static final Pattern FUNC_DEF = Pattern.compile("^(\\s*)def\\s+(\\w+)\\s*\\(");
    private static final Pattern IF_KW = Pattern.compile("^\\s*if\\s+");
    private static final Pattern ELIF_KW = Pattern.compile("^\\s*elif\\s+");
    private static final Pattern FOR_KW = Pattern.compile("^\\s*for\\s+");
    private static final Pattern WHILE_KW = Pattern.compile("^\\s*while\\s+");
    private static final Pattern EXCEPT_KW = Pattern.compile("^\\s*except\\b");
    private static final Pattern BOOL_AND = Pattern.compile("\\band\\b");
    private static final Pattern BOOL_OR = Pattern.compile("\\bor\\b");

    private int nodeCounter;

    public List<ControlFlowGraph> buildAll(List<String> lines) {
        List<ControlFlowGraph> cfgs = new ArrayList<>();
        for (FunctionBlock func : extractFunctions(lines)) cfgs.add(buildCFG(func));
        return cfgs;
    }

    private List<FunctionBlock> extractFunctions(List<String> lines) {
        List<FunctionBlock> functions = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Matcher matcher = FUNC_DEF.matcher(lines.get(i));
            if (!matcher.find()) continue;
            String funcName = matcher.group(2);
            int defIndent = matcher.group(1).length();
            List<String> bodyLines = new ArrayList<>();
            List<Integer> lineNums = new ArrayList<>();
            for (int j = i + 1; j < lines.size(); j++) {
                String bodyLine = lines.get(j);
                String stripped = bodyLine.strip();
                if (stripped.isEmpty() || stripped.startsWith("#")) {
                    bodyLines.add(bodyLine); lineNums.add(j + 1); continue;
                }
                if (getIndentLevel(bodyLine) <= defIndent) break;
                bodyLines.add(bodyLine); lineNums.add(j + 1);
            }
            functions.add(new FunctionBlock(funcName, i + 1, bodyLines, lineNums));
        }
        return functions;
    }

    private ControlFlowGraph buildCFG(FunctionBlock func) {
        nodeCounter = 0;
        ControlFlowGraph cfg = new ControlFlowGraph(func.name);
        int entryId = addNode(cfg, "ENTRY", func.startLine);
        int prevId = entryId;
        int decisions = 0;
        boolean inDocstring = false;

        for (int i = 0; i < func.bodyLines.size(); i++) {
            String line = func.bodyLines.get(i);
            String stripped = line.strip();
            int lineNum = func.lineNums.get(i);
            if (stripped.isEmpty() || stripped.startsWith("#")) continue;
            if (stripped.contains("\"\"\"") || stripped.contains("'''")) {
                String q = stripped.contains("\"\"\"") ? "\"\"\"" : "'''";
                if (countSubstring(stripped, q) == 1) inDocstring = !inDocstring;
                continue;
            }
            if (inDocstring) continue;

            String label;
            boolean isDecision = false;
            if (IF_KW.matcher(stripped).find()) { label = "If (line " + lineNum + ")"; isDecision = true; }
            else if (ELIF_KW.matcher(stripped).find()) { label = "Elif (line " + lineNum + ")"; isDecision = true; }
            else if (FOR_KW.matcher(stripped).find()) { label = "For (line " + lineNum + ")"; isDecision = true; }
            else if (WHILE_KW.matcher(stripped).find()) { label = "While (line " + lineNum + ")"; isDecision = true; }
            else if (EXCEPT_KW.matcher(stripped).find()) { label = "Except (line " + lineNum + ")"; isDecision = true; }
            else { label = statementLabel(stripped, lineNum); }

            if (isDecision) decisions++;
            decisions += countPattern(stripped, BOOL_AND);
            decisions += countPattern(stripped, BOOL_OR);

            int nodeId = addNode(cfg, label, lineNum);
            cfg.addEdge(new CFGEdge(prevId, nodeId));
            prevId = nodeId;
        }

        int exitId = addNode(cfg, "EXIT", -1);
        cfg.addEdge(new CFGEdge(prevId, exitId));
        cfg.setConnectedComponents(1);
        cfg.setDecisionCount(decisions);
        return cfg;
    }

    private int addNode(ControlFlowGraph cfg, String label, int lineNumber) {
        int id = nodeCounter++;
        cfg.addNode(new CFGNode(id, label, lineNumber));
        return id;
    }

    private String statementLabel(String stripped, int lineNum) {
        if (stripped.startsWith("return")) return "Return (line " + lineNum + ")";
        if (stripped.startsWith("try:")) return "Try (line " + lineNum + ")";
        if (stripped.startsWith("else:")) return "Else (line " + lineNum + ")";
        if (stripped.startsWith("finally:")) return "Finally (line " + lineNum + ")";
        if (stripped.startsWith("raise")) return "Raise (line " + lineNum + ")";
        return "Statement (line " + lineNum + ")";
    }

    private int getIndentLevel(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') count++;
            else if (c == '\t') count += 4;
            else break;
        }
        return count;
    }

    private int countPattern(String text, Pattern pattern) {
        int count = 0; Matcher m = pattern.matcher(text);
        while (m.find()) count++;
        return count;
    }

    private int countSubstring(String text, String sub) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) { count++; idx += sub.length(); }
        return count;
    }

    private static class FunctionBlock {
        final String name; final int startLine;
        final List<String> bodyLines; final List<Integer> lineNums;
        FunctionBlock(String name, int startLine, List<String> bodyLines, List<Integer> lineNums) {
            this.name = name; this.startLine = startLine;
            this.bodyLines = bodyLines; this.lineNums = lineNums;
        }
    }
}
