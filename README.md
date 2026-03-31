# CodeShield - Technical Debt and Security Scanner

CodeShield is a static analysis tool built in Java that scans Python source code files. It calculates how complex the code is, finds security vulnerabilities, and combines both into a single Technical Debt Index (TDI) score per file so you know which modules need refactoring first.

---

## Table of Contents

1. What You Need Before Starting
2. Downloading and Setting Up
3. Compiling the Project
4. Running Your First Scan
5. Understanding the Output
6. Saving a Report to JSON
7. Sorting the Report
8. Viewing the Security Rules
9. Running a Before/After Comparison
10. Running the Tests
11. Scanning Your Own Code
12. Troubleshooting
13. How the Metrics Work
14. Project Structure

---

## 1. What You Need Before Starting

You need the Java Development Kit (JDK) version 17 or higher installed on your machine. You need the JDK specifically (not just the JRE) because we use `javac` to compile the source code.

To check if you have it, open a terminal (Command Prompt on Windows, Terminal on Mac/Linux) and type:

```
java --version
```

You should see something like `openjdk 17.0.x` or `openjdk 21.0.x`. If you see an error or a version below 17, you need to install the JDK.

Then check the compiler is available:

```
javac --version
```

If this says `javac not found`, you have the JRE but not the JDK. Install the full JDK from https://adoptium.net or your package manager.

No other software is needed. CodeShield has zero external dependencies — it only uses the Java standard library.

---

## 2. Downloading and Setting Up

Step 1: Download the `codeshield-week7.zip` file.

Step 2: Unzip it to a folder on your computer. You should see this structure:

```
codeshield/
├── pom.xml
├── README.md
├── sample_code/
│   └── payment_service.py
├── sample_code_refactored/
│   └── payment_service.py
└── src/
    └── main/
        └── java/
            └── com/
                └── codeshield/
                    ├── Main.java
                    ├── TestRunner.java
                    └── (packages: model, parser, security, analysis, report, compare)
```

Step 3: Open a terminal and navigate into the unzipped folder:

```
cd path/to/codeshield
```

Make sure you are in the folder that contains `pom.xml` and `src/`. You can check by running `ls` (Mac/Linux) or `dir` (Windows) — you should see `pom.xml`, `README.md`, `sample_code/`, and `src/`.

---

## 3. Compiling the Project

Before you can run CodeShield, you need to compile the Java source code into class files.

Step 1: Create the output directory for compiled classes:

```
mkdir -p target/classes
```

On Windows (Command Prompt), use:

```
mkdir target\classes
```

Step 2: Compile all the source files. Copy and paste this entire command:

On Mac/Linux:
```bash
javac -d target/classes \
  src/main/java/com/codeshield/model/*.java \
  src/main/java/com/codeshield/parser/*.java \
  src/main/java/com/codeshield/security/*.java \
  src/main/java/com/codeshield/analysis/*.java \
  src/main/java/com/codeshield/report/*.java \
  src/main/java/com/codeshield/compare/*.java \
  src/main/java/com/codeshield/Main.java \
  src/main/java/com/codeshield/TestRunner.java
```

On Windows (all on one line):
```
javac -d target/classes src/main/java/com/codeshield/model/*.java src/main/java/com/codeshield/parser/*.java src/main/java/com/codeshield/security/*.java src/main/java/com/codeshield/analysis/*.java src/main/java/com/codeshield/report/*.java src/main/java/com/codeshield/compare/*.java src/main/java/com/codeshield/Main.java src/main/java/com/codeshield/TestRunner.java
```

If the command completes with no output and no errors, compilation was successful. If you see errors, check the Troubleshooting section below.

---

## 4. Running Your First Scan

CodeShield comes with a sample Python file called `payment_service.py` in the `sample_code/` folder. This file intentionally contains security vulnerabilities and complex code for demonstration.

To scan it, run:

```
java -cp target/classes com.codeshield.Main sample_code/
```

You will see output like this:

```
  CodeShield - Technical Debt & Security Scanner v1.0-ITERATION4
  ==================================================
  Found 1 Python file(s) to analyse.

  Analysing: payment_service.py... TDI=80.26 | CC=9 | VD=151.52 | Flags=10 !! HIGH RISK
```

Followed by a full report showing the summary dashboard, high-risk alerts, per-function complexity breakdown, and every security finding with its line number.

You can also scan a single file directly instead of a directory:

