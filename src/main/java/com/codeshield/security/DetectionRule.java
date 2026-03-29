package com.codeshield.security;

import java.util.regex.Pattern;

public class DetectionRule {

    private final String ruleId;
    private final String name;
    private final String severity;
    private final String description;
    private final Pattern pattern;
    private final String cweReference;

    public DetectionRule(String ruleId, String name, String severity,
                         String description, String regex, String cweReference) {
        this.ruleId = ruleId; this.name = name; this.severity = severity;
        this.description = description; this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        this.cweReference = cweReference;
    }

    public String getRuleId() { return ruleId; }
    public String getName() { return name; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public Pattern getPattern() { return pattern; }
    public String getCweReference() { return cweReference; }
}
