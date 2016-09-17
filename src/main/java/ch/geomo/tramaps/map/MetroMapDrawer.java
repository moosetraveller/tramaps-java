/*
 * Copyright (c) 2016 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.tramaps.map;

import ch.geomo.tramaps.conflict.BufferConflict;
import ch.geomo.tramaps.graph.Edge;
import ch.geomo.tramaps.map.signature.BendNodeSignature;
import com.vividsolutions.jts.geom.Envelope;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

public class MetroMapDrawer {

    private final MetroMap map;
    private final double margin;

    public MetroMapDrawer(@NotNull MetroMap map, double margin) {
        this.map = map;
        this.margin = margin;
    }

    private void drawEdge(@NotNull Edge edge, @NotNull GraphicsContext context) {
        context.strokeLine(edge.getNodeA().getX(), edge.getNodeA().getY(), edge.getNodeB().getX(), edge.getNodeB().getY());
    }

    public void draw(@NotNull GraphicsContext context, @NotNull Envelope bbox) {

        // start drawing at the top left
        context.translate(-bbox.getMinX() + margin, -bbox.getMinY() + margin);

        int max = (int) Math.ceil(Math.max(bbox.getMaxX(), bbox.getMaxY())) + 1000;
        for (int i = -max, j = 0; i < max; i = i + 25, j = j + 25) {
            context.setStroke(j % 100 == 0 ? Color.GRAY : Color.LIGHTGRAY);
            context.strokeLine(i, -max, i, max * 2);
            context.strokeLine(-max, i, max * 2, i);
        }

        map.getEdges().stream()
                .filter(Edge::hasRoutes)
                .forEach(edge -> {
                    double width = edge.calculateEdgeWidth(map.getRouteMargin());
                    context.setLineWidth(width);
                    context.setStroke(Color.rgb(139, 187, 206, 0.5d));
                    context.setLineCap(StrokeLineCap.BUTT);
                    drawEdge(edge, context);
                });
        map.getNodes().stream()
                .filter(node -> !(node.getNodeSignature() instanceof BendNodeSignature))
                .forEach(node -> {
                    Envelope station = node.getNodeSignature().getGeometry().getEnvelopeInternal();
                    context.setFill(Color.BLACK);
                    context.fillRoundRect(station.getMinX() - 5, station.getMinY() - 5, station.getWidth() + 10, station.getHeight() + 10, 25, 25);
                    context.setFill(Color.WHITE);
                    context.fillRoundRect(station.getMinX(), station.getMinY(), station.getWidth(), station.getHeight(), 25, 25);
                });
        map.getEdges().forEach(edge -> {
            context.setLineWidth(2);
            context.setStroke(Color.BLACK);
            drawEdge(edge, context);
        });
        context.translate(-5, -5);
        map.getNodes().forEach(node -> {
            context.setFill(Color.rgb(0, 145, 255));
            context.fillOval(node.getX(), node.getY(), 10, 10);
        });

        context.translate(5, 5);
        map.evaluateConflicts(true)
                .filter(conflict -> conflict instanceof BufferConflict)
                .forEach(conflict -> {
                    context.setFill(Color.rgb(240, 88, 88, 0.4));
                    Envelope bbox2 = ((BufferConflict)conflict).getConflictPolygon().getEnvelopeInternal();
                    context.fillRect(bbox2.getMinX(), bbox2.getMinY(), bbox2.getWidth(), bbox2.getHeight());

//                    context.setFill(Color.rgb(20, 172, 0, 0.4));
//                    Envelope bbox3 = conflict.getBufferA().getBuffer().getEnvelopeInternal();
//                    context.fillRect(bbox3.getMinX(), bbox3.getMinY(), bbox3.getWidth(), bbox3.getHeight());
//                    context.setFill(Color.rgb(176, 93, 117, 0.4));
//                    Envelope bbox4 = conflict.getBufferB().getBuffer().getEnvelopeInternal();
//                    context.fillRect(bbox4.getMinX(), bbox4.getMinY(), bbox4.getWidth(), bbox4.getHeight());
                });

        map.getNodes().forEach(node -> {
            Envelope station = node.getNodeSignature().getGeometry().getEnvelopeInternal();
            context.setStroke(Color.BLACK);
            context.setLineWidth(1);
            context.strokeText(node.getName() + "(" + Math.round(node.getX()) + "/" + Math.round(node.getY()) + ")", station.getMinX() - 50, station.getMaxY() + 20);
        });

    }

}
