/*
 * Copyright (c) 2016 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.tramaps.graph;

import ch.geomo.tramaps.geo.util.GeomUtil;
import ch.geomo.tramaps.graph.util.AnyDirection;
import ch.geomo.tramaps.graph.util.Direction;
import ch.geomo.tramaps.graph.util.OctilinearDirection;
import ch.geomo.util.pair.Pair;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents an edge within a {@link Graph}. Each edge has a name, a position,
 * a start node, an end node and a {@link Set} of routes. When comparing
 * two edges, the position of the nodes won't be considered.
 */
public class Edge extends Observable implements Observer, GraphElement {

    private String name;

    private Node nodeA;
    private Node nodeB;
    private Pair<Node> nodePair;

    private Set<Route> routes;

    private LineString lineString;
    private Direction direction;

    private boolean deleted;

    /**
     * Creates a new instance of {@link Edge} with given nodes.
     */
    public Edge(@NotNull Node nodeA, @NotNull Node nodeB) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        nodeA.addAdjacentEdge(this);
        nodeB.addAdjacentEdge(this);
        routes = new HashSet<>();
        nodePair = Pair.of(nodeA, nodeB);
        updateEdge();
    }

    public Edge(@NotNull Node nodeA, @NotNull Node nodeB, @NotNull String name) {
        this(nodeA, nodeB);
        this.name = name;
    }

    /**
     * Calculates the edge width of this edge using given margin between
     * the routes.
     *
     * @return the width of this edge
     */
    public double calculateEdgeWidth(double routeMargin) {
        double width = getRoutes().stream()
                .mapToDouble(Route::getLineWidth)
                .sum();
        return width + routeMargin * (getRoutes().size() - 2);
    }

    /**
     * @return the edge's name if available
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Sets the edge's name.
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * @return the first node of this edge
     */
    @NotNull
    public Node getNodeA() {
        return nodeA;
    }

    /**
     * @return the second node of this edge
     */
    @NotNull
    public Node getNodeB() {
        return nodeB;
    }

    /**
     * Updates the {@link LineString} representation and notifies Observers.
     */
    protected final void updateEdge() {
        lineString = GeomUtil.createLineString(nodeA, nodeB);
        double angle = Math.ceil(GeomUtil.getAngleToXAxisAsDegree(lineString));
        direction = AnyDirection.fromAngle(angle);
        setChanged();
        notifyObservers();
    }

    /**
     * Adds given routes to this edge and notifies Observers. Ignores
     * duplicated routes.
     */
    public void addRoutes(@NotNull Collection<Route> routes) {
        this.routes.addAll(routes);
        setChanged();
        notifyObservers();
    }

    /**
     * Adds given routes to this edge and notifies Observers. Ignores
     * duplicated routes.
     */
    public void addRoutes(@NotNull Route... routes) {
        addRoutes(Arrays.asList(routes));
    }

    /**
     * @return an unmodifiable {@link Set} with all routes
     */
    @NotNull
    public Set<Route> getRoutes() {
        // unmodifiable in order to avoid side effects
        return Collections.unmodifiableSet(routes);
    }

    /**
     * @throws NoSuchElementException if given node is not an end node of this edge
     */
    @NotNull
    public Node getOtherNode(@NotNull Node node) {
        return nodePair.getOtherValue(node);
    }

    @Override
    @Contract("null->false")
    public boolean isAdjacent(@Nullable Edge edge) {
        return edge != null && (getNodeA().getAdjacentEdges().contains(edge) || getNodeB().getAdjacentEdges().contains(edge));
    }

    @Override
    @Contract("null->false")
    public boolean isAdjacent(@Nullable Node node) {
        return nodeA.equals(node) || nodeB.equals(node);
    }

    @NotNull
    public LineString getLineString() {
        return lineString;
    }

    @NotNull
    @Override
    public Geometry getGeometry() {
        return lineString;
    }

    @Override
    public void update(Observable o, Object arg) {
        updateEdge();
    }

    public boolean isNonOctilinear() {
        return !OctilinearDirection.isOctilinear(direction);
    }

    /**
     * @return true if vertical to x-axis
     */
    public boolean isVertical() {
        return direction.isVertical();
    }

    /**
     * @return true if horizontal to x-axis
     */
    public boolean isHorizontal() {
        return direction.isHorizontal();
    }

    /**
     * @return true if neither vertical nor horizontal to x-axis but octliniear
     */
    public boolean isDiagonal() {
        return direction.isDiagonal();
    }

    /**
     * @return the direction of this edge from <b>node A</b>
     * @see #getDirection(boolean)
     */
    @NotNull
    public Direction getDirection() {
        return getDirection(true);
    }

    /**
     * @return the direction of this edge from <b>node A or B depending on the flag</b>
     */
    @NotNull
    public Direction getDirection(boolean nodeA) {
        if (nodeA) {
            return direction;
        }
        return direction.opposite();
    }

    /**
     * @return the direction of this edge from <b>node A or B depending on the node</b>
     * @throws IllegalArgumentException if given node is neither equal to node A nor node B
     */
    public Direction getDirection(@NotNull Node node) {
        if (nodeA.equals(node)) {
            return getDirection(true);
        }
        else if (nodeB.equals(node)) {
            return getDirection(false);
        }
        throw new IllegalArgumentException("Node must be equals to node A or B.");
    }

    public double getAngle() {
        return direction.getAngle();
    }

    /**
     * @return true since this implementation of {@link GraphElement} is an edge ;-)
     */
    @Override
    @Contract("->true")
    public boolean isEdge() {
        return true;
    }

    /**
     * @return false since this implementation of {@link GraphElement} is an edge ;-)
     */
    @Override
    @Contract("->false")
    public boolean isNode() {
        return false;
    }

    @Contract("null->true")
    public boolean isNotEquals(@Nullable Edge edge) {
        return !equals(edge);
    }

    /**
     * @return true if given edge has the same nodes than this instance
     */
    public boolean equalNodes(Edge edge) {
        return getNodeA().isAdjacent(edge) && getNodeB().isAdjacent(edge);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete() {
        // remove from adjacent nodes
        getNodeA().removeAdjacentEdge(this);
        getNodeB().removeAdjacentEdge(this);
        deleted = true;
        // notify observers a last time
        setChanged();
        notifyObservers();
        // unsubscribe all observers
        deleteObservers();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO compare routes as well
        return obj instanceof Edge
                && Objects.equals(name, ((Edge) obj).name)
                && nodeA.equals(((Edge) obj).nodeA)
                && nodeB.equals(((Edge) obj).nodeB)
                && deleted == ((Edge) obj).deleted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nodeA, nodeB, deleted);
    }

    @Override
    public String toString() {
        String name = Optional.ofNullable(getName())
                .orElse(getNodeA().getName() + " <-> " + getNodeB().getName());
        return "Edge: {" + name + "}";
    }

    @Contract(pure = true)
    public boolean hasRoutes() {
        return !routes.isEmpty();
    }
}
