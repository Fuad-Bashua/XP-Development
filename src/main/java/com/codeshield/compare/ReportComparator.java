package com.codeshield.compare;

import java.io.IOException;
import java.util.*;

public class ReportComparator {

    private String projectName, beforeTimestamp, afterTimestamp;
    private final List<ModuleComparison> comparisons = new ArrayList<>();
    private int modulesCompared, modulesAdded, modulesRemoved, modulesImproved, modulesWorsened;
    private double averageTdiDelta;
    private String overallTrend;

    public void compare(String beforePath, String afterPath) throws IOException {
        JsonReportReader br = new JsonReportReader(), ar = new JsonReportReader();
        br.load(beforePath); ar.load(afterPath);
        projectName = br.getProjectName(); beforeTimestamp = br.getTimestamp(); afterTimestamp = ar.getTimestamp();

        Map<String, Map<String, String>> bm = new LinkedHashMap<>(), am = new LinkedHashMap<>();
        for (Map<String, String> m : br.getModules()) bm.put(JsonReportReader.extractBasename(m.get("filename")), m);
        for (Map<String, String> m : ar.getModules()) am.put(JsonReportReader.extractBasename(m.get("filename")), m);

        Set<String> all = new TreeSet<>(); all.addAll(bm.keySet()); all.addAll(am.keySet());
        double totalDelta = 0; int cc = 0;

        for (String fn : all) {
            Map<String, String> b = bm.get(fn), a = am.get(fn);
            if (b != null && a != null) {
                ModuleComparison mc = new ModuleComparison(fn, "compared");
                double tb = pd(b, "tdi"), ta = pd(a, "tdi");
                mc.setTdiBefore(tb); mc.setTdiAfter(ta); mc.setTdiDelta(r2(ta - tb));
                mc.setComplexityBefore(pd(b, "complexity_score")); mc.setComplexityAfter(pd(a, "complexity_score"));
                mc.setComplexityDelta(r2(pd(a, "complexity_score") - pd(b, "complexity_score")));
                mc.setVulnDensityBefore(pd(b, "vulnerability_density")); mc.setVulnDensityAfter(pd(a, "vulnerability_density"));
                mc.setVulnDensityDelta(r2(pd(a, "vulnerability_density") - pd(b, "vulnerability_density")));
                mc.setRiskBefore(b.getOrDefault("risk_classification", "Unknown"));
                mc.setRiskAfter(a.getOrDefault("risk_classification", "Unknown"));
                mc.setImproved(ta < tb);
                comparisons.add(mc); totalDelta += mc.getTdiDelta(); cc++;
                if (mc.isImproved()) modulesImproved++; else if (mc.getTdiDelta() > 0) modulesWorsened++;
            } else if (b != null) {
                ModuleComparison mc = new ModuleComparison(fn, "removed");
                mc.setTdiBefore(pd(b, "tdi")); mc.setComplexityBefore(pd(b, "complexity_score"));
                mc.setVulnDensityBefore(pd(b, "vulnerability_density"));
                mc.setRiskBefore(b.getOrDefault("risk_classification", "Unknown"));
                comparisons.add(mc); modulesRemoved++;
            } else {
                ModuleComparison mc = new ModuleComparison(fn, "added");
                mc.setTdiAfter(pd(a, "tdi")); mc.setComplexityAfter(pd(a, "complexity_score"));
                mc.setVulnDensityAfter(pd(a, "vulnerability_density"));
                mc.setRiskAfter(a.getOrDefault("risk_classification", "Unknown"));
                comparisons.add(mc); modulesAdded++;
            }
        }
        modulesCompared = cc;
        averageTdiDelta = cc > 0 ? r2(totalDelta / cc) : 0;
        overallTrend = averageTdiDelta < 0 ? "Improved" : averageTdiDelta > 0 ? "Worsened" : "Unchanged";
    }

