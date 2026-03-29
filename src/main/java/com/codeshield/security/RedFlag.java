package com.codeshield.security;

public class RedFlag {

    private final String ruleId;
    private final String ruleName;
    private final String severity;
    private final String description;
    private final int lineNumber;
    private final String lineContent;
    private final String cweReference;

    public RedFlag(String ruleId, String ruleName, String severity, String description,
                   int lineNumber, String lineContent, String cweReference) {
        this.ruleId = ruleId; this.ruleName = ruleName; this.severity = severity;
        this.description = description; this.lineNumber = lineNumber;
        this.lineContent = lineContent; this.cweReference = cweReference;
    }

    public String getRuleId() { return ruleId; }
    public String getRuleName() { return ruleName; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public int getLineNumber() { return lineNumber; }
    public String getLineContent() { return lineContent; }
    public String getCweReference() { return cweReference; }
}
