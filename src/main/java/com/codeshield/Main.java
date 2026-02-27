package com.codeshield;

import com.codeshield.analysis.ComplexityAnalyser;
import com.codeshield.model.FunctionComplexity;
import com.codeshield.model.ModuleResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    private static final String VERSION = "1.0-ITERATION1";
    private static final String SEPARATOR = "=".repeat(70);
    private static final String THIN_SEP = "-".repeat(40);

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String path = args[0];
        List<String> files = findPythonFiles(path);

        if (files.isEmpty()) {
            System.out.println("  [ERROR] No Python source files found at: " + path);
            System.exit(1);
        }

        System.out.println();
        System.out.println("  CodeShield - Technical Debt & Security Scanner v" + VERSION);
        System.out.println("  " + SEPARATOR);
        System.out.println("  Found " + files.size() + " Python file(s) to analyse.");
        System.out.println();

        ComplexityAnalyser analyser = new ComplexityAnalyser();
        List<ModuleResult> results = new ArrayList<>();

        for (String filepath : files) {
            String basename = new File(filepath).getName();
            System.out.print("  Analysing: " + basename + "... ");

            ModuleResult result = analyser.analyse(filepath);
            results.add(result);

            if (result.isSkipped()) {
                System.out.println("SKIPPED (" + result.getSkipReason() + ")");
            } else {
                System.out.println("CC_max=" + result.getMaxComplexity()
                        + " | Functions=" + result.getFunctions().size()
                        + " | LOC=" + result.getTotalLoc());
            }
        }

        printReport(results, path);
    }

    private static void printReport(List<ModuleResult> results, String projectPath) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  CODESHIELD COMPLEXITY REPORT");
        System.out.println("  Project: " + new File(projectPath).getName());
        System.out.println(SEPARATOR);

        int analysedCount = 0;
        int skippedCount = 0;
        int totalLoc = 0;
        int totalFunctions = 0;
        int overallMaxCC = 0;

        for (ModuleResult r : results) {
            if (r.isSkipped()) {
                skippedCount++;
            } else {
                analysedCount++;
                totalLoc += r.getTotalLoc();
                totalFunctions += r.getFunctions().size();
                overallMaxCC = Math.max(overallMaxCC, r.getMaxComplexity());
            }
        }

        System.out.println();
        System.out.println("  SUMMARY");
        System.out.println("  " + THIN_SEP);
        System.out.println("  Modules analysed:   " + analysedCount);
        System.out.println("  Modules skipped:    " + skippedCount);
        System.out.println("  Total LOC:          " + totalLoc);
        System.out.println("  Total functions:    " + totalFunctions);
        System.out.println("  Overall max CC:     " + overallMaxCC
                + " [" + ComplexityAnalyser.classifyRisk(overallMaxCC) + "]");

        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  MODULE DETAILS");
        System.out.println(SEPARATOR);

        int moduleNum = 1;
        for (ModuleResult r : results) {
            String basename = new File(r.getFilename()).getName();
            System.out.println();
            System.out.println("  [" + moduleNum + "] " + basename);

            if (r.isSkipped()) {
                System.out.println("      Status:    skipped/unsupported");
                System.out.println("      Reason:    " + r.getSkipReason());
                moduleNum++;
                continue;
            }

            System.out.println("      Status:    analysed");
            System.out.println("      LOC:       " + r.getTotalLoc());
            System.out.println("      Functions: " + r.getFunctions().size());
            System.out.println("      Max CC:    " + r.getMaxComplexity());
            System.out.println("      Avg CC:    " + r.getAverageComplexity());
            System.out.println("      Total CC:  " + r.getTotalComplexity());

            System.out.println("      " + THIN_SEP);
            System.out.printf("      %-30s %5s   %s%n", "Function", "CC", "Risk Level");
            System.out.println("      " + THIN_SEP);

            for (FunctionComplexity fc : r.getFunctions()) {
                System.out.printf("      %-30s %5d   %s%n",
                        truncate(fc.getFunctionName(), 28),
                        fc.getCyclomaticComplexity(),
                        fc.getRiskLevel());
                System.out.printf("        (E=%d, N=%d, P=%d)%n",
                        fc.getEdges(), fc.getNodes(), fc.getConnectedComponents());
            }

            moduleNum++;
        }

        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  End of Report");
        System.out.println(SEPARATOR);
    }

    private static List<String> findPythonFiles(String path) {
        List<String> pyFiles = new ArrayList<>();
        File file = new File(path);

        if (file.isFile()) {
            if (path.endsWith(".py")) {
                pyFiles.add(path);
            } else {
                System.out.println("  [WARN] Not a Python file: " + path);
            }
            return pyFiles;
        }

        if (file.isDirectory()) {
            try (Stream<Path> stream = Files.walk(file.toPath())) {
                stream.filter(p -> p.toString().endsWith(".py"))
                      .filter(p -> {
                          String s = p.toString();
                          return !s.contains("__pycache__")
                                  && !s.contains(".git")
                                  && !s.contains("venv");
                      })
                      .sorted()
                      .forEach(p -> pyFiles.add(p.toString()));
            } catch (IOException e) {
                System.out.println("  [ERROR] Could not scan directory: " + e.getMessage());
            }
        }

        return pyFiles;
    }

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 2) + "..";
    }

    private static void printUsage() {
        System.out.println("  Usage: java com.codeshield.Main <file-or-directory>");
        System.out.println();
        System.out.println("  Analyses Python source files for cyclomatic complexity.");
        System.out.println("  Accepts a .py file or a directory (scanned recursively).");
    }
}
