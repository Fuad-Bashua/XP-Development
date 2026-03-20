package com.codeshield.model;

import com.codeshield.security.SecurityResult;

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
    private SecurityResult securityResult;
    private int totalRedFlags;
    private double vulnerabilityDensity;
    private double tdi;
    private String riskClassification;
    private boolean highRisk;

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

    public void setSecurityResult(SecurityResult securityResult) {
        this.securityResult = securityResult;
    }

    public SecurityResult getSecurityResult() {
        return securityResult;
    }

    public void setTotalRedFlags(int totalRedFlags) {
        this.totalRedFlags = totalRedFlags;
    }

    public int getTotalRedFlags() {
        return totalRedFlags;
    }

    public void setVulnerabilityDensity(double vulnerabilityDensity) {
        this.vulnerabilityDensity = vulnerabilityDensity;
    }

    public double getVulnerabilityDensity() {
        return vulnerabilityDensity;
    }

    public void setTdi(double tdi) { this.tdi = tdi; }
    public double getTdi() { return tdi; }

    public void setRiskClassification(String riskClassification) { this.riskClassification = riskClassification; }
    public String getRiskClassification() { return riskClassification; }

    public void setHighRisk(boolean highRisk) { this.highRisk = highRisk; }
    public boolean isHighRisk() { return highRisk; }
}