    public String formatReport() {
        String sep = "=".repeat(78), thin = "-".repeat(72);
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(sep).append("\n  CODESHIELD - BEFORE / AFTER COMPARISON\n");
        sb.append("  Project: ").append(projectName).append("\n  Before:  ").append(beforeTimestamp).append("\n  After:   ").append(afterTimestamp).append("\n").append(sep).append("\n");
        sb.append("\n  COMPARISON SUMMARY\n  ").append("-".repeat(40)).append("\n");
        sb.append(String.format("  Modules compared:     %d%n  Modules added:        %d%n  Modules removed:      %d%n", modulesCompared, modulesAdded, modulesRemoved));
        sb.append(String.format("  Modules improved:     %d%n  Modules worsened:     %d%n  Average TDI delta:    %+.2f%n  Overall trend:        %s%n", modulesImproved, modulesWorsened, averageTdiDelta, overallTrend));
        sb.append(String.format("%n  %-28s %-10s %10s %10s %10s%n", "Module", "Status", "TDI Before", "TDI After", "Delta"));
        sb.append("  ").append(thin).append("\n");
        for (ModuleComparison mc : comparisons) {
            String n = mc.getFilename().length() > 26 ? mc.getFilename().substring(0, 24) + ".." : mc.getFilename();
            if ("compared".equals(mc.getStatus())) {
                String ind = mc.isImproved() ? " v" : mc.getTdiDelta() > 0 ? " ^" : "  ";
                sb.append(String.format("  %-28s %-10s %10.2f %10.2f %+10.2f%s%n", n, "OK", mc.getTdiBefore(), mc.getTdiAfter(), mc.getTdiDelta(), ind));
            } else if ("added".equals(mc.getStatus())) sb.append(String.format("  %-28s %-10s %10s %10.2f %10s%n", n, "ADDED", "---", mc.getTdiAfter(), ""));
            else sb.append(String.format("  %-28s %-10s %10.2f %10s %10s%n", n, "REMOVED", mc.getTdiBefore(), "---", ""));
        }
        List<ModuleComparison> cmp = comparisons.stream().filter(c -> "compared".equals(c.getStatus())).toList();
        if (!cmp.isEmpty()) {
            sb.append(String.format("%n  DETAILED DELTAS%n  %s%n  %-28s %12s %12s %12s%n  %s%n", thin, "Module", "TDI Delta", "CC Delta", "VD Delta", thin));
            for (ModuleComparison mc : cmp) sb.append(String.format("  %-28s %+12.2f %+12.0f %+12.2f%n", mc.getFilename().length() > 26 ? mc.getFilename().substring(0, 24) + ".." : mc.getFilename(), mc.getTdiDelta(), mc.getComplexityDelta(), mc.getVulnDensityDelta()));
            sb.append("\n  RISK TRANSITIONS\n  ").append(thin).append("\n");
            boolean any = false;
            for (ModuleComparison mc : cmp) if (!mc.getRiskBefore().equals(mc.getRiskAfter())) { sb.append(String.format("  %-28s %s -> %s%n", mc.getFilename(), mc.getRiskBefore(), mc.getRiskAfter())); any = true; }
            if (!any) sb.append("  (No risk level changes)\n");
        }
        sb.append("\n").append(sep).append("\n");
        return sb.toString();
    }

    private double pd(Map<String, String> m, String k) { String v = m.get(k); if (v == null) return 0; try { return Double.parseDouble(v); } catch (NumberFormatException e) { return 0; } }
    private double r2(double v) { return Math.round(v * 100.0) / 100.0; }
    public List<ModuleComparison> getComparisons() { return comparisons; }
    public int getModulesCompared() { return modulesCompared; }
    public int getModulesAdded() { return modulesAdded; }
    public int getModulesRemoved() { return modulesRemoved; }
    public int getModulesImproved() { return modulesImproved; }
    public int getModulesWorsened() { return modulesWorsened; }
    public double getAverageTdiDelta() { return averageTdiDelta; }
    public String getOverallTrend() { return overallTrend; }
}
