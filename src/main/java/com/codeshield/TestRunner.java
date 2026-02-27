package com.codeshield;

import com.codeshield.analysis.ComplexityAnalyser;
import com.codeshield.model.*;
import com.codeshield.parser.CFGBuilder;
import com.codeshield.parser.SourceReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;
    private static final ComplexityAnalyser analyser = new ComplexityAnalyser();

    public static void main(String[] args) throws IOException {
        System.out.println("\n  CodeShield Test Runner - Iteration 1");
        System.out.println("  " + "=".repeat(50));

        section("LOC Counting");
        testLocSimple();
        testLocBlanksExcluded();
        testLocCommentsExcluded();
        testLocEmpty();
        testLocDocstrings();

        section("CFG Construction");
        testSingleFunction();
        testMultipleFunctions();
        testEntryAndExit();
        testClassMethods();
        testNoFunctions();

        section("Cyclomatic Complexity (M = E - N + 2P)");
        testKnownCFGDiamond();
        testLinearCFG();
        testAppendixA();
        testSimpleFunctionCC();
        testIfIncreasesCC();
        testMultipleBranches();
        testForLoop();
        testWhileLoop();
        testMinimumCC();

        section("Risk Classification");
        testRiskLow();
        testRiskModerate();
        testRiskHigh();
        testRiskVeryHigh();

        section("Error Handling");
        testNonExistentFile();
        testNoFunctionsModule();
        testSourceReaderError();

        System.out.println("\n  " + "=".repeat(50));
        System.out.printf("  RESULTS: %d passed, %d failed, %d total%n",
                passed, failed, passed + failed);
        System.out.println("  " + "=".repeat(50));

        if (failed > 0) {
            System.exit(1);
        }
    }

    static void testLocSimple() throws IOException {
        Path f = writeTempPython("x = 1\ny = 2\nz = x + y\n");
        SourceReader r = new SourceReader(f.toString());
        r.read();
        assertEquals("Simple LOC count", 3, r.countLoc());
        Files.delete(f);
    }

    static void testLocBlanksExcluded() throws IOException {
        Path f = writeTempPython("x = 1\n\n\ny = 2\n\n");
        SourceReader r = new SourceReader(f.toString());
        r.read();
        assertEquals("Blanks excluded", 2, r.countLoc());
        Files.delete(f);
    }

    static void testLocCommentsExcluded() throws IOException {
        Path f = writeTempPython("# comment\nx = 1\n# another\ny = 2\n");
        SourceReader r = new SourceReader(f.toString());
        r.read();
        assertEquals("Comments excluded", 2, r.countLoc());
        Files.delete(f);
    }

    static void testLocEmpty() throws IOException {
        Path f = writeTempPython("");
        SourceReader r = new SourceReader(f.toString());
        r.read();
        assertEquals("Empty file LOC", 0, r.countLoc());
        Files.delete(f);
    }

    static void testLocDocstrings() throws IOException {
        Path f = writeTempPython("def f():\n    \"\"\"Docstring.\"\"\"\n    return 1\n");
        SourceReader r = new SourceReader(f.toString());
        r.read();
        assertEquals("Docstrings excluded", 2, r.countLoc());
        Files.delete(f);
    }

    static void testSingleFunction() {
        List<String> lines = List.of("def hello():", "    return 'hi'");
        List<ControlFlowGraph> cfgs = new CFGBuilder().buildAll(lines);
        assertEquals("Single function detected", 1, cfgs.size());
        assertEquals("Function name", "hello", cfgs.get(0).getFunctionName());
    }

    static void testMultipleFunctions() {
        List<String> lines = List.of(
                "def a():", "    pass", "",
                "def b():", "    pass", "",
                "def c():", "    pass");
        List<ControlFlowGraph> cfgs = new CFGBuilder().buildAll(lines);
        assertEquals("Three functions detected", 3, cfgs.size());
    }

    static void testEntryAndExit() {
        List<String> lines = List.of("def f():", "    x = 1");
        ControlFlowGraph cfg = new CFGBuilder().buildAll(lines).get(0);
        List<String> labels = cfg.getNodes().stream().map(CFGNode::getLabel).toList();
        assertTrue("CFG has ENTRY", labels.contains("ENTRY"));
        assertTrue("CFG has EXIT", labels.contains("EXIT"));
    }

    static void testClassMethods() {
        List<String> lines = List.of(
                "class Foo:", "    def bar(self):", "        pass",
                "    def baz(self):", "        pass");
        List<ControlFlowGraph> cfgs = new CFGBuilder().buildAll(lines);
        assertEquals("Class methods detected", 2, cfgs.size());
    }

    static void testNoFunctions() {
        List<String> lines = List.of("x = 1", "y = 2");
        List<ControlFlowGraph> cfgs = new CFGBuilder().buildAll(lines);
        assertEquals("No functions found", 0, cfgs.size());
    }

    static void testKnownCFGDiamond() {

        ControlFlowGraph cfg = new ControlFlowGraph("test");
        cfg.addNode(new CFGNode(0, "ENTRY", 1));
        cfg.addNode(new CFGNode(1, "IF", 2));
        cfg.addNode(new CFGNode(2, "A", 3));
        cfg.addNode(new CFGNode(3, "EXIT", -1));
        cfg.addEdge(new CFGEdge(0, 1));
        cfg.addEdge(new CFGEdge(1, 2));
        cfg.addEdge(new CFGEdge(1, 3));
        cfg.addEdge(new CFGEdge(2, 3));
        cfg.setDecisionCount(1);
        FunctionComplexity result = analyser.calculateComplexity(cfg);
        assertEquals("Diamond CFG: M=2", 2, result.getCyclomaticComplexity());
    }

    static void testLinearCFG() {

        ControlFlowGraph cfg = new ControlFlowGraph("linear");
        cfg.addNode(new CFGNode(0, "ENTRY", 1));
        cfg.addNode(new CFGNode(1, "STMT", 2));
        cfg.addNode(new CFGNode(2, "EXIT", -1));
        cfg.addEdge(new CFGEdge(0, 1));
        cfg.addEdge(new CFGEdge(1, 2));
        cfg.setDecisionCount(0);
        FunctionComplexity result = analyser.calculateComplexity(cfg);
        assertEquals("Linear CFG: M=1", 1, result.getCyclomaticComplexity());
    }

    static void testAppendixA() {

        ControlFlowGraph cfg = new ControlFlowGraph("payment_service");
        for (int i = 0; i < 8; i++) cfg.addNode(new CFGNode(i, "N" + i, i));
        cfg.setDecisionCount(3);
        FunctionComplexity result = analyser.calculateComplexity(cfg);
        assertEquals("Appendix A: M=4", 4, result.getCyclomaticComplexity());
    }

    static void testSimpleFunctionCC() throws IOException {
        Path f = writeTempPython("def simple():\n    x = 1\n    y = 2\n    return x + y\n");
        ModuleResult r = analyser.analyse(f.toString());
        assertTrue("Simple function CC >= 1", r.getMaxComplexity() >= 1);
        Files.delete(f);
    }

    static void testIfIncreasesCC() throws IOException {
        Path f = writeTempPython("def f(x):\n    if x > 0:\n        return 1\n    return 0\n");
        ModuleResult r = analyser.analyse(f.toString());
        assertTrue("If gives CC > 1", r.getMaxComplexity() > 1);
        Files.delete(f);
    }

    static void testMultipleBranches() throws IOException {
        String code = "def f(x):\n    if x > 10:\n        return 'a'\n"
                + "    elif x > 5:\n        return 'b'\n"
                + "    elif x > 0:\n        return 'c'\n"
                + "    else:\n        return 'd'\n";
        Path f = writeTempPython(code);
        ModuleResult r = analyser.analyse(f.toString());
        assertTrue("Multiple branches CC > 2", r.getMaxComplexity() > 2);
        Files.delete(f);
    }

    static void testForLoop() throws IOException {
        Path f = writeTempPython("def f(items):\n    for i in items:\n        print(i)\n");
        ModuleResult r = analyser.analyse(f.toString());
        assertTrue("For loop CC > 1", r.getMaxComplexity() > 1);
        Files.delete(f);
    }

    static void testWhileLoop() throws IOException {
        Path f = writeTempPython("def f():\n    while True:\n        pass\n");
        ModuleResult r = analyser.analyse(f.toString());
        assertTrue("While loop CC > 1", r.getMaxComplexity() > 1);
        Files.delete(f);
    }

    static void testMinimumCC() {
        ControlFlowGraph cfg = new ControlFlowGraph("empty");
        cfg.addNode(new CFGNode(0, "ENTRY", 1));
        cfg.addNode(new CFGNode(1, "EXIT", -1));
        cfg.addEdge(new CFGEdge(0, 1));
        cfg.setDecisionCount(0);
        assertTrue("Minimum CC >= 1", analyser.calculateComplexity(cfg).getCyclomaticComplexity() >= 1);
    }

    static void testRiskLow() {
        assertEquals("CC=5 Low Risk", "Low Risk", ComplexityAnalyser.classifyRisk(5));
        assertEquals("CC=10 Low Risk", "Low Risk", ComplexityAnalyser.classifyRisk(10));
    }

    static void testRiskModerate() {
        assertEquals("CC=15 Moderate", "Moderate Risk", ComplexityAnalyser.classifyRisk(15));
    }

    static void testRiskHigh() {
        assertEquals("CC=30 High", "High Risk", ComplexityAnalyser.classifyRisk(30));
    }

    static void testRiskVeryHigh() {
        assertEquals("CC=60 Very High", "Very High Risk", ComplexityAnalyser.classifyRisk(60));
    }

    static void testNonExistentFile() {
        ModuleResult r = analyser.analyse("/nonexistent/file.py");
        assertTrue("Missing file is skipped", r.isSkipped());
        assertEquals("Status = skipped", "skipped/unsupported", r.getStatus());
    }

    static void testNoFunctionsModule() throws IOException {
        Path f = writeTempPython("x = 1\ny = 2\nprint(x + y)\n");
        ModuleResult r = analyser.analyse(f.toString());
        assertEquals("No functions: max CC = 0", 0, r.getMaxComplexity());
        Files.delete(f);
    }

    static void testSourceReaderError() {
        SourceReader r = new SourceReader("/does/not/exist.py");
        assertTrue("Read fails for missing file", !r.read());
        assertTrue("Error is set", r.hasError());
    }

    private static Path writeTempPython(String code) throws IOException {
        Path f = Files.createTempFile("codeshield_test_", ".py");
        Files.writeString(f, code);
        return f;
    }

    private static void section(String name) {
        System.out.println("\n  ── " + name + " ──");
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("    ✓ " + testName);
            passed++;
        } else {
            System.out.println("    ✗ " + testName + " (expected=" + expected + ", actual=" + actual + ")");
            failed++;
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("    ✓ " + testName);
            passed++;
        } else {
            System.out.println("    ✗ " + testName + " (expected true, got false)");
            failed++;
        }
    }
}
