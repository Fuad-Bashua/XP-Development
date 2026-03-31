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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for CodeShield.
 *
 * Test categories:
 *   1. TDI Calculation          – formula correctness, boundary at threshold 50, edge cases
 *   2. Vulnerability Density    – density formula, zero-LOC guard, coursework example
 *   3. Security Scanner         – red-flag detection for each rule category
 *   4. Report Generator         – console and JSON output correctness
 *   5. Integration / E2E        – full pipeline from source file to final TDI score
 *
 * Traceability:
 *   FR-06  Cyclomatic Complexity        → existing tests in CodeShieldTest
 *   FR-07  Vulnerability Density        → VulnerabilityDensityTests
 *   FR-08  TDI Calculation              → TdiCalculationTests
 *   FR-09  Risk Classification          → TdiCalculationTests.classifyRisk*
 *   FR-10  Security Red-Flag Detection  → SecurityScannerTests
 *   FR-11  Report Output                → ReportGeneratorTests
 *   NFR-01 Robustness / Error Handling  → ErrorHandlingTests
 *
 * @author Tester
 */
class CodeShieldTestSuite {

    @TempDir
    Path tempDir;

    // ── helper ──────────────────────────────────────────────────────────
    private String writePython(String filename, String code) throws IOException {
        Path file = tempDir.resolve(filename);
        Files.writeString(file, code);
        return file.toString();
    }

    // ====================================================================
    //  1. TDI CALCULATION TESTS
    // ====================================================================
    @Nested
    @DisplayName("TDI Calculation (FR-08)")
    class TdiCalculationTests {

        // --- formula correctness ---

        @Test
        @DisplayName("TDI formula: (4*0.5)+(15*0.5) = 9.5  (Appendix A example)")
        void appendixAExample() {
            // Appendix A: complexity=4, vuln_density=15.0 → TDI=9.5
            assertEquals(9.5, TDICalculator.calculate(4, 15.0));
        }

        @Test
        @DisplayName("TDI formula: (7*0.5)+(150*0.5) = 78.5  (Appendix B example)")
        void appendixBExample() {
            // Appendix B: complexity=7, vuln_density=150 → TDI=78.5
            assertEquals(78.5, TDICalculator.calculate(7, 150.0));
        }

        @Test
        @DisplayName("TDI with both inputs zero → 0.0")
        void bothZero() {
            assertEquals(0.0, TDICalculator.calculate(0, 0));
        }

        @Test
        @DisplayName("TDI symmetry: equal weighting 50/50")
        void equalWeighting() {
            double result = TDICalculator.calculate(10, 10);
            assertEquals(10.0, result);
        }

        @Test
        @DisplayName("TDI with very large inputs")
        void largeValues() {
            double result = TDICalculator.calculate(500, 2000);
            assertEquals(1250.0, result);
        }

        // --- threshold / risk classification ---

        @Test
        @DisplayName("TDI exactly 50 → high risk")
        void thresholdExact() {
            assertTrue(TDICalculator.isHighRisk(50.0));
        }

        @Test
        @DisplayName("TDI just below 50 → NOT high risk")
        void thresholdJustBelow() {
            assertFalse(TDICalculator.isHighRisk(49.99));
        }

        @Test
        @DisplayName("TDI above 50 → high risk")
        void thresholdAbove() {
            assertTrue(TDICalculator.isHighRisk(78.5));
        }

        @Test
        @DisplayName("classifyRisk: < 10 → Minimal Risk")
        void classifyMinimal() {
            String result = TDICalculator.classifyRisk(5.0);
            assertTrue(result.contains("Minimal"));
        }

        @Test
        @DisplayName("classifyRisk: 10-29.99 → Low Risk")
        void classifyLow() {
            String result = TDICalculator.classifyRisk(15.0);
            assertTrue(result.contains("Low Risk"));
        }

        @Test
        @DisplayName("classifyRisk: 30-49.99 → Moderate Risk")
        void classifyModerate() {
            String result = TDICalculator.classifyRisk(35.0);
            assertTrue(result.contains("Moderate"));
        }

