package org.smojol.analysis.graph.graphml;

public interface TypedGraphVertex {
    String id();
    String type();
    String label();
    String name();
    String text();
    String namespace();
}
