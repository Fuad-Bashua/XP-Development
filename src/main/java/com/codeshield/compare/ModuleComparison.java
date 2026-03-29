package com.codeshield.compare;

public class ModuleComparison {

    private final String filename;
    private final String status;
    private double tdiBefore, tdiAfter, tdiDelta;
    private double complexityBefore, complexityAfter, complexityDelta;
    private double vulnDensityBefore, vulnDensityAfter, vulnDensityDelta;
    private String riskBefore, riskAfter;
    private boolean improved;

    public ModuleComparison(String filename, String status) { this.filename = filename; this.status = status; }

    public String getFilename() { return filename; }
    public String getStatus() { return status; }
    public double getTdiBefore() { return tdiBefore; }
    public void setTdiBefore(double v) { this.tdiBefore = v; }
    public double getTdiAfter() { return tdiAfter; }
    public void setTdiAfter(double v) { this.tdiAfter = v; }
    public double getTdiDelta() { return tdiDelta; }
    public void setTdiDelta(double v) { this.tdiDelta = v; }
    public double getComplexityBefore() { return complexityBefore; }
    public void setComplexityBefore(double v) { this.complexityBefore = v; }
    public double getComplexityAfter() { return complexityAfter; }
    public void setComplexityAfter(double v) { this.complexityAfter = v; }
    public double getComplexityDelta() { return complexityDelta; }
    public void setComplexityDelta(double v) { this.complexityDelta = v; }
    public double getVulnDensityBefore() { return vulnDensityBefore; }
    public void setVulnDensityBefore(double v) { this.vulnDensityBefore = v; }
    public double getVulnDensityAfter() { return vulnDensityAfter; }
    public void setVulnDensityAfter(double v) { this.vulnDensityAfter = v; }
    public double getVulnDensityDelta() { return vulnDensityDelta; }
    public void setVulnDensityDelta(double v) { this.vulnDensityDelta = v; }
    public String getRiskBefore() { return riskBefore; }
    public void setRiskBefore(String v) { this.riskBefore = v; }
    public String getRiskAfter() { return riskAfter; }
    public void setRiskAfter(String v) { this.riskAfter = v; }
    public boolean isImproved() { return improved; }
    public void setImproved(boolean v) { this.improved = v; }
}