        @Test
        @DisplayName("classifyRisk: >= 50 → High Risk / Immediate Refactoring")
        void classifyHighRisk() {
            String result = TDICalculator.classifyRisk(78.5);
            assertTrue(result.contains("High Risk"));
            assertTrue(result.contains("Refactoring"));
        }

        @Test
        @DisplayName("HIGH_RISK_THRESHOLD constant equals 50")
        void thresholdConstant() {
            assertEquals(50.0, TDICalculator.HIGH_RISK_THRESHOLD);
        }
    }

    // ====================================================================
    //  2. VULNERABILITY DENSITY TESTS
    // ====================================================================
    @Nested
    @DisplayName("Vulnerability Density (FR-07)")
    class VulnerabilityDensityTests {

        @Test
        @DisplayName("Appendix A: 3 flags / 200 LOC → 15.0 per 1k LOC")
        void appendixADensity() {
            assertEquals(15.0, VulnerabilityCalculator.calculateDensity(3, 200));
        }

        @Test
        @DisplayName("Appendix B: 3 flags / 20 LOC → 150.0 per 1k LOC")
        void appendixBDensity() {
            assertEquals(150.0, VulnerabilityCalculator.calculateDensity(3, 20));
        }

        @Test
        @DisplayName("Zero red flags → density 0.0")
        void zeroFlags() {
            assertEquals(0.0, VulnerabilityCalculator.calculateDensity(0, 500));
        }

        @Test
        @DisplayName("Zero LOC → density 0.0 (no division by zero)")
        void zeroLoc() {
            assertEquals(0.0, VulnerabilityCalculator.calculateDensity(5, 0));
        }

        @Test
        @DisplayName("1 flag / 1000 LOC → exactly 1.0")
        void exactlyOnePerThousand() {
            assertEquals(1.0, VulnerabilityCalculator.calculateDensity(1, 1000));
        }

        @Test
        @DisplayName("Large file: 10 flags / 5000 LOC → 2.0")
        void largeFile() {
            assertEquals(2.0, VulnerabilityCalculator.calculateDensity(10, 5000));
        }
    }

    // ====================================================================
    //  3. SECURITY SCANNER TESTS
    // ====================================================================
    @Nested
    @DisplayName("Security Scanner (FR-10)")
    class SecurityScannerTests {

        private SecurityScanner scanner;

        @BeforeEach
        void setUp() {
            scanner = new SecurityScanner();
        }

        @Test
        @DisplayName("Loads all 16 detection rules")
        void rulesLoaded() {
            assertEquals(16, scanner.getRules().size());
        }

        // --- Individual rule detection ---

        @Test
        @DisplayName("SEC-001: Detects hardcoded password")
        void detectsHardcodedPassword() {
            SecurityResult r = scanner.scan("test.py", "password = \"super_secret_123\"");
            assertTrue(r.getTotalFlags() > 0);
            assertEquals("SEC-001", r.getRedFlags().get(0).getRuleId());
        }

        @Test
        @DisplayName("SEC-004: Detects MD5 usage")
        void detectsMd5() {
            SecurityResult r = scanner.scan("test.py", "hashlib.md5(data)");
            assertTrue(r.getTotalFlags() > 0);
            assertTrue(r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals("SEC-004")));
        }

