package com.codeshield;

import com.codeshield.analysis.ComplexityAnalyser;
import com.codeshield.model.*;
import com.codeshield.parser.CFGBuilder;
import com.codeshield.parser.SourceReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeShieldTest {

    @TempDir
    Path tempDir;

    private ComplexityAnalyser analyser;

    @BeforeEach
    void setUp() {
        analyser = new ComplexityAnalyser();
    }

    private String writeTempPython(String code) throws IOException {
        Path file = tempDir.resolve("test_module.py");
        Files.writeString(file, code);
        return file.toString();
    }

    @Nested
    @DisplayName("LOC Counting")
    class LocTests {
        @Test void simpleCode() throws IOException {
            SourceReader r = new SourceReader(writeTempPython("x = 1\ny = 2\nz = 3\n"));
            r.read();
            assertEquals(3, r.countLoc());
        }
        @Test void blanksExcluded() throws IOException {
            SourceReader r = new SourceReader(writeTempPython("x = 1\n\n\ny = 2\n"));
            r.read();
            assertEquals(2, r.countLoc());
        }
        @Test void commentsExcluded() throws IOException {
            SourceReader r = new SourceReader(writeTempPython("# c\nx = 1\n# c\ny = 2\n"));
            r.read();
            assertEquals(2, r.countLoc());
        }
        @Test void emptyFile() throws IOException {
            SourceReader r = new SourceReader(writeTempPython(""));
            r.read();
            assertEquals(0, r.countLoc());
        }
    }

    @Nested
    @DisplayName("CFG Construction")
    class CfgTests {
        @Test void detectsSingleFunction() {
            var cfgs = new CFGBuilder().buildAll(List.of("def f():", "    pass"));
            assertEquals(1, cfgs.size());
            assertEquals("f", cfgs.get(0).getFunctionName());
        }
        @Test void detectsMultipleFunctions() {
            var cfgs = new CFGBuilder().buildAll(List.of(
                    "def a():", "    pass", "", "def b():", "    pass"));
            assertEquals(2, cfgs.size());
        }
        @Test void hasEntryAndExit() {
            var cfg = new CFGBuilder().buildAll(List.of("def f():", "    x = 1")).get(0);
            var labels = cfg.getNodes().stream().map(CFGNode::getLabel).toList();
            assertTrue(labels.contains("ENTRY"));
            assertTrue(labels.contains("EXIT"));
        }
        @Test void detectsClassMethods() {
            var cfgs = new CFGBuilder().buildAll(List.of(
                    "class C:", "    def a(self):", "        pass",
                    "    def b(self):", "        pass"));
            assertEquals(2, cfgs.size());
        }
    }

    @Nested
    @DisplayName("Cyclomatic Complexity")
    class ComplexityTests {
        @Test void knownCfgDiamond() {
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
            assertEquals(2, analyser.calculateComplexity(cfg).getCyclomaticComplexity());
        }
        @Test void linearCfg() {
            ControlFlowGraph cfg = new ControlFlowGraph("linear");
            cfg.addNode(new CFGNode(0, "ENTRY", 1));
            cfg.addNode(new CFGNode(1, "EXIT", -1));
            cfg.addEdge(new CFGEdge(0, 1));
            cfg.setDecisionCount(0);
            assertEquals(1, analyser.calculateComplexity(cfg).getCyclomaticComplexity());
        }
        @Test void appendixA() {
            ControlFlowGraph cfg = new ControlFlowGraph("payment");
            for (int i = 0; i < 8; i++) cfg.addNode(new CFGNode(i, "N" + i, i));
            cfg.setDecisionCount(3);
            assertEquals(4, analyser.calculateComplexity(cfg).getCyclomaticComplexity());
        }
        @Test void ifIncreasesCC() throws IOException {
            ModuleResult r = analyser.analyse(writeTempPython(
                    "def f(x):\n    if x > 0:\n        return 1\n    return 0\n"));
            assertTrue(r.getMaxComplexity() > 1);
        }
        @Test void multipleBranches() throws IOException {
            ModuleResult r = analyser.analyse(writeTempPython(
                    "def f(x):\n    if x>10:\n        pass\n    elif x>5:\n        pass\n    elif x>0:\n        pass\n"));
            assertTrue(r.getMaxComplexity() > 2);
        }
        @Test void forLoop() throws IOException {
            ModuleResult r = analyser.analyse(writeTempPython(
                    "def f(xs):\n    for x in xs:\n        print(x)\n"));
            assertTrue(r.getMaxComplexity() > 1);
        }
    }

    @Nested
    @DisplayName("Risk Classification")
    class RiskTests {
        @Test void low()      { assertEquals("Low Risk",       ComplexityAnalyser.classifyRisk(5)); }
        @Test void moderate()  { assertEquals("Moderate Risk",  ComplexityAnalyser.classifyRisk(15)); }
        @Test void high()      { assertEquals("High Risk",      ComplexityAnalyser.classifyRisk(30)); }
        @Test void veryHigh()  { assertEquals("Very High Risk", ComplexityAnalyser.classifyRisk(60)); }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorTests {
        @Test void nonExistentFile() {
            ModuleResult r = analyser.analyse("/nonexistent.py");
            assertTrue(r.isSkipped());
            assertEquals("skipped/unsupported", r.getStatus());
        }
        @Test void noFunctions() throws IOException {
            ModuleResult r = analyser.analyse(writeTempPython("x = 1\n"));
            assertEquals(0, r.getMaxComplexity());
        }
    }

    @Nested
    @DisplayName("Edge Deduplication")
    class EdgeTests {
        @Test void noDuplicates() {
            ControlFlowGraph cfg = new ControlFlowGraph("t");
            cfg.addEdge(new CFGEdge(0, 1));
            cfg.addEdge(new CFGEdge(0, 1));
            assertEquals(1, cfg.getNumEdges());
        }
    }
}
