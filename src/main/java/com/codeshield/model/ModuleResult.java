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

    public void addFunction(FunctionComplexity fc) { functions.add(fc); }
    public void setAverageComplexity(double v) { this.averageComplexity = v; }
    public void setMaxComplexity(int v) { this.maxComplexity = v; }
    public void setTotalComplexity(int v) { this.totalComplexity = v; }
    public void setTotalLoc(int v) { this.totalLoc = v; }
    public void setStatus(String v) { this.status = v; }
    public void setSkipReason(String v) { this.skipReason = v; }
    public void setSecurityResult(SecurityResult v) { this.securityResult = v; }
    public void setTotalRedFlags(int v) { this.totalRedFlags = v; }
    public void setVulnerabilityDensity(double v) { this.vulnerabilityDensity = v; }
    public void setTdi(double v) { this.tdi = v; }
    public void setRiskClassification(String v) { this.riskClassification = v; }
    public void setHighRisk(boolean v) { this.highRisk = v; }

    public String getFilename() { return filename; }
    public List<FunctionComplexity> getFunctions() { return functions; }
    public double getAverageComplexity() { return averageComplexity; }
    public int getMaxComplexity() { return maxComplexity; }
    public int getTotalComplexity() { return totalComplexity; }
    public int getTotalLoc() { return totalLoc; }
    public String getStatus() { return status; }
    public String getSkipReason() { return skipReason; }
    public SecurityResult getSecurityResult() { return securityResult; }
    public int getTotalRedFlags() { return totalRedFlags; }
    public double getVulnerabilityDensity() { return vulnerabilityDensity; }
    public double getTdi() { return tdi; }
    public String getRiskClassification() { return riskClassification; }
    public boolean isHighRisk() { return highRisk; }
    public boolean isSkipped() { return "skipped/unsupported".equals(status); }
}
