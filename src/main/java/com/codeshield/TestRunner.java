package com.codeshield;

import com.codeshield.analysis.ComplexityAnalyser;
import com.codeshield.analysis.TDICalculator;
import com.codeshield.analysis.VulnerabilityCalculator;
import com.codeshield.compare.ReportComparator;
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

    private static int passed = 0, failed = 0;
    private static final ComplexityAnalyser analyser = new ComplexityAnalyser();
    private static final SecurityScanner scanner = new SecurityScanner();

    public static void main(String[] args) throws IOException {
        System.out.println("\n  CodeShield Test Runner - Iterations 1, 2, 3 & 4\n  " + "=".repeat(50));

        section("LOC Counting");
        testLocSimple(); testLocBlanksExcluded(); testLocCommentsExcluded(); testLocEmpty(); testLocDocstrings();
        section("CFG Construction");
        testSingleFunction(); testMultipleFunctions(); testEntryAndExit(); testClassMethods(); testNoFunctions();
        section("Cyclomatic Complexity (M = D + 1)");
        testKnownCFGDiamond(); testLinearCFG(); testAppendixA(); testSimpleFunctionCC(); testIfIncreasesCC(); testMultipleBranches(); testForLoop(); testWhileLoop(); testMinimumCC();
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
        testVdBasic(); testVdZeroLoc(); testVdZeroFlags(); testVdCourseworkAppendixA(); testVdCourseworkAppendixB();
        section("TDI Calculation");
        testTdiBasic(); testTdiAppendixA(); testTdiAppendixB(); testTdiZeroAll();
        section("TDI Risk Classification");
        testTdiHighRisk(); testTdiModerateRisk(); testTdiLowRisk(); testTdiMinimalRisk(); testTdiThresholdBoundary();
        section("TDI Pipeline Integration");
        testPipelineTdiComputed(); testPipelineHighRiskFlagged();
        section("Reporting: JSON Export");
        testJsonContainsProjectName(); testJsonContainsTdi(); testJsonContainsSummary();
        section("Reporting: Sorting");
        testSortByTdi(); testSortByComplexity(); testSortByVd();
        section("Full Pipeline End-to-End");
        testFullPipelineWithSecurity();
        section("Comparison: Basic Delta");
        testCompareBasicDelta();
        section("Comparison: Module Added");
        testCompareModuleAdded();
        section("Comparison: Module Removed");
        testCompareModuleRemoved();
        section("Comparison: Mixed (Added + Removed + Changed)");
        testCompareMixed();
        section("Comparison: Improvement Detected");
        testCompareImprovementTrend();
        section("Comparison: Format Output");
        testCompareFormatContainsSummary();

        System.out.println("\n  " + "=".repeat(50));
        System.out.printf("  RESULTS: %d passed, %d failed, %d total%n", passed, failed, passed + failed);
        System.out.println("  " + "=".repeat(50));
        if (failed > 0) System.exit(1);
    }

    static void testLocSimple() throws IOException { assertEquals("Simple LOC", 3, reader("x = 1\ny = 2\nz = 3\n").countLoc()); }
    static void testLocBlanksExcluded() throws IOException { assertEquals("Blanks excluded", 2, reader("x = 1\n\n\ny = 2\n").countLoc()); }
    static void testLocCommentsExcluded() throws IOException { assertEquals("Comments excluded", 2, reader("# c\nx = 1\n# c\ny = 2\n").countLoc()); }
    static void testLocEmpty() throws IOException { assertEquals("Empty file", 0, reader("").countLoc()); }
    static void testLocDocstrings() throws IOException { assertEquals("Docstrings excluded", 2, reader("def f():\n    \"\"\"Doc.\"\"\"\n    return 1\n").countLoc()); }

    static void testSingleFunction() { assertEquals("Single func", 1, new CFGBuilder().buildAll(List.of("def f():", "    pass")).size()); }
    static void testMultipleFunctions() { assertEquals("Three funcs", 3, new CFGBuilder().buildAll(List.of("def a():", "    pass", "", "def b():", "    pass", "", "def c():", "    pass")).size()); }
    static void testEntryAndExit() { var l = new CFGBuilder().buildAll(List.of("def f():", "    x = 1")).get(0).getNodes().stream().map(CFGNode::getLabel).toList(); assertTrue("ENTRY", l.contains("ENTRY")); assertTrue("EXIT", l.contains("EXIT")); }
    static void testClassMethods() { assertEquals("Class methods", 2, new CFGBuilder().buildAll(List.of("class C:", "    def a(self):", "        pass", "    def b(self):", "        pass")).size()); }
    static void testNoFunctions() { assertEquals("No funcs", 0, new CFGBuilder().buildAll(List.of("x = 1")).size()); }

    static void testKnownCFGDiamond() { ControlFlowGraph c = makeCfg("t", 4, 1); c.addEdge(new CFGEdge(0,1)); c.addEdge(new CFGEdge(1,2)); c.addEdge(new CFGEdge(1,3)); c.addEdge(new CFGEdge(2,3)); assertEquals("Diamond M=2", 2, analyser.calculateComplexity(c).getCyclomaticComplexity()); }
    static void testLinearCFG() { ControlFlowGraph c = makeCfg("t", 3, 0); c.addEdge(new CFGEdge(0,1)); c.addEdge(new CFGEdge(1,2)); assertEquals("Linear M=1", 1, analyser.calculateComplexity(c).getCyclomaticComplexity()); }
    static void testAppendixA() { assertEquals("Appendix A M=4", 4, analyser.calculateComplexity(makeCfg("t", 8, 3)).getCyclomaticComplexity()); }
    static void testSimpleFunctionCC() throws IOException { assertTrue("Simple CC>=1", analyseCode("def f():\n    return 1\n").getMaxComplexity() >= 1); }
    static void testIfIncreasesCC() throws IOException { assertTrue("If CC>1", analyseCode("def f(x):\n    if x>0:\n        return 1\n    return 0\n").getMaxComplexity() > 1); }
    static void testMultipleBranches() throws IOException { assertTrue("Branches CC>2", analyseCode("def f(x):\n    if x>10:\n        pass\n    elif x>5:\n        pass\n    elif x>0:\n        pass\n").getMaxComplexity() > 2); }
    static void testForLoop() throws IOException { assertTrue("For CC>1", analyseCode("def f(xs):\n    for x in xs:\n        print(x)\n").getMaxComplexity() > 1); }
    static void testWhileLoop() throws IOException { assertTrue("While CC>1", analyseCode("def f():\n    while True:\n        pass\n").getMaxComplexity() > 1); }
    static void testMinimumCC() { assertTrue("Min CC>=1", analyser.calculateComplexity(makeCfg("t", 2, 0)).getCyclomaticComplexity() >= 1); }

    static void testRiskLow() { assertEquals("CC=5", "Low Risk", ComplexityAnalyser.classifyRisk(5)); }
    static void testRiskModerate() { assertEquals("CC=15", "Moderate Risk", ComplexityAnalyser.classifyRisk(15)); }
    static void testRiskHigh() { assertEquals("CC=30", "High Risk", ComplexityAnalyser.classifyRisk(30)); }
    static void testRiskVeryHigh() { assertEquals("CC=60", "Very High Risk", ComplexityAnalyser.classifyRisk(60)); }

    static void testNonExistentFile() { assertTrue("Missing skipped", analyser.analyse("/no.py").isSkipped()); }
    static void testNoFunctionsModule() throws IOException { assertEquals("No funcs CC=0", 0, analyseCode("x=1\n").getMaxComplexity()); }
    static void testSourceReaderError() { assertTrue("Read fails", !new SourceReader("/no.py").read()); }

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

    static void testVdBasic() { assertEquals("VD 3/200", 15.0, VulnerabilityCalculator.calculateDensity(3, 200)); }
    static void testVdZeroLoc() { assertEquals("VD 0 LOC", 0.0, VulnerabilityCalculator.calculateDensity(5, 0)); }
    static void testVdZeroFlags() { assertEquals("VD 0 flags", 0.0, VulnerabilityCalculator.calculateDensity(0, 100)); }
    static void testVdCourseworkAppendixA() { assertEquals("VD App A", 15.0, VulnerabilityCalculator.calculateDensity(3, 200)); }
    static void testVdCourseworkAppendixB() { assertEquals("VD App B", 150.0, VulnerabilityCalculator.calculateDensity(3, 20)); }

    static void testTdiBasic() { assertEquals("TDI (4,15)=9.5", 9.5, TDICalculator.calculate(4, 15.0)); }
    static void testTdiAppendixA() { assertEquals("TDI App A", 9.5, TDICalculator.calculate(4, 15.0)); }
    static void testTdiAppendixB() { assertEquals("TDI App B", 78.5, TDICalculator.calculate(7, 150.0)); }
    static void testTdiZeroAll() { assertEquals("TDI (0,0)=0", 0.0, TDICalculator.calculate(0, 0)); }
    static void testTdiHighRisk() { assertTrue("TDI 78.5 high", TDICalculator.classifyRisk(78.5).contains("High Risk")); }
    static void testTdiModerateRisk() { assertTrue("TDI 35 mod", TDICalculator.classifyRisk(35).contains("Moderate")); }
    static void testTdiLowRisk() { assertTrue("TDI 15 low", TDICalculator.classifyRisk(15).contains("Low")); }
    static void testTdiMinimalRisk() { assertTrue("TDI 5 min", TDICalculator.classifyRisk(5).contains("Minimal")); }
    static void testTdiThresholdBoundary() { assertTrue("TDI 50 high", TDICalculator.isHighRisk(50.0)); assertFalse("TDI 49.9 not", TDICalculator.isHighRisk(49.9)); }
    static void testPipelineTdiComputed() throws IOException { ModuleResult r = analyseCode("import os\npassword = \"abc\"\ndef f():\n    os.system(\"ls\")\n"); assertTrue("TDI>0", r.getTdi() > 0); assertNotNull("Risk set", r.getRiskClassification()); }
    static void testPipelineHighRiskFlagged() throws IOException { assertTrue("High TDI", analyseCode("password=\"a\"\ntoken=\"b\"\nsecret=\"c\"\napi_key=\"d\"\ndef f():\n    eval(x)\n    exec(y)\n    os.system(z)\n    hashlib.md5(a)\n").getTdi() > 10); }

    static void testJsonContainsProjectName() throws IOException { String j = ReportGenerator.generateJson(List.of(analyseCode("def f():\n    return 1\n")), "TestProject", "tdi"); assertTrue("JSON project_name", j.contains("\"project_name\"")); assertTrue("JSON TestProject", j.contains("TestProject")); }
    static void testJsonContainsTdi() throws IOException { String j = ReportGenerator.generateJson(List.of(analyseCode("def f():\n    return 1\n")), "t", "tdi"); assertTrue("JSON tdi", j.contains("\"tdi\"")); assertTrue("JSON risk", j.contains("\"risk_classification\"")); assertTrue("JSON high_risk", j.contains("\"is_high_risk\"")); }
    static void testJsonContainsSummary() throws IOException { String j = ReportGenerator.generateJson(List.of(analyseCode("def f():\n    return 1\n")), "t", "tdi"); assertTrue("JSON summary", j.contains("\"summary\"")); assertTrue("JSON avg_tdi", j.contains("\"average_tdi\"")); assertTrue("JSON max_tdi", j.contains("\"max_tdi\"")); assertTrue("JSON modules", j.contains("\"modules\"")); }
    static void testSortByTdi() { assertEquals("TDI sort", "high.py", ReportGenerator.sort(List.of(makeResult("low.py", 2, 0, 1.0), makeResult("high.py", 10, 100, 55.0)), "tdi").get(0).getFilename()); }
    static void testSortByComplexity() { assertEquals("CC sort", "complex.py", ReportGenerator.sort(List.of(makeResult("simple.py", 2, 0, 1.0), makeResult("complex.py", 25, 0, 12.5)), "complexity").get(0).getFilename()); }
    static void testSortByVd() { assertEquals("VD sort", "vuln.py", ReportGenerator.sort(List.of(makeResult("safe.py", 1, 0, 0.5), makeResult("vuln.py", 1, 200, 100.5)), "vulnerability_density").get(0).getFilename()); }

    static void testFullPipelineWithSecurity() throws IOException {
        ModuleResult r = analyseCode("import hashlib\nimport os\n\npassword = \"admin123\"\nDEBUG = True\n\ndef process(data):\n    if data:\n        result = eval(data)\n        return result\n    return None\n\ndef run_cmd(cmd):\n    os.system(cmd)\n");
        assertFalse("Not skipped", r.isSkipped()); assertEquals("2 funcs", 2, r.getFunctions().size());
        assertTrue("Has flags", r.getTotalRedFlags() > 0); assertTrue("VD>0", r.getVulnerabilityDensity() > 0); assertTrue("TDI>0", r.getTdi() > 0);
        assertNotNull("Risk set", r.getRiskClassification());
        assertTrue("SEC-001", hasRule(r.getSecurityResult(), "SEC-001")); assertTrue("SEC-009", hasRule(r.getSecurityResult(), "SEC-009")); assertTrue("SEC-013", hasRule(r.getSecurityResult(), "SEC-013"));
    }

    static void testCompareBasicDelta() throws IOException {
        Path bf = writeJson(makeJsonReport("test", "module_a.py", 60.0, 10, 110, "High Risk"));
        Path af = writeJson(makeJsonReport("test", "module_a.py", 25.0, 5, 45, "Low Risk"));
        ReportComparator c = new ReportComparator(); c.compare(bf.toString(), af.toString()); Files.delete(bf); Files.delete(af);
        assertEquals("1 compared", 1, c.getModulesCompared()); assertEquals("Status", "compared", c.getComparisons().get(0).getStatus());
        assertTrue("Delta neg", c.getComparisons().get(0).getTdiDelta() < 0); assertTrue("Improved", c.getComparisons().get(0).isImproved());
    }
    static void testCompareModuleAdded() throws IOException {
        Path bf = writeJson("{\"project_name\":\"test\",\"timestamp\":\"t1\",\"modules\":[]}");
        Path af = writeJson(makeJsonReport("test", "new.py", 10.0, 3, 14, "Low"));
        ReportComparator c = new ReportComparator(); c.compare(bf.toString(), af.toString()); Files.delete(bf); Files.delete(af);
        assertEquals("1 added", 1, c.getModulesAdded()); assertEquals("Status added", "added", c.getComparisons().get(0).getStatus());
    }
    static void testCompareModuleRemoved() throws IOException {
        Path bf = writeJson(makeJsonReport("test", "old.py", 40.0, 8, 72, "Moderate"));
        Path af = writeJson("{\"project_name\":\"test\",\"timestamp\":\"t2\",\"modules\":[]}");
        ReportComparator c = new ReportComparator(); c.compare(bf.toString(), af.toString()); Files.delete(bf); Files.delete(af);
        assertEquals("1 removed", 1, c.getModulesRemoved()); assertEquals("Status removed", "removed", c.getComparisons().get(0).getStatus());
    }
    static void testCompareMixed() throws IOException {
        Path bf = writeJson("{\"project_name\":\"test\",\"timestamp\":\"t1\",\"modules\":[{\"filename\":\"kept.py\",\"status\":\"analysed\",\"tdi\":50.0,\"complexity_score\":10,\"vulnerability_density\":80,\"risk_classification\":\"High\",\"is_high_risk\":true},{\"filename\":\"removed.py\",\"status\":\"analysed\",\"tdi\":30.0,\"complexity_score\":5,\"vulnerability_density\":50,\"risk_classification\":\"Moderate\",\"is_high_risk\":false}]}");
        Path af = writeJson("{\"project_name\":\"test\",\"timestamp\":\"t2\",\"modules\":[{\"filename\":\"kept.py\",\"status\":\"analysed\",\"tdi\":20.0,\"complexity_score\":4,\"vulnerability_density\":36,\"risk_classification\":\"Low\",\"is_high_risk\":false},{\"filename\":\"added.py\",\"status\":\"analysed\",\"tdi\":5.0,\"complexity_score\":2,\"vulnerability_density\":8,\"risk_classification\":\"Minimal\",\"is_high_risk\":false}]}");
        ReportComparator c = new ReportComparator(); c.compare(bf.toString(), af.toString()); Files.delete(bf); Files.delete(af);
        assertEquals("1 compared", 1, c.getModulesCompared()); assertEquals("1 added", 1, c.getModulesAdded()); assertEquals("1 removed", 1, c.getModulesRemoved()); assertEquals("3 total", 3, c.getComparisons().size());
    }
    static void testCompareImprovementTrend() throws IOException {
        Path bf = writeJson(makeJsonReport("test", "a.py", 80.0, 15, 145, "High"));
        Path af = writeJson(makeJsonReport("test", "a.py", 5.0, 3, 7, "Minimal"));
        ReportComparator c = new ReportComparator(); c.compare(bf.toString(), af.toString()); Files.delete(bf); Files.delete(af);
        assertEquals("Trend", "Improved", c.getOverallTrend()); assertEquals("1 improved", 1, c.getModulesImproved()); assertEquals("0 worsened", 0, c.getModulesWorsened());
    }
    static void testCompareFormatContainsSummary() throws IOException {
        Path bf = writeJson(makeJsonReport("test", "a.py", 60.0, 10, 110, "High"));
        Path af = writeJson(makeJsonReport("test", "a.py", 20.0, 4, 36, "Low"));
        ReportComparator c = new ReportComparator(); c.compare(bf.toString(), af.toString()); Files.delete(bf); Files.delete(af);
        String rpt = c.formatReport(); assertTrue("Has COMPARISON", rpt.contains("COMPARISON")); assertTrue("Has SUMMARY", rpt.contains("SUMMARY")); assertTrue("Has trend", rpt.contains("Improved")); assertTrue("Has table", rpt.contains("TDI Before"));
    }

    private static String makeJsonReport(String project, String filename, double tdi, int cc, double vd, String risk) {
        return "{\"project_name\":\"" + project + "\",\"timestamp\":\"2025-01-01\",\"modules\":[{\"filename\":\"" + filename + "\",\"status\":\"analysed\",\"tdi\":" + tdi + ",\"complexity_score\":" + cc + ",\"vulnerability_density\":" + vd + ",\"risk_classification\":\"" + risk + "\",\"is_high_risk\":" + (tdi >= 50) + "}]}";
    }
    private static Path writeJson(String content) throws IOException { Path f = Files.createTempFile("cs_", ".json"); Files.writeString(f, content); return f; }
    private static SourceReader reader(String code) throws IOException { Path f = Files.createTempFile("cs_", ".py"); Files.writeString(f, code); SourceReader r = new SourceReader(f.toString()); r.read(); Files.delete(f); return r; }
    private static ModuleResult analyseCode(String code) throws IOException { Path f = Files.createTempFile("cs_", ".py"); Files.writeString(f, code); ModuleResult r = analyser.analyse(f.toString()); Files.delete(f); return r; }
    private static boolean hasRule(SecurityResult r, String id) { return r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals(id)); }
    private static ControlFlowGraph makeCfg(String name, int nodes, int decisions) { ControlFlowGraph c = new ControlFlowGraph(name); for (int i = 0; i < nodes; i++) c.addNode(new CFGNode(i, "N" + i, i)); c.setDecisionCount(decisions); return c; }
    private static ModuleResult makeResult(String name, int maxCC, double vd, double tdi) { ModuleResult r = new ModuleResult(name); r.setMaxComplexity(maxCC); r.setVulnerabilityDensity(vd); r.setTdi(tdi); return r; }
    private static void section(String s) { System.out.println("\n  -- " + s + " --"); }
    private static void assertEquals(String t, Object e, Object a) { if (e.equals(a)) { System.out.println("    PASS " + t); passed++; } else { System.out.println("    FAIL " + t + " (expected=" + e + ", actual=" + a + ")"); failed++; } }
    private static void assertTrue(String t, boolean c) { if (c) { System.out.println("    PASS " + t); passed++; } else { System.out.println("    FAIL " + t); failed++; } }
    private static void assertFalse(String t, boolean c) { if (!c) { System.out.println("    PASS " + t); passed++; } else { System.out.println("    FAIL " + t); failed++; } }
    private static void assertNotNull(String t, Object o) { if (o != null) { System.out.println("    PASS " + t); passed++; } else { System.out.println("    FAIL " + t + " (null)"); failed++; } }
}
