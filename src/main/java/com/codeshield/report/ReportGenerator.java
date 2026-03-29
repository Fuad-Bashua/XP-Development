package com.codeshield.report;

import com.codeshield.model.FunctionComplexity;
import com.codeshield.model.ModuleResult;
import com.codeshield.security.RedFlag;
import com.codeshield.security.SecurityResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReportGenerator {

    private static final String SEP = "=".repeat(70);
    private static final String THIN = "-".repeat(40);

    public static List<ModuleResult> sort(List<ModuleResult> results, String sortBy) {
        List<ModuleResult> sorted = new ArrayList<>(results);
        Comparator<ModuleResult> c = switch (sortBy) {
            case "complexity" -> Comparator.comparingDouble(ModuleResult::getMaxComplexity).reversed();
            case "vulnerability_density" -> Comparator.comparingDouble(ModuleResult::getVulnerabilityDensity).reversed();
            default -> Comparator.comparingDouble(ModuleResult::getTdi).reversed();
        };
        sorted.sort(c);
        return sorted;
    }

    public static String generateConsoleReport(List<ModuleResult> results, String projectName, String sortBy) {
        List<ModuleResult> sorted = sort(results, sortBy);
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        sb.append("\n").append(SEP).append("\n");
        sb.append("  CODESHIELD ANALYSIS REPORT\n");
        sb.append("  Project: ").append(projectName).append("\n");
        sb.append("  Generated: ").append(timestamp).append("\n");
        sb.append("  Sorted by: ").append(sortBy).append("\n");
        sb.append(SEP).append("\n");

        int analysed = 0, skipped = 0, totalLoc = 0, totalFuncs = 0, totalFlags = 0, highRiskCount = 0;
        double maxTdi = 0;
        for (ModuleResult r : sorted) {
            if (r.isSkipped()) { skipped++; continue; }
            analysed++; totalLoc += r.getTotalLoc(); totalFuncs += r.getFunctions().size();
            totalFlags += r.getTotalRedFlags(); maxTdi = Math.max(maxTdi, r.getTdi());
            if (r.isHighRisk()) highRiskCount++;
        }
        double avgTdi = analysed > 0 ? sorted.stream().filter(r -> !r.isSkipped()).mapToDouble(ModuleResult::getTdi).average().orElse(0) : 0;

        sb.append("\n  SUMMARY DASHBOARD\n  ").append(THIN).append("\n");
        sb.append(String.format("  Modules analysed:    %d%n", analysed));
        sb.append(String.format("  Modules skipped:     %d%n", skipped));
        sb.append(String.format("  Total LOC:           %,d%n", totalLoc));
        sb.append(String.format("  Total functions:     %d%n", totalFuncs));
        sb.append(String.format("  Total red flags:     %d%n", totalFlags));
        sb.append(String.format("  Average TDI:         %.2f%n", avgTdi));
        sb.append(String.format("  Max TDI:             %.2f%n", maxTdi));
        sb.append(String.format("  HIGH RISK modules:   %d%n", highRiskCount));

        List<ModuleResult> highRisk = sorted.stream().filter(ModuleResult::isHighRisk).toList();
        if (!highRisk.isEmpty()) {
            sb.append("\n  !! HIGH RISK MODULES (TDI >= 50)\n  ").append(THIN).append("\n");
            for (ModuleResult hr : highRisk)
                sb.append(String.format("  >> %-40s  TDI: %.2f%n", new File(hr.getFilename()).getName(), hr.getTdi()));
        } else sb.append("\n  No high-risk modules detected.\n");

        sb.append("\n").append(SEP).append("\n  MODULE DETAILS\n").append(SEP).append("\n");
        int num = 1;
        for (ModuleResult r : sorted) {
            String name = new File(r.getFilename()).getName();
            sb.append("\n  [").append(num).append("] ").append(name).append("\n");
            if (r.isSkipped()) {
                sb.append("      Status:               skipped/unsupported\n");
                sb.append("      Reason:               ").append(r.getSkipReason()).append("\n");
                num++; continue;
            }
            sb.append("      Status:               analysed\n");
            sb.append(String.format("      LOC:                  %d%n", r.getTotalLoc()));
            sb.append(String.format("      Functions:            %d%n", r.getFunctions().size()));
            sb.append(String.format("      Complexity Score:     %d%n", r.getMaxComplexity()));
            sb.append(String.format("      Vulnerability Density: %.2f%n", r.getVulnerabilityDensity()));
            sb.append(String.format("      TDI:                  %.2f%n", r.getTdi()));
            sb.append(String.format("      Risk:                 %s%n", r.getRiskClassification()));
            if (!r.getFunctions().isEmpty()) {
                sb.append("      ").append(THIN).append("\n");
                sb.append(String.format("      %-30s %5s   %s%n", "Function", "CC", "Risk"));
                sb.append("      ").append(THIN).append("\n");
                for (FunctionComplexity fc : r.getFunctions())
                    sb.append(String.format("      %-30s %5d   %s%n", fc.getFunctionName().length() > 28 ? fc.getFunctionName().substring(0, 26) + ".." : fc.getFunctionName(), fc.getCyclomaticComplexity(), fc.getRiskLevel()));
            }
            SecurityResult sec = r.getSecurityResult();
            if (sec != null && sec.getTotalFlags() > 0) {
                sb.append("      ").append(THIN).append("\n");
                sb.append(String.format("      Security Findings (%d flags: %d Critical, %d High, %d Medium)%n", sec.getTotalFlags(), sec.getCriticalCount(), sec.getHighCount(), sec.getMediumCount()));
                sb.append("      ").append(THIN).append("\n");
                for (RedFlag rf : sec.getRedFlags()) {
                    sb.append(String.format("      [%-8s] %s - %s (line %d)%n", rf.getSeverity(), rf.getRuleId(), rf.getRuleName(), rf.getLineNumber()));
                    String content = rf.getLineContent(); if (content.length() > 60) content = content.substring(0, 57) + "...";
                    sb.append("                 ").append(content).append("\n");
                    sb.append("                 ").append(rf.getCweReference()).append("\n");
                }
            }
            num++;
        }
        sb.append("\n").append(SEP).append("\n  End of Report\n").append(SEP).append("\n");
        return sb.toString();
    }

    public static String generateJson(List<ModuleResult> results, String projectName, String sortBy) {
        List<ModuleResult> sorted = sort(results, sortBy);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"project_name\": ").append(js(projectName)).append(",\n");
        sb.append("  \"timestamp\": ").append(js(timestamp)).append(",\n");
        sb.append("  \"sorted_by\": ").append(js(sortBy)).append(",\n");
        int analysed = (int) sorted.stream().filter(r -> !r.isSkipped()).count();
        int totalFlags = sorted.stream().filter(r -> !r.isSkipped()).mapToInt(ModuleResult::getTotalRedFlags).sum();
        int hrc = (int) sorted.stream().filter(ModuleResult::isHighRisk).count();
        double maxTdi = sorted.stream().filter(r -> !r.isSkipped()).mapToDouble(ModuleResult::getTdi).max().orElse(0);
        double avgTdi = sorted.stream().filter(r -> !r.isSkipped()).mapToDouble(ModuleResult::getTdi).average().orElse(0);
        sb.append("  \"summary\": {\"total_modules\": ").append(sorted.size()).append(", \"analysed_modules\": ").append(analysed);
        sb.append(", \"high_risk_modules\": ").append(hrc).append(", \"total_red_flags\": ").append(totalFlags);
        sb.append(", \"average_tdi\": ").append(r2(avgTdi)).append(", \"max_tdi\": ").append(r2(maxTdi)).append("},\n");
        sb.append("  \"modules\": [\n");
        for (int i = 0; i < sorted.size(); i++) {
            ModuleResult r = sorted.get(i);
            sb.append("    {\"filename\": ").append(js(r.getFilename())).append(", \"status\": ").append(js(r.getStatus()));
            if (r.isSkipped()) { sb.append(", \"skip_reason\": ").append(js(r.getSkipReason())); }
            else {
                sb.append(", \"loc\": ").append(r.getTotalLoc()).append(", \"num_functions\": ").append(r.getFunctions().size());
                sb.append(", \"complexity_score\": ").append(r.getMaxComplexity()).append(", \"vulnerability_density\": ").append(r.getVulnerabilityDensity());
                sb.append(", \"tdi\": ").append(r.getTdi()).append(", \"risk_classification\": ").append(js(r.getRiskClassification()));
                sb.append(", \"is_high_risk\": ").append(r.isHighRisk());
                sb.append(", \"functions\": [");
                for (int j = 0; j < r.getFunctions().size(); j++) {
                    FunctionComplexity fc = r.getFunctions().get(j);
                    sb.append("{\"name\": ").append(js(fc.getFunctionName())).append(", \"cyclomatic_complexity\": ").append(fc.getCyclomaticComplexity());
                    sb.append(", \"edges\": ").append(fc.getEdges()).append(", \"nodes\": ").append(fc.getNodes());
                    sb.append(", \"risk_level\": ").append(js(fc.getRiskLevel())).append("}");
                    if (j < r.getFunctions().size() - 1) sb.append(", ");
                }
                sb.append("], \"red_flags\": [");
                SecurityResult sec = r.getSecurityResult();
                if (sec != null) {
                    for (int j = 0; j < sec.getRedFlags().size(); j++) {
                        RedFlag rf = sec.getRedFlags().get(j);
                        sb.append("{\"rule_id\": ").append(js(rf.getRuleId())).append(", \"rule_name\": ").append(js(rf.getRuleName()));
                        sb.append(", \"severity\": ").append(js(rf.getSeverity())).append(", \"line_number\": ").append(rf.getLineNumber());
                        sb.append(", \"cwe\": ").append(js(rf.getCweReference())).append("}");
                        if (j < sec.getRedFlags().size() - 1) sb.append(", ");
                    }
                }
                sb.append("]");
            }
            sb.append("}").append(i < sorted.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  ]\n}\n");
        return sb.toString();
    }

    public static void saveJson(List<ModuleResult> results, String projectName, String sortBy, String filepath) throws IOException {
        Files.writeString(Path.of(filepath), generateJson(results, projectName, sortBy));
    }

    private static String js(String v) { return v == null ? "null" : "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
    private static double r2(double v) { return Math.round(v * 100.0) / 100.0; }
}