```
java -cp target/classes com.codeshield.Main sample_code/payment_service.py
```

---

## 5. Understanding the Output

The scanner produces three sections of output:

**Section 1: Summary Dashboard**

This shows totals across all scanned files:
- Modules analysed / skipped — how many files were processed vs skipped (due to errors)
- Total LOC — total effective lines of code (excluding blanks, comments, docstrings)
- Total red flags — total security issues found across all files
- Average TDI / Max TDI — the average and worst TDI scores
- HIGH RISK modules — count of modules with TDI >= 50

**Section 2: High-Risk Module Alerts**

If any module has a TDI of 50 or above, it appears here with its score. These are the modules that should be refactored first.

**Section 3: Per-Module Detail**

For each scanned file, you see:
- LOC, number of functions, complexity score, vulnerability density, TDI, and risk classification
- A table showing every function with its cyclomatic complexity (CC) and risk level
- A list of every security red flag found, showing the rule ID, severity, rule name, the line number where it was found, the actual code on that line, and the CWE reference

The per-file scan line means:
- **TDI=80.26** — Technical Debt Index. Higher is worse. Anything 50+ is high risk.
- **CC=9** — The highest cyclomatic complexity of any function in the file. Higher means harder to test.
- **VD=151.52** — Vulnerability density. Number of security issues per 1,000 lines of code.
- **Flags=10** — Total number of security red flags found in the file.
- **!! HIGH RISK** — This appears when TDI is 50 or above.

---

## 6. Saving a Report to JSON

To save the full report as a JSON file (needed for before/after comparisons), add the `--json` flag followed by the output filename:

```
java -cp target/classes com.codeshield.Main sample_code/ --json report.json
```

This creates a file called `report.json` in your current directory. The JSON contains all the same data as the console report in a structured format: project name, timestamp, summary statistics, and per-module detail including every function's complexity and every security finding.

You can open the JSON file in any text editor to inspect it.

---

## 7. Sorting the Report

By default, modules are sorted by TDI (highest risk first). You can change this with the `--sort` flag.

Sort by TDI (default):
```
java -cp target/classes com.codeshield.Main sample_code/ --sort tdi
```

Sort by cyclomatic complexity (most complex first):
```
java -cp target/classes com.codeshield.Main sample_code/ --sort complexity
```

Sort by vulnerability density (most vulnerable first):
```
java -cp target/classes com.codeshield.Main sample_code/ --sort vulnerability_density
```

You can combine `--sort` and `--json` together:
```
java -cp target/classes com.codeshield.Main sample_code/ --sort complexity --json report.json
```

---

## 8. Viewing the Security Rules

To see all 16 security detection rules that CodeShield checks for, run:

```
java -cp target/classes com.codeshield.Main rules
```

This lists every rule with its:
- Rule ID (e.g. SEC-001)
- Severity level (Critical, High, Medium)
- Name (e.g. "Hardcoded Password")
- CWE reference (e.g. CWE-798)
- Description of what the rule detects and why it matters

The 16 rules cover five categories:
1. Hardcoded credentials and secrets (SEC-001 to SEC-003)
2. Weak or deprecated cryptography (SEC-004 to SEC-006)
3. SQL injection patterns (SEC-007 to SEC-008)
4. Code and shell injection risks (SEC-009 to SEC-013)
5. Insecure configuration values (SEC-014 to SEC-016)

---

## 9. Running a Before/After Comparison

This is the key feature for demonstrating refactoring improvement. It takes two JSON reports (one from before refactoring, one from after) and shows exactly what improved.

**Step 1:** Scan the original vulnerable code and save the report:

```
java -cp target/classes com.codeshield.Main sample_code/ --json before.json
```

**Step 2:** Scan the refactored code and save the report:

```
java -cp target/classes com.codeshield.Main sample_code_refactored/ --json after.json
```

**Step 3:** Compare the two reports:

```
java -cp target/classes com.codeshield.Main compare before.json after.json
```

**What the comparison shows:**

The output has four sections:

1. **Comparison Summary** — How many modules were compared, added, removed, improved, or worsened. Shows the average TDI delta and the overall trend (Improved / Worsened / Unchanged).

2. **Module Table** — Each module with its before TDI, after TDI, and the delta. A down arrow (v) means the module improved. An up arrow (^) means it got worse.

3. **Detailed Deltas** — Per-module breakdown showing the TDI delta, cyclomatic complexity delta, and vulnerability density delta separately.

