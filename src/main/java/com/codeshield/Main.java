package com.codeshield;

import com.codeshield.analysis.ComplexityAnalyser;
import com.codeshield.model.ModuleResult;
import com.codeshield.report.ReportGenerator;
import com.codeshield.security.DetectionRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    private static final String VERSION = "1.0-ITERATION3";

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        if (args[0].equals("rules")) {
            printRules();
            return;
        }

        String path = args[0];
        String sortBy = "tdi";
        String jsonOutput = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--sort") && i + 1 < args.length) {
                sortBy = args[++i];
            } else if (args[i].equals("--json") && i + 1 < args.length) {
                jsonOutput = args[++i];
            }
        }

        List<String> files = findPythonFiles(path);
        if (files.isEmpty()) {
            System.out.println("  [ERROR] No Python source files found at: " + path);
            System.exit(1);
        }

        System.out.println();
        System.out.println("  CodeShield - Technical Debt & Security Scanner v" + VERSION);
        System.out.println("  " + "=".repeat(50));
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
                String flag = result.isHighRisk() ? " !! HIGH RISK" : "";
                System.out.printf("TDI=%.2f | CC=%d | VD=%.2f | Flags=%d%s%n",
                        result.getTdi(), result.getMaxComplexity(),
                        result.getVulnerabilityDensity(), result.getTotalRedFlags(), flag);
            }
        }

        String projectName = new File(new File(path).getAbsolutePath()).getName();
        String report = ReportGenerator.generateConsoleReport(results, projectName, sortBy);
        System.out.println(report);

        if (jsonOutput != null) {
            try {
                ReportGenerator.saveJson(results, projectName, sortBy, jsonOutput);
                System.out.println("  JSON report saved to: " + jsonOutput);
            } catch (IOException e) {
                System.out.println("  [ERROR] Could not save JSON: " + e.getMessage());
            }
        }
    }

    private static void printRules() {
        ComplexityAnalyser analyser = new ComplexityAnalyser();
        List<DetectionRule> rules = analyser.getSecurityScanner().getRules();

        System.out.println();
        System.out.println("  CodeShield - Active Security Detection Rules");
        System.out.println("  " + "=".repeat(50));
        System.out.println("  " + rules.size() + " rules active:");
        System.out.println();

        for (DetectionRule r : rules) {
            System.out.printf("  %-8s [%-8s] %s%n", r.getRuleId(), r.getSeverity(), r.getName());
            String desc = r.getDescription();
            if (desc.length() > 55) desc = desc.substring(0, 53) + "..";
            System.out.println("           " + r.getCweReference() + " - " + desc);
            System.out.println();
        }
    }

    private static List<String> findPythonFiles(String path) {
        List<String> pyFiles = new ArrayList<>();
        File file = new File(path);

        if (file.isFile()) {
            if (path.endsWith(".py")) pyFiles.add(path);
            else System.out.println("  [WARN] Not a Python file: " + path);
            return pyFiles;
        }

        if (file.isDirectory()) {
            try (Stream<Path> stream = Files.walk(file.toPath())) {
                stream.filter(p -> p.toString().endsWith(".py"))
                      .filter(p -> {
                          String s = p.toString();
                          return !s.contains("__pycache__") && !s.contains(".git") && !s.contains("venv");
                      })
                      .sorted()
                      .forEach(p -> pyFiles.add(p.toString()));
            } catch (IOException e) {
                System.out.println("  [ERROR] Could not scan directory: " + e.getMessage());
            }
        }

        return pyFiles;
    }

    private static void printUsage() {
        System.out.println("  Usage:");
        System.out.println("    java com.codeshield.Main <path> [--sort tdi|complexity|vulnerability_density] [--json output.json]");
        System.out.println("    java com.codeshield.Main rules");
    }
}
