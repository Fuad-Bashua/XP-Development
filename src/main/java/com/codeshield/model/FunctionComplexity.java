package com.codeshield.model;

public class FunctionComplexity {

    private final String functionName;
    private final int cyclomaticComplexity;
    private final int edges;
    private final int nodes;
    private final int connectedComponents;
    private final String riskLevel;

    public FunctionComplexity(String functionName, int cyclomaticComplexity,
                              int edges, int nodes, int connectedComponents,
                              String riskLevel) {
        this.functionName = functionName;
        this.cyclomaticComplexity = cyclomaticComplexity;
        this.edges = edges;
        this.nodes = nodes;
        this.connectedComponents = connectedComponents;
        this.riskLevel = riskLevel;
    }

    public String getFunctionName() {
        return functionName;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public int getEdges() {
        return edges;
    }

    public int getNodes() {
        return nodes;
    }

    public int getConnectedComponents() {
        return connectedComponents;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    @Override
    public String toString() {
        return functionName + ": CC=" + cyclomaticComplexity
                + " (E=" + edges + ", N=" + nodes + ", P=" + connectedComponents + ")"
                + " [" + riskLevel + "]";
    }
}
