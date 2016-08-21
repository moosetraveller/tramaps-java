package ch.geomo.tramaps.geo;

import ch.geomo.tramaps.geo.util.GeomUtil;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.math.Vector2D;
import org.jetbrains.annotations.NotNull;

public class MoveVector extends Vector2D {

    @SuppressWarnings("unused")
    public static final Vector2D VECTOR_ALONG_X_AXIS = new Vector2D(1, 0);
    @SuppressWarnings("unused")
    public static final Vector2D VECTOR_ALONG_Y_AXIS = new Vector2D(0, 1);

    private final LineString lineString;

    public MoveVector() {
        super(0, 0);
        this.lineString = GeomUtil.createLineString();
    }

    public MoveVector(@NotNull LineString lineString) {
        super(lineString.getStartPoint().getCoordinate(), lineString.getEndPoint().getCoordinate());
        this.lineString = lineString;
    }

    @NotNull
    @SuppressWarnings("unused")
    public LineString getLineString() {
        return lineString;
    }

    @NotNull
    public Vector2D getProjection(@NotNull Vector2D alongVector) {
        return alongVector.multiply(this.dot(alongVector)/alongVector.dot(alongVector));
    }

}
