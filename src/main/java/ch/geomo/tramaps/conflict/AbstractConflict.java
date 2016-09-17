/*
 * Copyright (c) 2016 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.tramaps.conflict;

import ch.geomo.tramaps.conflict.buffer.ElementBuffer;
import ch.geomo.tramaps.conflict.buffer.ElementBufferPair;
import ch.geomo.tramaps.geom.Axis;
import ch.geomo.tramaps.geom.MoveVector;
import ch.geomo.tramaps.graph.Edge;
import ch.geomo.tramaps.graph.GraphElement;
import ch.geomo.tramaps.graph.Node;
import ch.geomo.tramaps.graph.util.OctilinearDirection;
import ch.geomo.util.collection.pair.Pair;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector2D;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.geomo.tramaps.conflict.ConflictFinder.CONFLICT_COMPARATOR;
import static ch.geomo.tramaps.geom.Axis.X;

public abstract class AbstractConflict implements Conflict {

    protected final ElementBufferPair buffers;

    protected Pair<MoveVector> projection;

    protected ConflictType conflictType;
    protected MoveVector displaceVector;
    protected MoveVector bestDisplaceVector;
    protected Axis bestDisplaceAxis;
    protected Coordinate bestDisplaceStartPoint;
    protected boolean solved = false;

    public AbstractConflict(@NotNull Pair<ElementBuffer> bufferPair) {
        this(bufferPair.getFirst(), bufferPair.getSecond());
    }

    public AbstractConflict(@NotNull ElementBuffer bufferA, @NotNull ElementBuffer bufferB) {
        buffers = new ElementBufferPair(bufferA, bufferB);
    }

    @NotNull
    protected MoveVector getProjection() {
        return projection.getFirst();
    }

    @NotNull
    protected MoveVector getRejection() {
        return projection.getSecond();
    }

    @Override
    @NotNull
    public ConflictType getConflictType() {
        return conflictType;
    }

    @Override
    @NotNull
    public MoveVector getDisplaceVector() {
        return displaceVector;
    }

    @Override
    @NotNull
    public Vector2D getBestDisplaceVector() {
        return bestDisplaceVector;
    }

    @Override
    @NotNull
    public Axis getBestDisplaceAxis() {
        return bestDisplaceAxis;
    }

    /**
     * @return best move direction along an axis
     * @see OctilinearDirection#NORTH
     * @see OctilinearDirection#EAST
     */
    @Override
    @NotNull
    public OctilinearDirection getBestDisplaceDirection() {
        if (bestDisplaceAxis == X) {
            return OctilinearDirection.EAST;
        }
        return OctilinearDirection.NORTH;
    }

    /**
     * @return best move distance along the best move direction
     * @see #getBestDisplaceDirection()
     */
    @Override
    public int getBestDisplaceDistance() {
        // we do work with int values only
        return (int) Math.ceil(Math.abs(bestDisplaceVector.length()));
    }

    @Override
    @NotNull
    public ElementBuffer getBufferA() {
        return buffers.first();
    }

    @Override
    @NotNull
    public ElementBuffer getBufferB() {
        return buffers.second();
    }

    @NotNull
    public List<Node> getNodes() {
        return Stream.of(getBufferA().getElement(), getBufferB().getElement())
                .filter(element -> element instanceof Node)
                .map(element -> (Node) element)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<Edge> getEdges() {
        return Stream.of(getBufferA().getElement(), getBufferB().getElement())
                .filter(element -> element instanceof Edge)
                .map(element -> (Edge) element)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isNotSolved() {
        return !solved;
    }

    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    @NotNull
    public Coordinate getDisplaceOriginPoint() {
        return bestDisplaceStartPoint;
    }

    @Override
    public double getDisplaceDistanceAlongX() {
        return Math.ceil(Math.abs(projection.getFirst().length()));
    }

    @Override
    public double getDisplaceDistanceAlongY() {
        return Math.ceil(Math.abs(projection.getSecond().length()));
    }

    private boolean isConflictElement(@NotNull GraphElement graphElement) {
        return graphElement.equals(getBufferA().getElement())
                || graphElement.equals(getBufferB().getElement());
    }

    private boolean isAdjacentToConflictElement(@NotNull GraphElement graphElement) {
        return graphElement.isAdjacent(getBufferA().getElement())
                || graphElement.isAdjacent(getBufferB().getElement());
    }

    @Override
    public boolean isConflictRelated(@NotNull GraphElement graphElement) {
        return isConflictElement(graphElement) || isAdjacentToConflictElement(graphElement);
    }

    @Override
    public int compareTo(@NotNull Conflict o) {
        return CONFLICT_COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractConflict
                && (Objects.equals(buffers, ((AbstractConflict) obj).buffers));
    }

    @Override
    public int hashCode() {
        return Objects.hash(buffers);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": {" + getBufferA() + ", " + getBufferB() + ", distance=" + getBestDisplaceDistance() + ", point=" + bestDisplaceStartPoint + ", axis=" + bestDisplaceAxis + "}";
    }

}