4. **Risk Transitions** — Shows modules whose risk classification changed, e.g. "Extremely Complex / High Risk -> Minimal Risk - Acceptable".

The comparison handles three cases for each module:
- **Both reports contain it** — shows the delta (status: OK)
- **Only in the before report** — labelled as REMOVED
- **Only in the after report** — labelled as ADDED

**Expected results with the included sample files:**

```
  Module                       Status     TDI Before  TDI After      Delta
  payment_service.py           OK              80.26       2.50     -77.76 v
```

TDI drops from 80.26 to 2.50 (a reduction of 77.76), complexity drops from 9 to 5, vulnerability density drops from 151.52 to 0.00, and the risk level transitions from High Risk to Minimal Risk.

---

## 10. Running the Tests

CodeShield has two test suites: a built-in test runner (no dependencies needed) and a JUnit 5 suite (requires Maven).

### Option A: Built-in TestRunner (104 tests, no Maven required)

This runs the original test suite using the custom TestRunner class. No additional tools needed — just Java.

```
java -cp target/classes com.codeshield.TestRunner
```

You will see each test print PASS or FAIL, grouped by category:
- LOC Counting (5 tests)
- CFG Construction (5 tests)
- Cyclomatic Complexity (9 tests)
- Risk Classification (4 tests)
- Error Handling (3 tests)
- Security Detection — one section per rule category (19 tests)
- Vulnerability Density (5 tests)
- TDI Calculation (4 tests)
- TDI Risk Classification (6 tests)
- TDI Pipeline Integration (2 tests)
- JSON Export (9 tests)
- Sorting (3 tests)
- Full Pipeline End-to-End (9 tests)
- Before/After Comparison (19 tests)

At the end you should see:
```
  RESULTS: 104 passed, 0 failed, 104 total
```

### Option B: JUnit 5 Test Suite (81 tests, requires Maven)

This runs the full JUnit 5 test suite which includes additional tests for TDI calculation, vulnerability density, security scanner (all 16 rules), report generation, integration/E2E pipeline, and error handling.

You need Maven installed. Check with:
```
mvn --version
```

Then run:
```
mvn clean test
```

Expected output:
```
[INFO] Tests run: 81, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The JUnit 5 tests are located in:
- `src/test/java/com/codeshield/CodeShieldTest.java` — 21 tests (LOC, CFG, complexity, risk, edge cases)
- `src/test/java/com/codeshield/CodeShieldTestSuite.java` — 60 tests (TDI, vulnerability density, security scanner, report generator, integration, error handling)

If any tests fail, check that your `pom.xml` includes the JUnit 5 dependency and the Maven Surefire plugin (see pom.xml for details).

---

## 11. Scanning Your Own Code

To scan your own Python files, just point CodeShield at them instead of the sample directory.

Scan a single file:
```
java -cp target/classes com.codeshield.Main /path/to/your/script.py
```

Scan an entire project folder:
```
java -cp target/classes com.codeshield.Main /path/to/your/python/project/
```

When scanning a directory, CodeShield will:
- Recursively find all `.py` files
- Automatically skip `__pycache__`, `.git`, and `venv` directories
- Skip any non-Python files with a warning
- Analyse each `.py` file and include it in the report

To do a before/after comparison on your own code:
1. Scan the original version: `java -cp target/classes com.codeshield.Main my_project/ --json before.json`
2. Make your refactoring changes to the code
3. Scan the refactored version: `java -cp target/classes com.codeshield.Main my_project/ --json after.json`
4. Compare: `java -cp target/classes com.codeshield.Main compare before.json after.json`

---

## 12. Troubleshooting

**"javac: not found" or "javac is not recognized"**
You have the Java Runtime (JRE) but not the Java Development Kit (JDK). Install JDK 17+ from https://adoptium.net.

**"Error: Could not find or load main class com.codeshield.Main"**
You are not in the correct directory, or compilation failed. Make sure you are in the folder containing `pom.xml` and that you ran the `javac` command from Step 3 without errors.

**"[ERROR] No Python source files found"**
The path you provided does not contain any `.py` files. Check the path is correct and points to a file or directory containing Python source code.

**"SKIPPED (Could not read file: ...)"**
The scanner could not read the file. Check file permissions and that the path is correct.

**Compilation errors mentioning "source option 17"**
Your Java version is below 17. Run `java --version` to check and upgrade if needed.

**Windows: the backslash line continuation does not work**
Put the entire javac command on one line, or use PowerShell with backtick (`) for line continuation instead of backslash.

