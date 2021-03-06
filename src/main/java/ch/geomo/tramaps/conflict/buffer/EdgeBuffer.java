/*
 * Copyright (c) 2016-2018 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.tramaps.conflict.buffer;

import ch.geomo.tramaps.graph.Edge;
import ch.geomo.tramaps.graph.GraphElement;
import ch.geomo.util.geom.GeomUtil;
import com.vividsolutions.jts.geom.Polygon;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Observable;

/**
 * Represents the buffer of a {@link Edge}.
 */
public class EdgeBuffer implements ElementBuffer {

    private final Edge edge;
    private final double routeMargin;
    private final double edgeMargin;

    private Polygon buffer;

    public EdgeBuffer(@NotNull Edge edge, double routeMargin, double edgeMargin) {
        this.edge = edge;
        edge.addObserver(this);
        this.routeMargin = routeMargin;
        this.edgeMargin = edgeMargin;
        updateBuffer();
    }

    /**
     * Initialize or updates this buffer representation.
     */
    private void updateBuffer() {
        double width = edge.calculateEdgeWidth(routeMargin) + edgeMargin * 2;
        buffer = GeomUtil.createBuffer(edge.getLineString(), width / 2, true);
    }

    @NotNull
    @Override
    public Polygon getBuffer() {
        return buffer;
    }

    @NotNull
    @Override
    public GraphElement getElement() {
        return edge;
    }

    /**
     * Notifies the observers.
     */
    @Override
    public void update(Observable o, Object arg) {
        updateBuffer();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EdgeBuffer
                && Objects.equals(edge, ((EdgeBuffer) obj).edge)
                && Objects.equals(edgeMargin, ((EdgeBuffer) obj).edgeMargin)
                && Objects.equals(routeMargin, ((EdgeBuffer) obj).routeMargin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, edgeMargin, routeMargin);
    }

    @Override
    public String toString() {
        return "EdgeBuffer: {" + edge + "}";
    }
}
