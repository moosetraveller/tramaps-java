/*
 * Copyright (c) 2016-2018 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.tramaps.map.displacement.alg;

import ch.geomo.tramaps.conflict.BufferConflict;
import ch.geomo.tramaps.conflict.Conflict;
import ch.geomo.tramaps.conflict.ConflictType;
import ch.geomo.tramaps.graph.Edge;
import ch.geomo.tramaps.graph.Node;
import ch.geomo.tramaps.map.MetroMap;
import ch.geomo.tramaps.map.displacement.LineSpaceHandler;
import ch.geomo.tramaps.map.displacement.alg.adjustment.EdgeAdjuster;
import ch.geomo.util.collection.list.EnhancedList;
import ch.geomo.util.logging.Loggers;
import com.vividsolutions.jts.geom.Envelope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This {@link LineSpaceHandler} implementation makes space by displacing and moving nodes of the underlying graph.
 */
public class DisplaceLineSpaceHandler implements LineSpaceHandler {

    /**
     * Max iteration until algorithm will be terminated when not found a non-conflict solution.
     */
    private static final int MAX_ITERATIONS = 200;

    private final MetroMap map;

    public DisplaceLineSpaceHandler(@NotNull MetroMap map) {
        this.map = map;
    }

    /**
     * Iterates over all non-octilinear edges and corrects them.
     */
    private void correctNonOctilinearEdges() {
        Loggers.info(this, "Non-Octilinear edges: " + map.countNonOctilinearEdges());
        map.getEdges().stream()
                .filter(Edge::isNotOctilinear)
                .sorted(new NonOctilinearEdgeComparator())
                .forEach(edge -> EdgeAdjuster.correctEdge(map, edge));
    }

//    private boolean hasOnlyEdgeAdjacentNodeConflicts(@NotNull EnhancedList<Conflict> conflicts) {
//        return conflicts.allMatch(conflict -> {
//            if (conflict.getConflictType() == ConflictType.NODE_EDGE) {
//                Node node = ((BufferConflict) conflict).getNodes().get(0);
//                Edge edge = ((BufferConflict) conflict).getEdges().get(0);
//                return node.getAdjacentEdges().stream()
//                        .map(e -> e.getOtherNode(node))
//                        .anyMatch(n -> n.equals(edge.getNodeA()) || n.equals(edge.getNodeB()));
//            }
//            return false;
//        });
//    }

    /**
     * Makes space for line and station signatures by displacing and moving nodes recursively.
     */
    private void makeSpace(int lastIteration, @Nullable Conflict lastConflict, double correctionFactor, boolean majorMisalignmentOnly) {

        int currentIteration = lastIteration + 1;

        EnhancedList<Conflict> conflicts = map.evaluateConflicts(true, correctionFactor, majorMisalignmentOnly);

        Loggers.separator(this);
        Loggers.info(this, "Start iteration: {0}", currentIteration);

        if (!conflicts.isEmpty()) {

            Loggers.warning(this, "Conflicts found: {0}", conflicts.size());

            Conflict conflict = conflicts.get(0);
            if (lastConflict != null
                    && conflicts.size() > 1
                    && Objects.equals(conflict.getBufferA().getElement(), lastConflict.getBufferA().getElement())
                    && Objects.equals(conflict.getBufferB().getElement(), lastConflict.getBufferB().getElement())) {

                // skip conflict to give another conflict a chance to be solved
                Loggers.warning(this, "Skip conflict for one iteration... Take next one.");
                conflict = conflicts.get(1);

            }

            Loggers.flag(this, "Handle conflict: {0}", conflict);
            NodeDisplacer.displace(map, conflict);

            // try to move nodes to correct non-octilinear edges
            correctNonOctilinearEdges();

            Loggers.warning(this, "Uncorrected non-octilinear edges found: {0}", map.countNonOctilinearEdges());

            // repeat as long as max iteration is not reached
            if (currentIteration < MAX_ITERATIONS) {
                makeSpace(currentIteration, conflict, correctionFactor, majorMisalignmentOnly);
            }
            else {
                Loggers.separator(this);
                Loggers.warning(this, "Max number set iteration reached. Stop algorithm.");
            }

        }
        else {
            Loggers.separator(this);
            Loggers.info(this, "No (more) conflicts found.");
        }

    }

    /**
     * @return the bounding box size as a {@link String}
     */
    @NotNull
    private String getBoundingBoxString() {
        Envelope mapBoundingBox = map.getBoundingBox();
        return "Size: " + (int) Math.ceil(mapBoundingBox.getWidth()) + "x" + (int) Math.ceil(mapBoundingBox.getHeight());
    }

    /**
     * Starts algorithm and makes space for line and station signatures by displacing and moving nodes.
     */
    @Override
    public void makeSpace() {

        Loggers.separator(this);
        Loggers.info(this, "Start TRAMAPS algorithm");

        Loggers.separator(this);
        Loggers.info(this, "Make space for edge and node signatures...");
        makeSpace(0, null, 0.25, true);

        Loggers.separator(this);
        Loggers.info(this, "Restore octilinearity...");
        makeSpace(0, null, 1, false);

        Loggers.separator(this);
        Loggers.info(this, getBoundingBoxString());
        map.evaluateConflicts(true)
                .doIfNotEmpty(list -> Loggers.warning(this, "Remaining conflicts found! :-("))
                .forEach(conflict -> Loggers.warning(this, "-> {0}", conflict));
        Loggers.separator(this);

    }

}
