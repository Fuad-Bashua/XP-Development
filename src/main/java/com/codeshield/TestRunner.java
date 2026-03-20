package com.codeshield;

import com.codeshield.analysis.ComplexityAnalyser;
import com.codeshield.analysis.TDICalculator;
import com.codeshield.analysis.VulnerabilityCalculator;
import com.codeshield.model.*;
import com.codeshield.parser.CFGBuilder;
import com.codeshield.parser.SourceReader;
import com.codeshield.report.ReportGenerator;
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
        System.out.println("\n  CodeShield Test Runner - Iterations 1, 2 & 3");
        System.out.println("  " + "=".repeat(50));

        section("LOC Counting");
        testLocSimple(); testLocBlanksExcluded(); testLocCommentsExcluded();
        testLocEmpty(); testLocDocstrings();

        section("CFG Construction");
        testSingleFunction(); testMultipleFunctions(); testEntryAndExit();
        testClassMethods(); testNoFunctions();

        section("Cyclomatic Complexity (M = D + 1)");
        testKnownCFGDiamond(); testLinearCFG(); testAppendixA();
        testSimpleFunctionCC(); testIfIncreasesCC(); testMultipleBranches();
        testForLoop(); testWhileLoop(); testMinimumCC();

        section("Risk Classification");
        testRiskLow(); testRiskModerate(); testRiskHigh(); testRiskVeryHigh();

        section("Error Handling");
        testNonExistentFile(); testNoFunctionsModule(); testSourceReaderError();

        section("Security: Hardcoded Credentials");
        testHardcodedPassword(); testHardcodedApiKey(); testHardcodedToken();

        section("Security: Weak Crypto");
        testMd5Usage(); testSha1Usage();

        section("Security: Insecure Random");
        testInsecureRandom();

        section("Security: SQL Injection");
        testSqlInjectionFstring(); testSqlInjectionConcat();

        section("Security: Code Injection");
        testEvalUsage(); testExecUsage();

        section("Security: Unsafe Deserialization");
        testPickleLoads();

        section("Security: Shell Injection");
        testSubprocessShellTrue(); testOsSystem();

        section("Security: Insecure Config");
        testDebugMode(); testSslVerifyFalse(); testBindAllInterfaces();

        section("Security: Clean Code");
        testCleanCodeNoFlags(); testCommentsIgnored();

        section("Security: Severity Counts");
        testSeverityCounts();

        section("Vulnerability Density");
        testVdBasic(); testVdZeroLoc(); testVdZeroFlags();
        testVdCourseworkAppendixA(); testVdCourseworkAppendixB();

        section("TDI Calculation");
        testTdiBasic(); testTdiAppendixA(); testTdiAppendixB();
        testTdiZeroAll();

        section("TDI Risk Classification");
        testTdiHighRisk(); testTdiModerateRisk(); testTdiLowRisk(); testTdiMinimalRisk();
        testTdiThresholdBoundary();

        section("TDI Pipeline Integration");
        testPipelineTdiComputed(); testPipelineHighRiskFlagged();

        section("Reporting: JSON Export");
        testJsonContainsProjectName(); testJsonContainsTdi();
        testJsonContainsSummary();

        section("Reporting: Sorting");
        testSortByTdi(); testSortByComplexity(); testSortByVd();

        section("Full Pipeline End-to-End");
        testFullPipelineWithSecurity();

        System.out.println("\n  " + "=".repeat(50));
        System.out.printf("  RESULTS: %d passed, %d failed, %d total%n", passed, failed, passed + failed);
        System.out.println("  " + "=".repeat(50));
        if (failed > 0) System.exit(1);
    }

    // ── LOC ──
    static void testLocSimple() throws IOException { assertEquals("Simple LOC", 3, reader("x = 1\ny = 2\nz = 3\n").countLoc()); }
    static void testLocBlanksExcluded() throws IOException { assertEquals("Blanks excluded", 2, reader("x = 1\n\n\ny = 2\n").countLoc()); }
    static void testLocCommentsExcluded() throws IOException { assertEquals("Comments excluded", 2, reader("# c\nx = 1\n# c\ny = 2\n").countLoc()); }
    static void testLocEmpty() throws IOException { assertEquals("Empty file", 0, reader("").countLoc()); }
    static void testLocDocstrings() throws IOException { assertEquals("Docstrings excluded", 2, reader("def f():\n    \"\"\"Doc.\"\"\"\n    return 1\n").countLoc()); }

    // ── CFG ──
    static void testSingleFunction() { var c = new CFGBuilder().buildAll(List.of("def f():", "    pass")); assertEquals("Single func", 1, c.size()); }
    static void testMultipleFunctions() { var c = new CFGBuilder().buildAll(List.of("def a():", "    pass", "", "def b():", "    pass", "", "def c():", "    pass")); assertEquals("Three funcs", 3, c.size()); }
    static void testEntryAndExit() { var cfg = new CFGBuilder().buildAll(List.of("def f():", "    x = 1")).get(0); var l = cfg.getNodes().stream().map(CFGNode::getLabel).toList(); assertTrue("ENTRY", l.contains("ENTRY")); assertTrue("EXIT", l.contains("EXIT")); }
    static void testClassMethods() { assertEquals("Class methods", 2, new CFGBuilder().buildAll(List.of("class C:", "    def a(self):", "        pass", "    def b(self):", "        pass")).size()); }
    static void testNoFunctions() { assertEquals("No funcs", 0, new CFGBuilder().buildAll(List.of("x = 1")).size()); }

    // ── Complexity ──
    static void testKnownCFGDiamond() { ControlFlowGraph c = makeCfg("t", 4, 1); c.addEdge(new CFGEdge(0,1)); c.addEdge(new CFGEdge(1,2)); c.addEdge(new CFGEdge(1,3)); c.addEdge(new CFGEdge(2,3)); assertEquals("Diamond M=2", 2, analyser.calculateComplexity(c).getCyclomaticComplexity()); }
    static void testLinearCFG() { ControlFlowGraph c = makeCfg("t", 3, 0); c.addEdge(new CFGEdge(0,1)); c.addEdge(new CFGEdge(1,2)); assertEquals("Linear M=1", 1, analyser.calculateComplexity(c).getCyclomaticComplexity()); }
    static void testAppendixA() { ControlFlowGraph c = makeCfg("t", 8, 3); assertEquals("Appendix A M=4", 4, analyser.calculateComplexity(c).getCyclomaticComplexity()); }
    static void testSimpleFunctionCC() throws IOException { assertTrue("Simple CC>=1", analyseCode("def f():\n    return 1\n").getMaxComplexity() >= 1); }
    static void testIfIncreasesCC() throws IOException { assertTrue("If CC>1", analyseCode("def f(x):\n    if x>0:\n        return 1\n    return 0\n").getMaxComplexity() > 1); }
    static void testMultipleBranches() throws IOException { assertTrue("Branches CC>2", analyseCode("def f(x):\n    if x>10:\n        pass\n    elif x>5:\n        pass\n    elif x>0:\n        pass\n").getMaxComplexity() > 2); }
    static void testForLoop() throws IOException { assertTrue("For CC>1", analyseCode("def f(xs):\n    for x in xs:\n        print(x)\n").getMaxComplexity() > 1); }
    static void testWhileLoop() throws IOException { assertTrue("While CC>1", analyseCode("def f():\n    while True:\n        pass\n").getMaxComplexity() > 1); }
    static void testMinimumCC() { ControlFlowGraph c = makeCfg("t", 2, 0); assertTrue("Min CC>=1", analyser.calculateComplexity(c).getCyclomaticComplexity() >= 1); }

    // ── Risk ──
    static void testRiskLow() { assertEquals("CC=5", "Low Risk", ComplexityAnalyser.classifyRisk(5)); }
    static void testRiskModerate() { assertEquals("CC=15", "Moderate Risk", ComplexityAnalyser.classifyRisk(15)); }
    static void testRiskHigh() { assertEquals("CC=30", "High Risk", ComplexityAnalyser.classifyRisk(30)); }
    static void testRiskVeryHigh() { assertEquals("CC=60", "Very High Risk", ComplexityAnalyser.classifyRisk(60)); }

    // ── Errors ──
    static void testNonExistentFile() { var r = analyser.analyse("/no.py"); assertTrue("Missing skipped", r.isSkipped()); }
    static void testNoFunctionsModule() throws IOException { assertEquals("No funcs CC=0", 0, analyseCode("x=1\n").getMaxComplexity()); }
    static void testSourceReaderError() { var r = new SourceReader("/no.py"); assertTrue("Read fails", !r.read()); }

    // ── Security ──
    static void testHardcodedPassword() { assertTrue("SEC-001 password", hasRule(scanner.scan("t", "password = \"secret\"\n"), "SEC-001")); }
    static void testHardcodedApiKey() { assertTrue("SEC-001 api_key", hasRule(scanner.scan("t", "api_key = \"sk-abc\"\n"), "SEC-001")); }
    static void testHardcodedToken() { assertTrue("SEC-001 token", hasRule(scanner.scan("t", "auth_token = \"xyz\"\n"), "SEC-001")); }
    static void testMd5Usage() { assertTrue("SEC-004 MD5", hasRule(scanner.scan("t", "hashlib.md5(d)\n"), "SEC-004")); }
    static void testSha1Usage() { assertTrue("SEC-005 SHA1", hasRule(scanner.scan("t", "hashlib.sha1(d)\n"), "SEC-005")); }
    static void testInsecureRandom() { assertTrue("SEC-006 random", hasRule(scanner.scan("t", "random.randint(1,9)\n"), "SEC-006")); }
    static void testSqlInjectionFstring() { assertTrue("SEC-007 fstring", scanner.scan("t", "cursor.execute(f\"SELECT {x}\")\n").getTotalFlags() > 0); }
    static void testSqlInjectionConcat() { assertTrue("SEC-008 concat", hasRule(scanner.scan("t", "\"SELECT * FROM t WHERE n='\" + n + \"'\"\n"), "SEC-008")); }
    static void testEvalUsage() { assertTrue("SEC-009 eval", hasRule(scanner.scan("t", "eval(x)\n"), "SEC-009")); }
    static void testExecUsage() { assertTrue("SEC-010 exec", hasRule(scanner.scan("t", "exec(x)\n"), "SEC-010")); }
    static void testPickleLoads() { assertTrue("SEC-011 pickle", hasRule(scanner.scan("t", "pickle.loads(d)\n"), "SEC-011")); }
    static void testSubprocessShellTrue() { assertTrue("SEC-012 shell", hasRule(scanner.scan("t", "subprocess.call(c, shell=True)\n"), "SEC-012")); }
    static void testOsSystem() { assertTrue("SEC-013 os.system", hasRule(scanner.scan("t", "os.system(\"ls\")\n"), "SEC-013")); }
    static void testDebugMode() { assertTrue("SEC-014 debug", hasRule(scanner.scan("t", "DEBUG = True\n"), "SEC-014")); }
    static void testSslVerifyFalse() { assertTrue("SEC-015 ssl", hasRule(scanner.scan("t", "verify=False\n"), "SEC-015")); }
    static void testBindAllInterfaces() { assertTrue("SEC-016 bind", hasRule(scanner.scan("t", "host = '0.0.0.0'\n"), "SEC-016")); }
    static void testCleanCodeNoFlags() { assertEquals("Clean=0", 0, scanner.scan("t", "def f():\n    return 1\n").getTotalFlags()); }
    static void testCommentsIgnored() { assertEquals("Comment=0", 0, scanner.scan("t", "# password = \"x\"\n").getTotalFlags()); }
    static void testSeverityCounts() { var r = scanner.scan("t", "password=\"s\"\nDEBUG=True\n"); assertEquals("Sev sum", r.getTotalFlags(), r.getCriticalCount()+r.getHighCount()+r.getMediumCount()+r.getLowCount()); }

    // ── Vulnerability Density ──
    static void testVdBasic() { assertEquals("VD 3/200", 15.0, VulnerabilityCalculator.calculateDensity(3, 200)); }
    static void testVdZeroLoc() { assertEquals("VD 0 LOC", 0.0, VulnerabilityCalculator.calculateDensity(5, 0)); }
    static void testVdZeroFlags() { assertEquals("VD 0 flags", 0.0, VulnerabilityCalculator.calculateDensity(0, 100)); }
    static void testVdCourseworkAppendixA() { assertEquals("VD App A", 15.0, VulnerabilityCalculator.calculateDensity(3, 200)); }
    static void testVdCourseworkAppendixB() { assertEquals("VD App B", 150.0, VulnerabilityCalculator.calculateDensity(3, 20)); }

    // ── TDI Calculation ──
    static void testTdiBasic() { assertEquals("TDI (4,15)=9.5", 9.5, TDICalculator.calculate(4, 15.0)); }
    static void testTdiAppendixA() { assertEquals("TDI App A: (4,15)=9.5", 9.5, TDICalculator.calculate(4, 15.0)); }
    static void testTdiAppendixB() { assertEquals("TDI App B: (7,150)=78.5", 78.5, TDICalculator.calculate(7, 150.0)); }
    static void testTdiZeroAll() { assertEquals("TDI (0,0)=0", 0.0, TDICalculator.calculate(0, 0)); }

    // ── TDI Risk Classification ──
    static void testTdiHighRisk() { assertTrue("TDI 78.5 high risk", TDICalculator.classifyRisk(78.5).contains("High Risk")); }
    static void testTdiModerateRisk() { assertTrue("TDI 35 moderate", TDICalculator.classifyRisk(35).contains("Moderate")); }
    static void testTdiLowRisk() { assertTrue("TDI 15 low", TDICalculator.classifyRisk(15).contains("Low")); }
    static void testTdiMinimalRisk() { assertTrue("TDI 5 minimal", TDICalculator.classifyRisk(5).contains("Minimal")); }
    static void testTdiThresholdBoundary() {
        assertTrue("TDI 50 is high", TDICalculator.isHighRisk(50.0));
        assertFalse("TDI 49.9 not high", TDICalculator.isHighRisk(49.9));
    }

    // ── TDI Pipeline ──
    static void testPipelineTdiComputed() throws IOException {
        String code = "import os\npassword = \"abc\"\ndef f():\n    os.system(\"ls\")\n";
        ModuleResult r = analyseCode(code);
        assertTrue("TDI > 0 in pipeline", r.getTdi() > 0);
        assertNotNull("Risk set in pipeline", r.getRiskClassification());
    }
    static void testPipelineHighRiskFlagged() throws IOException {
        String code = "password=\"a\"\ntoken=\"b\"\nsecret=\"c\"\napi_key=\"d\"\n"
                + "def f():\n    eval(x)\n    exec(y)\n    os.system(z)\n    hashlib.md5(a)\n";
        ModuleResult r = analyseCode(code);
        assertTrue("Many flags -> high VD -> high TDI", r.getTdi() > 10);
    }

    // ── JSON Export ──
    static void testJsonContainsProjectName() throws IOException {
        List<ModuleResult> results = List.of(analyseCode("def f():\n    return 1\n"));
        String json = ReportGenerator.generateJson(results, "TestProject", "tdi");
        assertTrue("JSON has project_name", json.contains("\"project_name\""));
        assertTrue("JSON has TestProject", json.contains("TestProject"));
    }
    static void testJsonContainsTdi() throws IOException {
        List<ModuleResult> results = List.of(analyseCode("def f():\n    return 1\n"));
        String json = ReportGenerator.generateJson(results, "test", "tdi");
        assertTrue("JSON has tdi field", json.contains("\"tdi\""));
        assertTrue("JSON has risk_classification", json.contains("\"risk_classification\""));
        assertTrue("JSON has is_high_risk", json.contains("\"is_high_risk\""));
    }
    static void testJsonContainsSummary() throws IOException {
        List<ModuleResult> results = List.of(analyseCode("def f():\n    return 1\n"));
        String json = ReportGenerator.generateJson(results, "test", "tdi");
        assertTrue("JSON has summary", json.contains("\"summary\""));
        assertTrue("JSON has average_tdi", json.contains("\"average_tdi\""));
        assertTrue("JSON has max_tdi", json.contains("\"max_tdi\""));
        assertTrue("JSON has modules array", json.contains("\"modules\""));
    }

    // ── Sorting ──
    static void testSortByTdi() {
        ModuleResult low = makeResult("low.py", 2, 0, 1.0);
        ModuleResult high = makeResult("high.py", 10, 100, 55.0);
        List<ModuleResult> sorted = ReportGenerator.sort(List.of(low, high), "tdi");
        assertEquals("TDI sort: high first", "high.py", sorted.get(0).getFilename());
    }
    static void testSortByComplexity() {
        ModuleResult simple = makeResult("simple.py", 2, 0, 1.0);
        ModuleResult complex = makeResult("complex.py", 25, 0, 12.5);
        List<ModuleResult> sorted = ReportGenerator.sort(List.of(simple, complex), "complexity");
        assertEquals("CC sort: complex first", "complex.py", sorted.get(0).getFilename());
    }
    static void testSortByVd() {
        ModuleResult safe = makeResult("safe.py", 1, 0, 0.5);
        ModuleResult vuln = makeResult("vuln.py", 1, 200, 100.5);
        List<ModuleResult> sorted = ReportGenerator.sort(List.of(safe, vuln), "vulnerability_density");
        assertEquals("VD sort: vuln first", "vuln.py", sorted.get(0).getFilename());
    }

    // ── Full Pipeline ──
    static void testFullPipelineWithSecurity() throws IOException {
        String code = "import hashlib\nimport os\n\npassword = \"admin123\"\nDEBUG = True\n\n"
                + "def process(data):\n    if data:\n        result = eval(data)\n        return result\n    return None\n\n"
                + "def run_cmd(cmd):\n    os.system(cmd)\n";
        ModuleResult r = analyseCode(code);
        assertFalse("Pipeline not skipped", r.isSkipped());
        assertEquals("2 functions", 2, r.getFunctions().size());
        assertTrue("Has red flags", r.getTotalRedFlags() > 0);
        assertTrue("VD > 0", r.getVulnerabilityDensity() > 0);
        assertTrue("TDI > 0", r.getTdi() > 0);
        assertNotNull("Risk classification set", r.getRiskClassification());
        assertTrue("SEC-001 found", hasRule(r.getSecurityResult(), "SEC-001"));
        assertTrue("SEC-009 found", hasRule(r.getSecurityResult(), "SEC-009"));
        assertTrue("SEC-013 found", hasRule(r.getSecurityResult(), "SEC-013"));
    }

    // ── Helpers ──

    private static SourceReader reader(String code) throws IOException {
        Path f = Files.createTempFile("cs_", ".py"); Files.writeString(f, code);
        SourceReader r = new SourceReader(f.toString()); r.read(); Files.delete(f); return r;
    }
    private static ModuleResult analyseCode(String code) throws IOException {
        Path f = Files.createTempFile("cs_", ".py"); Files.writeString(f, code);
        ModuleResult r = analyser.analyse(f.toString()); Files.delete(f); return r;
    }
    private static boolean hasRule(SecurityResult r, String id) {
        return r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals(id));
    }
    private static ControlFlowGraph makeCfg(String name, int nodes, int decisions) {
        ControlFlowGraph c = new ControlFlowGraph(name);
        for (int i = 0; i < nodes; i++) c.addNode(new CFGNode(i, "N" + i, i));
        c.setDecisionCount(decisions);
        return c;
    }
    private static ModuleResult makeResult(String name, int maxCC, double vd, double tdi) {
        ModuleResult r = new ModuleResult(name);
        r.setMaxComplexity(maxCC);
        r.setVulnerabilityDensity(vd);
        r.setTdi(tdi);
        return r;
    }
    private static void section(String s) { System.out.println("\n  -- " + s + " --"); }
    private static void assertEquals(String t, Object exp, Object act) {
        if (exp.equals(act)) { System.out.println("    PASS " + t); passed++; }
        else { System.out.println("    FAIL " + t + " (expected=" + exp + ", actual=" + act + ")"); failed++; }
    }
    private static void assertTrue(String t, boolean c) {
        if (c) { System.out.println("    PASS " + t); passed++; }
        else { System.out.println("    FAIL " + t + " (expected true)"); failed++; }
    }
    private static void assertFalse(String t, boolean c) {
        if (!c) { System.out.println("    PASS " + t); passed++; }
        else { System.out.println("    FAIL " + t + " (expected false)"); failed++; }
    }
    private static void assertNotNull(String t, Object o) {
        if (o != null) { System.out.println("    PASS " + t); passed++; }
        else { System.out.println("    FAIL " + t + " (was null)"); failed++; }
    }
}
