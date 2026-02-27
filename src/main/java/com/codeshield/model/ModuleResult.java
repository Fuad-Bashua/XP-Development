package com.codeshield.model;

import java.util.ArrayList;
import java.util.List;

public class ModuleResult {

    private final String filename;
    private final List<FunctionComplexity> functions;
    private double averageComplexity;
    private int maxComplexity;
    private int totalComplexity;
    private int totalLoc;
    private String status;
    private String skipReason;

    public ModuleResult(String filename) {
        this.filename = filename;
        this.functions = new ArrayList<>();
        this.status = "analysed";
    }

    public void addFunction(FunctionComplexity fc) {
        functions.add(fc);
    }

    public void setAverageComplexity(double averageComplexity) {
        this.averageComplexity = averageComplexity;
    }

    public void setMaxComplexity(int maxComplexity) {
        this.maxComplexity = maxComplexity;
    }

    public void setTotalComplexity(int totalComplexity) {
        this.totalComplexity = totalComplexity;
    }

    public void setTotalLoc(int totalLoc) {
        this.totalLoc = totalLoc;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSkipReason(String skipReason) {
        this.skipReason = skipReason;
    }

    public String getFilename() {
        return filename;
    }

    public List<FunctionComplexity> getFunctions() {
        return functions;
    }

    public double getAverageComplexity() {
        return averageComplexity;
    }

    public int getMaxComplexity() {
        return maxComplexity;
    }

    public int getTotalComplexity() {
        return totalComplexity;
    }

    public int getTotalLoc() {
        return totalLoc;
    }

    public String getStatus() {
        return status;
    }

    public String getSkipReason() {
        return skipReason;
    }

    public boolean isSkipped() {
        return "skipped/unsupported".equals(status);
    }
}