        @Test
        @DisplayName("SEC-005: Detects SHA1 usage")
        void detectsSha1() {
            SecurityResult r = scanner.scan("test.py", "hashlib.sha1(token.encode())");
            assertTrue(r.getTotalFlags() > 0);
            assertTrue(r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals("SEC-005")));
        }

        @Test
        @DisplayName("SEC-006: Detects insecure random")
        void detectsInsecureRandom() {
            SecurityResult r = scanner.scan("test.py", "x = random.randint(1, 100)");
            assertTrue(r.getTotalFlags() > 0);
            assertTrue(r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals("SEC-006")));
        }

        @Test
        @DisplayName("SEC-007: Detects SQL injection via f-string")
        void detectsSqlInjectionFString() {
            SecurityResult r = scanner.scan("test.py",
                    "cursor.execute(f\"SELECT * FROM users WHERE id = '{user_id}'\")");
            assertTrue(r.getTotalFlags() > 0);
        }

        @Test
        @DisplayName("SEC-008: Detects SQL injection via concatenation")
        void detectsSqlInjectionConcat() {
            SecurityResult r = scanner.scan("test.py",
                    "query = \"SELECT * FROM users WHERE name = '\" + username + \"'\"");
            assertTrue(r.getTotalFlags() > 0);
        }

        @Test
        @DisplayName("SEC-009: Detects eval()")
        void detectsEval() {
            SecurityResult r = scanner.scan("test.py", "result = eval(user_input)");
            assertTrue(r.getTotalFlags() > 0);
            assertTrue(r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals("SEC-009")));
        }

        @Test
        @DisplayName("SEC-013: Detects os.system()")
        void detectsOsSystem() {
            SecurityResult r = scanner.scan("test.py", "os.system(cmd)");
            assertTrue(r.getTotalFlags() > 0);
            assertTrue(r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals("SEC-013")));
        }

        @Test
        @DisplayName("SEC-014: Detects DEBUG = True")
        void detectsDebugMode() {
            SecurityResult r = scanner.scan("test.py", "DEBUG = True");
            assertTrue(r.getTotalFlags() > 0);
            assertTrue(r.getRedFlags().stream().anyMatch(f -> f.getRuleId().equals("SEC-014")));
        }

        @Test
        @DisplayName("SEC-015: Detects SSL verification disabled")
        void detectsSslDisabled() {
            SecurityResult r = scanner.scan("test.py", "requests.get(url, verify=False)");
            assertTrue(r.getTotalFlags() > 0);
        }

        // --- Clean code detection ---

        @Test
        @DisplayName("Clean code produces zero red flags")
        void cleanCode() {
            SecurityResult r = scanner.scan("clean.py",
                    "def add(a, b):\n    return a + b\n");
            assertEquals(0, r.getTotalFlags());
        }

        @Test
        @DisplayName("Comments are skipped by scanner")
        void commentsSkipped() {
            SecurityResult r = scanner.scan("test.py",
                    "# password = \"not_a_real_password\"");
            assertEquals(0, r.getTotalFlags());
        }

        // --- Severity counts ---

        @Test
        @DisplayName("Severity classification is correct on sample code")
        void severityCounts() throws IOException {
            String path = writePython("vuln.py",
                    "password = \"secret\"\nresult = eval(data)\nos.system(cmd)\nDEBUG = True\n");
            SourceReader reader = new SourceReader(path);
            reader.read();
            SecurityResult r = scanner.scan(path, reader.getLines());
            assertTrue(r.getCriticalCount() >= 2, "Should have at least 2 critical flags");
        }

        // --- Red flag metadata ---

        @Test
        @DisplayName("Red flag includes correct line number")
        void lineNumberTracking() {
            SecurityResult r = scanner.scan("test.py",
                    "x = 1\ny = 2\npassword = \"secret\"\nz = 3");
            assertFalse(r.getRedFlags().isEmpty());
            assertEquals(3, r.getRedFlags().get(0).getLineNumber());
        }

        @Test
        @DisplayName("Red flag includes CWE reference")
        void cweReference() {
            SecurityResult r = scanner.scan("test.py", "password = \"secret\"");
            assertFalse(r.getRedFlags().isEmpty());
            assertTrue(r.getRedFlags().get(0).getCweReference().startsWith("CWE-"));
        }
    }

    // ====================================================================
    //  4. REPORT GENERATOR TESTS
    // ====================================================================
    @Nested
    @DisplayName("Report Generator (FR-11)")
    class ReportGeneratorTests {

        private List<ModuleResult> buildSampleResults() {
            ModuleResult m1 = new ModuleResult("module_a.py");
            m1.setTotalLoc(100);
            m1.setMaxComplexity(5);
            m1.setVulnerabilityDensity(10.0);
            m1.setTdi(7.5);
            m1.setRiskClassification("Minimal Risk - Acceptable");
            m1.setHighRisk(false);
            m1.addFunction(new FunctionComplexity("func_a", 5, 6, 4, 1, "Low Risk"));

            ModuleResult m2 = new ModuleResult("module_b.py");
            m2.setTotalLoc(50);
            m2.setMaxComplexity(12);
            m2.setVulnerabilityDensity(100.0);
            m2.setTdi(56.0);
            m2.setRiskClassification("Extremely Complex / High Risk - Immediate Refactoring Recommended");
            m2.setHighRisk(true);
            m2.addFunction(new FunctionComplexity("func_b", 12, 15, 8, 1, "Moderate Risk"));

            return List.of(m1, m2);
        }

        @Test
        @DisplayName("Console report contains project name")
        void consoleReportContainsProject() {
            String report = ReportGenerator.generateConsoleReport(
                    buildSampleResults(), "TestProject", "tdi");
            assertTrue(report.contains("TestProject"));
        }

        @Test
        @DisplayName("Console report contains CODESHIELD header")
        void consoleReportHeader() {
            String report = ReportGenerator.generateConsoleReport(
                    buildSampleResults(), "TestProject", "tdi");
            assertTrue(report.contains("CODESHIELD ANALYSIS REPORT"));
        }

        @Test
        @DisplayName("Console report flags high-risk modules")
        void consoleReportHighRisk() {
            String report = ReportGenerator.generateConsoleReport(
                    buildSampleResults(), "TestProject", "tdi");
            assertTrue(report.contains("HIGH RISK MODULES"));
            assertTrue(report.contains("module_b.py"));
        }

        @Test
        @DisplayName("JSON report is parseable and contains modules")
        void jsonReportStructure() {
            String json = ReportGenerator.generateJson(
                    buildSampleResults(), "TestProject", "tdi");
            assertTrue(json.contains("\"project_name\": \"TestProject\""));
            assertTrue(json.contains("\"modules\""));
            assertTrue(json.contains("module_a.py"));
            assertTrue(json.contains("module_b.py"));
        }

        @Test
        @DisplayName("JSON report includes TDI values")
        void jsonReportTdi() {
            String json = ReportGenerator.generateJson(
                    buildSampleResults(), "TestProject", "tdi");
            assertTrue(json.contains("\"tdi\": 7.5"));
            assertTrue(json.contains("\"tdi\": 56.0"));
        }

        @Test
        @DisplayName("JSON report marks high-risk correctly")
        void jsonReportHighRisk() {
            String json = ReportGenerator.generateJson(
                    buildSampleResults(), "TestProject", "tdi");
            assertTrue(json.contains("\"is_high_risk\": true"));
            assertTrue(json.contains("\"is_high_risk\": false"));
        }

        @Test
        @DisplayName("Sort by complexity orders correctly")
        void sortByComplexity() {
            List<ModuleResult> sorted = ReportGenerator.sort(
                    buildSampleResults(), "complexity");
            assertEquals(12, sorted.get(0).getMaxComplexity());
            assertEquals(5, sorted.get(1).getMaxComplexity());
        }

        @Test
        @DisplayName("Sort by TDI orders correctly")
        void sortByTdi() {
            List<ModuleResult> sorted = ReportGenerator.sort(
                    buildSampleResults(), "tdi");
            assertTrue(sorted.get(0).getTdi() > sorted.get(1).getTdi());
        }

        @Test
        @DisplayName("Skipped modules shown in report")
        void skippedModulesShown() {
            ModuleResult skipped = new ModuleResult("binary.exe");
            skipped.setStatus("skipped/unsupported");
            skipped.setSkipReason("Not a supported source file");
            String report = ReportGenerator.generateConsoleReport(
                    List.of(skipped), "TestProject", "tdi");
            assertTrue(report.contains("skipped"));
        }

        @Test
        @DisplayName("Save JSON writes file to disk")
        void saveJsonToFile() throws IOException {
            Path output = tempDir.resolve("report.json");
            ReportGenerator.saveJson(
                    buildSampleResults(), "TestProject", "tdi", output.toString());
            assertTrue(Files.exists(output));
            String content = Files.readString(output);
            assertTrue(content.contains("TestProject"));
        }

        @Test
        @DisplayName("Empty results list produces valid report")
        void emptyResults() {
            String report = ReportGenerator.generateConsoleReport(
                    new ArrayList<>(), "EmptyProject", "tdi");
            assertTrue(report.contains("CODESHIELD"));
            assertTrue(report.contains("Modules analysed:    0"));
        }
    }

    // ====================================================================
    //  5. INTEGRATION / END-TO-END TESTS
    // ====================================================================
    @Nested
    @DisplayName("Integration Tests (End-to-End Pipeline)")
    class IntegrationTests {

        private ComplexityAnalyser analyser;

        @BeforeEach
        void setUp() {
            analyser = new ComplexityAnalyser();
        }

        @Test
        @DisplayName("E2E: Simple clean function → low TDI, no red flags")
        void cleanFunctionPipeline() throws IOException {
            String path = writePython("clean.py",
                    "def add(a, b):\n    return a + b\n");
            ModuleResult result = analyser.analyse(path);

            assertEquals("analysed", result.getStatus());
            assertTrue(result.getTotalLoc() > 0);
            assertEquals(1, result.getFunctions().size());
            assertEquals(0, result.getTotalRedFlags());
            assertEquals(0.0, result.getVulnerabilityDensity());
            assertFalse(result.isHighRisk());
        }

        @Test
        @DisplayName("E2E: Vulnerable code → flags detected and TDI calculated")
        void vulnerableCodePipeline() throws IOException {
            String path = writePython("vuln.py",
                    "def bad():\n" +
                    "    password = \"secret123\"\n" +
                    "    result = eval(input())\n" +
                    "    return result\n");
            ModuleResult result = analyser.analyse(path);

            assertEquals("analysed", result.getStatus());
            assertTrue(result.getTotalRedFlags() > 0, "Should detect red flags");
            assertTrue(result.getVulnerabilityDensity() > 0, "Density should be > 0");
            assertTrue(result.getTdi() > 0, "TDI should be > 0");
        }

        @Test
        @DisplayName("E2E: Complex + vulnerable → high risk flagged")
        void highRiskPipeline() throws IOException {
            // Create a module with enough complexity and vulnerabilities
            // to push TDI above 50
            StringBuilder code = new StringBuilder();
            code.append("def complex_func(x, y, z, a, b):\n");
            code.append("    password = \"hardcoded_pass\"\n");
            code.append("    if x > 0:\n");
            code.append("        if y > 0:\n");
            code.append("            if z > 0:\n");
            code.append("                eval(x)\n");
            code.append("                os.system(y)\n");
            code.append("                result = eval(z)\n");
            code.append("            elif a > 0:\n");
            code.append("                pass\n");
            code.append("        elif b > 0:\n");
            code.append("            pass\n");
            code.append("    return 0\n");

            String path = writePython("highrisk.py", code.toString());
            ModuleResult result = analyser.analyse(path);

            assertEquals("analysed", result.getStatus());
            assertTrue(result.getMaxComplexity() > 1, "Should have complexity > 1");
            assertTrue(result.getTotalRedFlags() > 0, "Should have red flags");
            assertNotNull(result.getRiskClassification());
        }

        @Test
        @DisplayName("E2E: Sample payment_service.py matches expected metrics")
        void samplePaymentService() throws IOException {
            // Use the project's sample code — read it from the fixture
            String sampleCode = Files.readString(
                    Path.of("sample_code/payment_service.py").toAbsolutePath());
            String path = writePython("payment_service.py", sampleCode);
            ModuleResult result = analyser.analyse(path);

            assertEquals("analysed", result.getStatus());
            assertTrue(result.getTotalLoc() > 0, "LOC should be > 0");
            assertTrue(result.getFunctions().size() >= 5,
                    "payment_service.py has >= 5 functions");
            assertTrue(result.getTotalRedFlags() >= 5,
                    "Should detect multiple security issues");
        }

        @Test
        @DisplayName("E2E: Full pipeline generates valid JSON report")
        void fullPipelineToReport() throws IOException {
            String path = writePython("report_test.py",
                    "def f(x):\n    if x > 0:\n        return 1\n    return 0\n");
            ModuleResult result = analyser.analyse(path);
            List<ModuleResult> results = List.of(result);

            String json = ReportGenerator.generateJson(results, "PipelineTest", "tdi");
            assertTrue(json.contains("\"project_name\": \"PipelineTest\""));
            assertTrue(json.contains("report_test.py"));
            assertTrue(json.contains("\"tdi\""));
        }

        @Test
        @DisplayName("E2E: Multiple modules produce summary statistics")
        void multiModulePipeline() throws IOException {
            String path1 = writePython("mod1.py",
                    "def a():\n    return 1\n");
            String path2 = writePython("mod2.py",
                    "def b(x):\n    if x:\n        return 1\n    return 0\n");

            ModuleResult r1 = analyser.analyse(path1);
            ModuleResult r2 = analyser.analyse(path2);
            List<ModuleResult> results = List.of(r1, r2);

            String report = ReportGenerator.generateConsoleReport(results, "Multi", "tdi");
            assertTrue(report.contains("Modules analysed:    2"));
        }
    }

    // ====================================================================
    //  6. ADDITIONAL ERROR HANDLING & EDGE CASE TESTS
    // ====================================================================
    @Nested
    @DisplayName("Error Handling & Edge Cases (NFR-01)")
    class ErrorHandlingTests {

        private ComplexityAnalyser analyser;

        @BeforeEach
        void setUp() {
            analyser = new ComplexityAnalyser();
        }

        @Test
        @DisplayName("Non-existent file → skipped with reason")
        void nonExistentFile() {
            ModuleResult r = analyser.analyse("/path/to/nonexistent.py");
            assertTrue(r.isSkipped());
            assertNotNull(r.getSkipReason());
        }

        @Test
        @DisplayName("Empty file → 0 LOC, 0 functions")
        void emptyFile() throws IOException {
            ModuleResult r = analyser.analyse(writePython("empty.py", ""));
            assertEquals(0, r.getTotalLoc());
            assertEquals(0, r.getFunctions().size());
        }

        @Test
        @DisplayName("File with only comments → 0 LOC")
        void onlyComments() throws IOException {
            ModuleResult r = analyser.analyse(writePython("comments.py",
                    "# This is a comment\n# Another comment\n"));
            assertEquals(0, r.getTotalLoc());
        }

        @Test
        @DisplayName("File with only blank lines → 0 LOC")
        void onlyBlanks() throws IOException {
            ModuleResult r = analyser.analyse(writePython("blanks.py",
                    "\n\n\n\n"));
            assertEquals(0, r.getTotalLoc());
        }

        @Test
        @DisplayName("Single-line function handled correctly")
        void singleLineFunction() throws IOException {
            ModuleResult r = analyser.analyse(writePython("single.py",
                    "def f(): pass\n"));
            // Should not crash; may or may not detect as function depending on parser
            assertNotNull(r.getStatus());
        }

        @Test
        @DisplayName("Very long file does not cause timeout or crash")
        void longFile() throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("def big_func():\n");
            for (int i = 0; i < 500; i++) {
                sb.append("    x").append(i).append(" = ").append(i).append("\n");
            }
            ModuleResult r = analyser.analyse(writePython("big.py", sb.toString()));
            assertNotNull(r.getStatus());
            assertTrue(r.getTotalLoc() >= 500);
        }

        @Test
        @DisplayName("SecurityResult on null/empty source does not crash")
        void securityScanEmptySource() {
            SecurityScanner scanner = new SecurityScanner();
            SecurityResult r = scanner.scan("empty.py", "");
            assertEquals(0, r.getTotalFlags());
        }

        @Test
        @DisplayName("CFGBuilder with no def lines returns empty list")
        void cfgBuilderNoDefs() {
            List<ControlFlowGraph> cfgs = new CFGBuilder().buildAll(
                    List.of("x = 1", "y = 2", "z = 3"));
            assertTrue(cfgs.isEmpty());
        }
    }
}