---

## 13. How the Metrics Work

**Cyclomatic Complexity (CC)**
Measures how many independent paths exist through a function. Calculated using M = D + 1 where D is the number of decision points (if, elif, for, while, except, and, or). A function with no branches has CC = 1. Higher CC means the function is harder to understand, test, and maintain.

Risk thresholds:
- 1-10: Low Risk
- 11-20: Moderate Risk
- 21-50: High Risk
- 51+: Very High Risk

**Vulnerability Density (VD)**
The number of security red flags found per 1,000 lines of code. Calculated as: VD = (red_flags / LOC) x 1000. A file with 3 red flags and 200 LOC has a VD of 15.0. A file with 3 red flags and only 20 LOC has a VD of 150.0.

**Technical Debt Index (TDI)**
Combines complexity and security risk into one score. Calculated as: TDI = (Complexity_Score x 0.5) + (Vulnerability_Density x 0.5). The complexity score is the highest CC of any function in the file. A TDI of 50 or above means the module is high risk and should be prioritised for refactoring.

**Lines of Code (LOC)**
Counts effective lines only. Blank lines, comment-only lines (starting with #), and docstring lines (inside triple quotes) are excluded.

---

## 14. Project Structure

```
codeshield/
├── pom.xml                                  # Maven build config
├── README.md                                # This file
├── sample_code/
│   └── payment_service.py                   # Vulnerable sample (TDI 80.26)
├── sample_code_refactored/
│   └── payment_service.py                   # Fixed sample (TDI 2.50)
└── src/main/java/com/codeshield/
    ├── model/
    │   ├── CFGNode.java                     # Node in control flow graph
    │   ├── CFGEdge.java                     # Edge in control flow graph
    │   ├── ControlFlowGraph.java            # CFG with nodes, edges, decision count
    │   ├── FunctionComplexity.java          # Per-function complexity result
    │   └── ModuleResult.java                # Per-file result (CC + security + TDI)
    ├── parser/
    │   ├── SourceReader.java                # Reads files and counts LOC
    │   └── CFGBuilder.java                  # Builds CFGs from Python source
    ├── security/
    │   ├── DetectionRule.java               # Defines a security rule (regex + CWE)
    │   ├── RedFlag.java                     # A single security finding
    │   ├── SecurityResult.java              # All findings for one file
    │   └── SecurityScanner.java             # 16-rule detection engine
    ├── analysis/
    │   ├── ComplexityAnalyser.java          # Full analysis pipeline
    │   ├── VulnerabilityCalculator.java     # VD = (flags/LOC) x 1000
    │   └── TDICalculator.java              # TDI = (CC x 0.5) + (VD x 0.5)
    ├── report/
    │   └── ReportGenerator.java             # Console dashboard + JSON export
    ├── compare/
    │   ├── ModuleComparison.java            # Per-module before/after delta
    │   ├── JsonReportReader.java            # Parses JSON reports
    │   └── ReportComparator.java            # Compares two reports
    ├── Main.java                            # CLI entry point
    └── TestRunner.java                      # 104 unit tests
    └── src/test/java/com/codeshield/
        ├── CodeShieldTest.java              # 21 JUnit 5 tests (LOC, CFG, complexity)
        └── CodeShieldTestSuite.java         # 60 JUnit 5 tests (TDI, security, reports, E2E)
```

---

## Quick Reference — All Commands

| What you want to do | Command |
|---|---|
| Scan a file | `java -cp target/classes com.codeshield.Main path/to/file.py` |
| Scan a directory | `java -cp target/classes com.codeshield.Main path/to/dir/` |
| Scan and save JSON | `java -cp target/classes com.codeshield.Main path/ --json report.json` |
| Scan sorted by complexity | `java -cp target/classes com.codeshield.Main path/ --sort complexity` |
| Scan sorted by vuln density | `java -cp target/classes com.codeshield.Main path/ --sort vulnerability_density` |
| Compare two reports | `java -cp target/classes com.codeshield.Main compare before.json after.json` |
| List security rules | `java -cp target/classes com.codeshield.Main rules` |
| Run tests (built-in) | `java -cp target/classes com.codeshield.TestRunner` |
| Run tests (JUnit 5) | `mvn clean test` |
