package com.codeshield;

import com.codeshield.analysis.ComplexityAnalyser;
import com.codeshield.analysis.VulnerabilityCalculator;
import com.codeshield.model.*;
import com.codeshield.parser.CFGBuilder;
import com.codeshield.parser.SourceReader;
import com.codeshield.security.SecurityResult;
import com.codeshield.security.SecurityScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;
    private static final ComplexityAnalyser analyser = new ComplexityAnalyser();
    private static final SecurityScanner scanner = new SecurityScanner();

    public static void main(String[] args) throws IOException {
        System.out.println("\n  CodeShield Test Runner - Iterations 1 & 2");
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

        section("Cyclomatic Complexity (M = D + 1)");
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

        section("Security: Hardcoded Credentials (SEC-001)");
        testHardcodedPassword();
        testHardcodedApiKey();
        testHardcodedToken();

        section("Security: Weak Crypto (SEC-004, SEC-005)");
        testMd5Usage();
        testSha1Usage();

        section("Security: Insecure Random (SEC-006)");
        testInsecureRandom();

        section("Security: SQL Injection (SEC-007, SEC-008)");
        testSqlInjectionFstring();
        testSqlInjectionConcat();

        section("Security: Code Injection (SEC-009, SEC-010)");
        testEvalUsage();
        testExecUsage();

        section("Security: Unsafe Deserialization (SEC-011)");
        testPickleLoads();

        section("Security: Shell Injection (SEC-012, SEC-013)");
        testSubprocessShellTrue();
        testOsSystem();

        section("Security: Insecure Config (SEC-014, SEC-015, SEC-016)");
        testDebugMode();
        testSslVerifyFalse();
        testBindAllInterfaces();

        section("Security: Clean Code");
        testCleanCodeNoFlags();
        testCommentsIgnored();

        section("Security: Severity Counts");
        testSeverityCounts();

        section("Vulnerability Density");
        testVdBasic();
        testVdZeroLoc();
        testVdZeroFlags();
        testVdCourseworkAppendixA();
        testVdCourseworkAppendixB();

        section("Full Pipeline Integration");
        testFullPipelineWithSecurity();

        System.out.println("\n  " + "=".repeat(50));
        System.out.printf("  RESULTS: %d passed, %d failed, %d total%n",
                passed, failed, passed + failed);
        System.out.println("  " + "=".repeat(50));

        if (failed > 0) System.exit(1);
    }

    // ── LOC Tests ──

    static void testLocSimple() throws IOException {
        SourceReader r = reader("x = 1\ny = 2\nz = x + y\n");
        assertEquals("Simple LOC count", 3, r.countLoc());
    }
    static void testLocBlanksExcluded() throws IOException {
        SourceReader r = reader("x = 1\n\n\ny = 2\n\n");
        assertEquals("Blanks excluded", 2, r.countLoc());
    }
    static void testLocCommentsExcluded() throws IOException {
        SourceReader r = reader("# comment\nx = 1\n# another\ny = 2\n");
        assertEquals("Comments excluded", 2, r.countLoc());
    }
    static void testLocEmpty() throws IOException {
        SourceReader r = reader("");
        assertEquals("Empty file LOC", 0, r.countLoc());
    }
    static void testLocDocstrings() throws IOException {
        SourceReader r = reader("def f():\n    \"\"\"Doc.\"\"\"\n    return 1\n");
        assertEquals("Docstrings excluded", 2, r.countLoc());
    }

    // ── CFG Tests ──

    static void testSingleFunction() {
        var cfgs = new CFGBuilder().buildAll(List.of("def hello():", "    return 'hi'"));
        assertEquals("Single function detected", 1, cfgs.size());
        assertEquals("Function name", "hello", cfgs.get(0).getFunctionName());
    }
    static void testMultipleFunctions() {
        var cfgs = new CFGBuilder().buildAll(List.of("def a():", "    pass", "", "def b():", "    pass", "", "def c():", "    pass"));
        assertEquals("Three functions detected", 3, cfgs.size());
    }
    static void testEntryAndExit() {
        var cfg = new CFGBuilder().buildAll(List.of("def f():", "    x = 1")).get(0);
        var labels = cfg.getNodes().stream().map(CFGNode::getLabel).toList();
        assertTrue("CFG has ENTRY", labels.contains("ENTRY"));
        assertTrue("CFG has EXIT", labels.contains("EXIT"));
    }
    static void testClassMethods() {
        var cfgs = new CFGBuilder().buildAll(List.of("class Foo:", "    def bar(self):", "        pass", "    def baz(self):", "        pass"));
        assertEquals("Class methods detected", 2, cfgs.size());
    }
    static void testNoFunctions() {
        var cfgs = new CFGBuilder().buildAll(List.of("x = 1", "y = 2"));
        assertEquals("No functions found", 0, cfgs.size());
    }

    // ── Complexity Tests ──

    static void testKnownCFGDiamond() {
        ControlFlowGraph cfg = new ControlFlowGraph("test");
        cfg.addNode(new CFGNode(0, "ENTRY", 1)); cfg.addNode(new CFGNode(1, "IF", 2));
        cfg.addNode(new CFGNode(2, "A", 3)); cfg.addNode(new CFGNode(3, "EXIT", -1));
        cfg.addEdge(new CFGEdge(0, 1)); cfg.addEdge(new CFGEdge(1, 2));
        cfg.addEdge(new CFGEdge(1, 3)); cfg.addEdge(new CFGEdge(2, 3));
        cfg.setDecisionCount(1);
        assertEquals("Diamond CFG: M=2", 2, analyser.calculateComplexity(cfg).getCyclomaticComplexity());
    }
    static void testLinearCFG() {
        ControlFlowGraph cfg = new ControlFlowGraph("linear");
        cfg.addNode(new CFGNode(0, "ENTRY", 1)); cfg.addNode(new CFGNode(1, "STMT", 2));
        cfg.addNode(new CFGNode(2, "EXIT", -1));
        cfg.addEdge(new CFGEdge(0, 1)); cfg.addEdge(new CFGEdge(1, 2));
        cfg.setDecisionCount(0);
        assertEquals("Linear CFG: M=1", 1, analyser.calculateComplexity(cfg).getCyclomaticComplexity());
    }
    static void testAppendixA() {
        ControlFlowGraph cfg = new ControlFlowGraph("payment_service");
        for (int i = 0; i < 8; i++) cfg.addNode(new CFGNode(i, "N" + i, i));
        cfg.setDecisionCount(3);
        assertEquals("Appendix A: M=4", 4, analyser.calculateComplexity(cfg).getCyclomaticComplexity());
    }
    static void testSimpleFunctionCC() throws IOException {
        ModuleResult r = analyseCode("def simple():\n    x = 1\n    y = 2\n    return x + y\n");
        assertTrue("Simple function CC >= 1", r.getMaxComplexity() >= 1);
    }
    static void testIfIncreasesCC() throws IOException {
        ModuleResult r = analyseCode("def f(x):\n    if x > 0:\n        return 1\n    return 0\n");
        assertTrue("If gives CC > 1", r.getMaxComplexity() > 1);
    }
    static void testMultipleBranches() throws IOException {
        ModuleResult r = analyseCode("def f(x):\n    if x>10:\n        pass\n    elif x>5:\n        pass\n    elif x>0:\n        pass\n");
        assertTrue("Multiple branches CC > 2", r.getMaxComplexity() > 2);
    }
    static void testForLoop() throws IOException {
        ModuleResult r = analyseCode("def f(xs):\n    for x in xs:\n        print(x)\n");
        assertTrue("For loop CC > 1", r.getMaxComplexity() > 1);
    }
    static void testWhileLoop() throws IOException {
        ModuleResult r = analyseCode("def f():\n    while True:\n        pass\n");
        assertTrue("While loop CC > 1", r.getMaxComplexity() > 1);
    }
    static void testMinimumCC() {
        ControlFlowGraph cfg = new ControlFlowGraph("empty");
        cfg.addNode(new CFGNode(0, "ENTRY", 1)); cfg.addNode(new CFGNode(1, "EXIT", -1));
        cfg.addEdge(new CFGEdge(0, 1)); cfg.setDecisionCount(0);
        assertTrue("Minimum CC >= 1", analyser.calculateComplexity(cfg).getCyclomaticComplexity() >= 1);
    }

    // ── Risk Tests ──

    static void testRiskLow() { assertEquals("CC=5 Low Risk", "Low Risk", ComplexityAnalyser.classifyRisk(5)); }
    static void testRiskModerate() { assertEquals("CC=15 Moderate", "Moderate Risk", ComplexityAnalyser.classifyRisk(15)); }
    static void testRiskHigh() { assertEquals("CC=30 High", "High Risk", ComplexityAnalyser.classifyRisk(30)); }
    static void testRiskVeryHigh() { assertEquals("CC=60 Very High", "Very High Risk", ComplexityAnalyser.classifyRisk(60)); }

    // ── Error Handling Tests ──

    static void testNonExistentFile() {
        ModuleResult r = analyser.analyse("/nonexistent/file.py");
        assertTrue("Missing file is skipped", r.isSkipped());
        assertEquals("Status = skipped", "skipped/unsupported", r.getStatus());
    }
    static void testNoFunctionsModule() throws IOException {
        ModuleResult r = analyseCode("x = 1\ny = 2\nprint(x + y)\n");
        assertEquals("No functions: max CC = 0", 0, r.getMaxComplexity());
    }
    static void testSourceReaderError() {
        SourceReader r = new SourceReader("/does/not/exist.py");
        assertTrue("Read fails for missing file", !r.read());
        assertTrue("Error is set", r.hasError());
    }

    // ── Security: Hardcoded Credentials ──

    static void testHardcodedPassword() {
        SecurityResult r = scanner.scan("t.py", "password = \"mysecret123\"\n");
        assertTrue("SEC-001 detects password", hasRule(r, "SEC-001"));
    }
    static void testHardcodedApiKey() {
        SecurityResult r = scanner.scan("t.py", "api_key = \"sk-live-abc123\"\n");
        assertTrue("SEC-001 detects api_key", hasRule(r, "SEC-001"));
    }
    static void testHardcodedToken() {
        SecurityResult r = scanner.scan("t.py", "auth_token = \"bearer_xyz789\"\n");
        assertTrue("SEC-001 detects auth_token", hasRule(r, "SEC-001"));
    }

    // ── Security: Weak Crypto ──

    static void testMd5Usage() {
        SecurityResult r = scanner.scan("t.py", "h = hashlib.md5(data)\n");
        assertTrue("SEC-004 detects MD5", hasRule(r, "SEC-004"));
    }
    static void testSha1Usage() {
        SecurityResult r = scanner.scan("t.py", "h = hashlib.sha1(data)\n");
        assertTrue("SEC-005 detects SHA1", hasRule(r, "SEC-005"));
    }

    // ── Security: Insecure Random ──

    static void testInsecureRandom() {
        SecurityResult r = scanner.scan("t.py", "x = random.randint(1, 100)\n");
        assertTrue("SEC-006 detects random.randint", hasRule(r, "SEC-006"));
    }

    // ── Security: SQL Injection ──

    static void testSqlInjectionFstring() {
        SecurityResult r = scanner.scan("t.py", "cursor.execute(f\"SELECT * FROM users WHERE id = {uid}\")\n");
        assertTrue("SEC-007 detects f-string SQL", r.getTotalFlags() > 0);
    }
    static void testSqlInjectionConcat() {
        SecurityResult r = scanner.scan("t.py", "q = \"SELECT * FROM users WHERE name = '\" + name + \"'\"\n");
        assertTrue("SEC-008 detects concatenated SQL", hasRule(r, "SEC-008"));
    }

    // ── Security: Code Injection ──

    static void testEvalUsage() {
        SecurityResult r = scanner.scan("t.py", "result = eval(user_input)\n");
        assertTrue("SEC-009 detects eval()", hasRule(r, "SEC-009"));
    }
    static void testExecUsage() {
        SecurityResult r = scanner.scan("t.py", "exec(code_string)\n");
        assertTrue("SEC-010 detects exec()", hasRule(r, "SEC-010"));
    }

    // ── Security: Unsafe Deserialization ──

    static void testPickleLoads() {
        SecurityResult r = scanner.scan("t.py", "obj = pickle.loads(data)\n");
        assertTrue("SEC-011 detects pickle.loads", hasRule(r, "SEC-011"));
    }

    // ── Security: Shell Injection ──

    static void testSubprocessShellTrue() {
        SecurityResult r = scanner.scan("t.py", "subprocess.call(cmd, shell=True)\n");
        assertTrue("SEC-012 detects shell=True", hasRule(r, "SEC-012"));
    }
    static void testOsSystem() {
        SecurityResult r = scanner.scan("t.py", "os.system(\"ls -la\")\n");
        assertTrue("SEC-013 detects os.system()", hasRule(r, "SEC-013"));
    }

    // ── Security: Insecure Config ──

    static void testDebugMode() {
        SecurityResult r = scanner.scan("t.py", "DEBUG = True\n");
        assertTrue("SEC-014 detects DEBUG=True", hasRule(r, "SEC-014"));
    }
    static void testSslVerifyFalse() {
        SecurityResult r = scanner.scan("t.py", "requests.get(url, verify=False)\n");
        assertTrue("SEC-015 detects verify=False", hasRule(r, "SEC-015"));
    }
    static void testBindAllInterfaces() {
        SecurityResult r = scanner.scan("t.py", "host = '0.0.0.0'\n");
        assertTrue("SEC-016 detects 0.0.0.0 binding", hasRule(r, "SEC-016"));
    }

    // ── Security: Clean Code ──

    static void testCleanCodeNoFlags() {
        SecurityResult r = scanner.scan("t.py", "def add(a, b):\n    return a + b\n");
        assertEquals("Clean code: 0 flags", 0, r.getTotalFlags());
    }
    static void testCommentsIgnored() {
        SecurityResult r = scanner.scan("t.py", "# password = \"just a comment\"\nx = 1\n");
        assertEquals("Comments ignored: 0 flags", 0, r.getTotalFlags());
    }

    // ── Security: Severity Counts ──

    static void testSeverityCounts() {
        SecurityResult r = scanner.scan("t.py", "password = \"secret\"\nDEBUG = True\n");
        int total = r.getCriticalCount() + r.getHighCount() + r.getMediumCount() + r.getLowCount();
        assertEquals("Severity counts sum to total", r.getTotalFlags(), total);
    }

    // ── Vulnerability Density ──

    static void testVdBasic() {
        assertEquals("3 flags / 200 LOC = 15.0", 15.0, VulnerabilityCalculator.calculateDensity(3, 200));
    }
    static void testVdZeroLoc() {
        assertEquals("0 LOC = 0.0 density", 0.0, VulnerabilityCalculator.calculateDensity(5, 0));
    }
    static void testVdZeroFlags() {
        assertEquals("0 flags = 0.0 density", 0.0, VulnerabilityCalculator.calculateDensity(0, 100));
    }
    static void testVdCourseworkAppendixA() {
        assertEquals("Appendix A: 3/200*1000 = 15.0", 15.0, VulnerabilityCalculator.calculateDensity(3, 200));
    }
    static void testVdCourseworkAppendixB() {
        assertEquals("Appendix B: 3/20*1000 = 150.0", 150.0, VulnerabilityCalculator.calculateDensity(3, 20));
    }

    // ── Full Pipeline ──

    static void testFullPipelineWithSecurity() throws IOException {
        String code = String.join("\n",
                "import hashlib",
                "import os",
                "",
                "password = \"admin123\"",
                "DEBUG = True",
                "",
                "def process(data):",
                "    if data:",
                "        result = eval(data)",
                "        return result",
                "    return None",
                "",
                "def run_cmd(cmd):",
                "    os.system(cmd)",
                ""
        );
        ModuleResult r = analyseCode(code);

        assertFalse("Pipeline not skipped", r.isSkipped());
        assertEquals("Pipeline finds 2 functions", 2, r.getFunctions().size());
        assertTrue("Pipeline finds red flags", r.getTotalRedFlags() > 0);
        assertTrue("Pipeline has VD > 0", r.getVulnerabilityDensity() > 0);

        SecurityResult sec = r.getSecurityResult();
        assertTrue("Pipeline detects hardcoded password", hasRule(sec, "SEC-001"));
        assertTrue("Pipeline detects eval()", hasRule(sec, "SEC-009"));
        assertTrue("Pipeline detects os.system()", hasRule(sec, "SEC-013"));
        assertTrue("Pipeline detects DEBUG=True", hasRule(sec, "SEC-014"));
    }

    // ── Helpers ──

    private static SourceReader reader(String code) throws IOException {
        Path f = Files.createTempFile("cs_test_", ".py");
        Files.writeString(f, code);
        SourceReader r = new SourceReader(f.toString());
        r.read();
        Files.delete(f);
        return r;
    }

    private static ModuleResult analyseCode(String code) throws IOException {
        Path f = Files.createTempFile("cs_test_", ".py");
        Files.writeString(f, code);
        ModuleResult r = analyser.analyse(f.toString());
        Files.delete(f);
        return r;
    }

    private static boolean hasRule(SecurityResult r, String ruleId) {
        return r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals(ruleId));
    }

    private static void section(String name) {
        System.out.println("\n  -- " + name + " --");
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if (expected.equals(actual)) { System.out.println("    PASS " + testName); passed++; }
        else { System.out.println("    FAIL " + testName + " (expected=" + expected + ", actual=" + actual + ")"); failed++; }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) { System.out.println("    PASS " + testName); passed++; }
        else { System.out.println("    FAIL " + testName + " (expected true)"); failed++; }
    }

    private static void assertFalse(String testName, boolean condition) {
        if (!condition) { System.out.println("    PASS " + testName); passed++; }
        else { System.out.println("    FAIL " + testName + " (expected false)"); failed++; }
    }
}
