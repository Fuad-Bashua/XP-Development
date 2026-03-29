package com.codeshield.model;

import java.util.Objects;

public class CFGEdge {

    private final int source;
    private final int target;

    public CFGEdge(int source, int target) {
        this.source = source;
        this.target = target;
    }

    public int getSource() { return source; }
    public int getTarget() { return target; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CFGEdge edge = (CFGEdge) o;
        return source == edge.source && target == edge.target;
    }

    @Override
    public int hashCode() { return Objects.hash(source, target); }
}
