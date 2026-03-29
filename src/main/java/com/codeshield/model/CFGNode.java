package com.codeshield.model;

public class CFGNode {

    private final int id;
    private final String label;
    private final int lineNumber;

    public CFGNode(int id, String label, int lineNumber) {
        this.id = id;
        this.label = label;
        this.lineNumber = lineNumber;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public int getLineNumber() { return lineNumber; }

    @Override
    public String toString() {
        return "Node{id=" + id + ", label='" + label + "', line=" + lineNumber + "}";
    }
}
