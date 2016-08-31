package ch.geomo.tramaps.map.displacement;

import ch.geomo.tramaps.conflicts.Conflict;
import ch.geomo.tramaps.conflicts.buffer.ElementBuffer;
import ch.geomo.tramaps.geo.Axis;
import ch.geomo.tramaps.geo.util.GeomUtil;
import ch.geomo.tramaps.map.MetroMap;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.math.Vector2D;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScaleHandler implements MakeSpaceHandler {

    private double evaluateScaleFactor(@NotNull Set<Conflict> conflicts, double mapWidth, double mapHeight) {

        double maxMoveX = 0d;
        double maxMoveY = 0d;

        for (Conflict conflict : conflicts) {
            Axis axis = conflict.getBestMoveVectorAxis();
            Vector2D v = conflict.getBestMoveVectorAlongAnAxis();
            if (axis == Axis.X) {
                maxMoveX = Math.max(maxMoveX, v.length());
            }
            else {
                maxMoveY = Math.max(maxMoveY, v.length());
            }
        }

        double scaleFactorAlongX = (mapWidth + maxMoveX) / mapWidth;
        double scaleFactorAlongY = (mapHeight + maxMoveY) / mapHeight;

        return Math.ceil(Math.max(scaleFactorAlongX, scaleFactorAlongY) * 1000) / 1000;

    }

    private void scale(@NotNull MetroMap map, double scaleFactor) {

        AffineTransformation scaleTransformation = new AffineTransformation();
        scaleTransformation.scale(scaleFactor, scaleFactor);

        map.getNodes().forEach(node -> {
            Geometry geom = scaleTransformation.transform(node.getGeometry());
            node.setCoordinate(geom.getCoordinate());
        });

    }

    private void makeSpace(@NotNull MetroMap map, double routeMargin, double edgeMargin, int count) {

        count++;

        System.out.println("makeSpaceByScaling");

        Set<Conflict> conflicts = map.evaluateConflicts(routeMargin, edgeMargin, false)
                .collect(Collectors.toSet());

        if (!conflicts.isEmpty()) {
            Stream<Geometry> buffers = conflicts.stream()
                    .flatMap(conflict -> Stream.of(conflict.getBufferA(), conflict.getBufferB()))
                    .map(ElementBuffer::getBuffer);
            GeometryCollection coll = GeomUtil.createCollection(buffers);
            Envelope bbox = coll.getEnvelopeInternal();
            double scaleFactor = this.evaluateScaleFactor(conflicts, bbox.getWidth(), bbox.getHeight());
            System.out.println("scale factor: " + scaleFactor);
            this.scale(map, scaleFactor);
        }

        // repeat if space issue is not yet solved
        if (conflicts.stream().anyMatch(conflict -> !conflict.isSolved()) && count < 25) {
            this.makeSpace(map, routeMargin, edgeMargin, count);
        }

    }

    @Override
    public void makeSpace(@NotNull MetroMap map, double routeMargin, double edgeMargin) {
        this.makeSpace(map, routeMargin, edgeMargin, 0);
        System.out.println(map);
    }

}