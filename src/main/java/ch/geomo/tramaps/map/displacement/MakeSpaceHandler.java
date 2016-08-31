package ch.geomo.tramaps.map.displacement;

import ch.geomo.tramaps.map.MetroMap;
import org.jetbrains.annotations.NotNull;

public interface MakeSpaceHandler {

    void makeSpace(@NotNull MetroMap map, double routeMargin, double edgeMargin);

}