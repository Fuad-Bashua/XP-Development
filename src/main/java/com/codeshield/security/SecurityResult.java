package com.codeshield.security;

import java.util.ArrayList;
import java.util.List;

public class SecurityResult {

    private final String filename;
    private final List<RedFlag> redFlags;

    public SecurityResult(String filename) {
        this.filename = filename;
        this.redFlags = new ArrayList<>();
    }

    public void addRedFlag(RedFlag flag) {
        redFlags.add(flag);
    }

    public String getFilename() { return filename; }
    public List<RedFlag> getRedFlags() { return redFlags; }
    public int getTotalFlags() { return redFlags.size(); }

    public int countBySeverity(String severity) {
        return (int) redFlags.stream()
                .filter(f -> f.getSeverity().equals(severity))
                .count();
    }

    public int getCriticalCount() { return countBySeverity("Critical"); }
    public int getHighCount() { return countBySeverity("High"); }
    public int getMediumCount() { return countBySeverity("Medium"); }
    public int getLowCount() { return countBySeverity("Low"); }
}
