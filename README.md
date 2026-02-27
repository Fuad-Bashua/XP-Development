# CodeShield - Technical Debt and Security Scanner

## Iteration 1: Scanner Engine & Cyclomatic Complexity

A Java-based static analysis tool that parses Python source code and
calculates cyclomatic complexity per function, per module, with risk classification.

### Quick Start

```bash
# Compile
javac -d target/classes src/main/java/com/codeshield/model/*.java \
  src/main/java/com/codeshield/parser/*.java \
  src/main/java/com/codeshield/analysis/*.java \
  src/main/java/com/codeshield/Main.java \
  src/main/java/com/codeshield/TestRunner.java

# Analyse a file or directory
java -cp target/classes com.codeshield.Main sample_code/

# Run tests
java -cp target/classes com.codeshield.TestRunner
```

Or with Maven (requires internet):
```bash
mvn compile
mvn test
java -cp target/classes com.codeshield.Main sample_code/
```

### Project Structure

```
codeshield/
├── pom.xml                              # Maven build config
├── sample_code/
│   └── payment_service.py               # Sample file for analysis
├── src/
│   ├── main/java/com/codeshield/
│   │   ├── model/
│   │   │   ├── CFGNode.java             # Node in control flow graph
│   │   │   ├── CFGEdge.java             # Edge in control flow graph
│   │   │   ├── ControlFlowGraph.java    # CFG container (nodes, edges, decisions)
│   │   │   ├── FunctionComplexity.java  # Per-function CC result
│   │   │   └── ModuleResult.java        # Per-module aggregate result
│   │   ├── parser/
│   │   │   ├── SourceReader.java        # File I/O and LOC counting
│   │   │   └── CFGBuilder.java          # CFG construction from Python source
│   │   ├── analysis/
│   │   │   └── ComplexityAnalyser.java  # CC calculation (M = D + 1)
│   │   ├── Main.java                    # CLI entry point
│   │   └── TestRunner.java              # Standalone test runner (no JUnit needed)
│   └── test/java/com/codeshield/
│       └── CodeShieldTest.java          # JUnit 5 tests (for Maven)
└── README.md
```

### What Iteration 1 Delivers

| Feature | Status |
|---------|--------|
| Parse Python source files | ✓ |
| Detect function/method definitions | ✓ |
| Build control flow graph per function | ✓ |
| Calculate cyclomatic complexity (M = D + 1) | ✓ |
| Count LOC (excluding blanks, comments, docstrings) | ✓ |
| Risk classification (Low/Moderate/High/Very High) | ✓ |
| Handle unparseable files (skipped/unsupported) | ✓ |
| Console report with summary + per-function detail | ✓ |
| 31 unit tests passing | ✓ |

### Cyclomatic Complexity Formula

Primary method (used for calculation):
```
M = D + 1
```
where D = number of decision points (if, elif, for, while, except, and, or).

This is mathematically equivalent to:
```
M = E - N + 2P
```
Both values are tracked; E, N, P are shown in the report for each function.

### Coming in Later Iterations

- **Iteration 2:** Security red flag detection, vulnerability density
- **Iteration 3:** TDI calculation, reporting dashboard, JSON export
- **Iteration 4:** Before/after comparison for refactoring evidence

### Dependencies

- Java 17+ (standard library only — no external packages for runtime)
- JUnit 5 (test scope only, via Maven)

### Assumptions

- Analyses Python (.py) source files only
- Function detection uses `def` keyword + indentation boundaries
- Decision points: if, elif, for, while, except, and, or
- Each source file = one module
- LOC excludes blank lines, comment lines, and docstring lines
