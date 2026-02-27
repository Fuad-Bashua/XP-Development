package com.codeshield.model;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowGraph {

    private final String functionName;
    private final List<CFGNode> nodes;
    private final List<CFGEdge> edges;
    private int connectedComponents;
    private int decisionCount;

    public ControlFlowGraph(String functionName) {
        this.functionName = functionName;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.connectedComponents = 1;
        this.decisionCount = 0;
    }

    public void addNode(CFGNode node) {
        nodes.add(node);
    }

    public void addEdge(CFGEdge edge) {
        if (!edges.contains(edge)) {
            edges.add(edge);
        }
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<CFGNode> getNodes() {
        return nodes;
    }

    public List<CFGEdge> getEdges() {
        return edges;
    }

    public int getNumNodes() {
        return nodes.size();
    }

    public int getNumEdges() {
        return edges.size();
    }

    public int getConnectedComponents() {
        return connectedComponents;
    }

    public void setConnectedComponents(int p) {
        this.connectedComponents = p;
    }

    public int getDecisionCount() {
        return decisionCount;
    }

    public void setDecisionCount(int decisionCount) {
        this.decisionCount = decisionCount;
    }

    @Override
    public String toString() {
        return "CFG{function='" + functionName + "', nodes=" + nodes.size()
                + ", edges=" + edges.size() + ", decisions=" + decisionCount + "}";
    }
}
