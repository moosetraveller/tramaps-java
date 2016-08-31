package ch.geomo.tramaps.map;

import ch.geomo.tramaps.geo.util.GeomUtil;
import ch.geomo.tramaps.graph.Edge;
import ch.geomo.tramaps.graph.Node;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.jetbrains.annotations.NotNull;

import java.util.Observable;
import java.util.Observer;

/**
 * A simple implementation of a station signature. The signature's form
 * is a rectangle.
 */
public class RectangleStationSignature extends StationSignature {

    private static final double MIN_SIDE_LENGTH = 20d;
    private static final double ROUTE_MARGIN = 5d;

    private final Node node;
    private Polygon signature;

    public RectangleStationSignature(@NotNull Node node) {
        this.node = node;
        node.addObserver(this);
        updateSignature();
    }

    /**
     * Updates the signature properties. Recalculates the signature geometry if the node's
     * x- and y-value where changed.
     */
    private void updateSignature() {
        double width = node.getAdjacentEdges().stream()
                .filter(edge -> !edge.isHorizontal())
                .map(edge -> edge.getEdgeWidth(ROUTE_MARGIN))
                .max(Double::compare)
                .orElse(ROUTE_MARGIN);
        double height = node.getAdjacentEdges().stream()
                .filter(edge -> !edge.isVertical())
                .map(edge -> edge.getEdgeWidth(ROUTE_MARGIN))
                .max(Double::compare)
                .orElse(ROUTE_MARGIN);
        signature = GeomUtil.createPolygon(node.getPoint(), Math.max(width, MIN_SIDE_LENGTH), Math.max(height, MIN_SIDE_LENGTH));
        setChanged();
        notifyObservers();
    }

    /**
     * @see StationSignature#getConvexHull()
     */
    @NotNull
    @Override
    public Geometry getConvexHull() {
        return this.signature.convexHull();
    }

    /**
     * @see StationSignature#getGeometry()
     */
    @NotNull
    @Override
    public Polygon getGeometry() {
        return this.signature;
    }

    /**
     * @see Observer#update(Observable, Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        this.updateSignature();
    }

}
